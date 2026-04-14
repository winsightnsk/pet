package t03.web.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import t03.datasource.model.HumanEntity;
import t03.datasource.model.SessionEntity;
import t03.datasource.repository.SessionRepository;
import t03.domain.DomainInterface;
import t03.web.mapper.WebMapperComponent;
import t03.web.model.HashHuman;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class SessionService {

    private final SessionRepository sessionRepository;
    private final DomainInterface domain;
    private final WebMapperComponent webMapper;

    public UUID createSession(HashHuman human, HttpServletRequest request) {
        return createSession(domain.heByHH(human), request);
    }
    public UUID createSession(HumanEntity human, HttpServletRequest request) {
        String ipAddress = getClientIp(request);
        String userAgent = request.getHeader("User-Agent");
        SessionEntity session = new SessionEntity(human, ipAddress, userAgent);
        sessionRepository.save(session);
        return session.getId();
    }

    public void setSessionCookie(HttpServletResponse response, UUID token) {
        Cookie sessionCookie = new Cookie("sessionToken", token.toString());
        sessionCookie.setHttpOnly(true);      // Недоступен из JavaScript
        sessionCookie.setSecure(false);        // Только по HTTPS (для продакшена)
        sessionCookie.setPath("/");
        sessionCookie.setMaxAge(7 * 60 * 60); // 7 часов
        sessionCookie.setAttribute("SameSite", "Strict"); // Защита от CSRF
        response.addCookie(sessionCookie);
    }

    public void removeSessionCookie(HttpServletResponse response) {
        Cookie sessionCookie = new Cookie("sessionToken", "");
        sessionCookie.setHttpOnly(true);
        sessionCookie.setSecure(false);
        sessionCookie.setPath("/");
        sessionCookie.setMaxAge(0); // Немедленное удаление

        response.addCookie(sessionCookie);
    }

    /**
     * Проверяет валидность сессии по токену из cookie
     */
    public Optional<HashHuman> getHumanFromSession(Cookie[] cookies, HttpServletRequest request) {
        if (cookies == null) return Optional.empty();

        String token = null;
        for (Cookie cookie : cookies)
            if ("sessionToken".equals(cookie.getName()))
                token = cookie.getValue();

        if (token == null) return Optional.empty();

        UUID id = UUID.fromString(token);
        Optional<SessionEntity> optSession = sessionRepository.findById(id);

        if (optSession.isEmpty()) return Optional.empty();
        SessionEntity session = optSession.get();

        if (session.expired()) {
            sessionRepository.delete(session);
            return Optional.empty();
        }

        session.touch();
        sessionRepository.save(session);
        return Optional.ofNullable(webMapper.heTohh(session.getHuman()));
    }


    public void invalidateSession(Cookie[] cookies, HttpServletResponse response) {
        if (cookies == null) return;

        for (Cookie cookie : cookies)
            if ("sessionToken".equals(cookie.getName()))
                sessionRepository.deleteById(UUID.fromString(cookie.getValue()));

        removeSessionCookie(response);
    }

    /**
     * Получение реального IP клиента (учитывая прокси)
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    /**
     * Периодическая очистка просроченных сессий (каждый час)
     */
    @Scheduled(fixedRate = 3600000) // Каждый час
    public void cleanExpiredSessions() {
        sessionRepository.deleteExpiredSessions(LocalDateTime.now());
    }

}
