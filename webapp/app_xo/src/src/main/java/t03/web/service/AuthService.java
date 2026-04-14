package t03.web.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import t03.datasource.model.HumanEntity;
import t03.datasource.repository.HumanInterface;
import t03.web.mapper.WebMapperComponent;
import t03.web.model.HashHuman;
import t03.web.model.api.MR;
import t03.web.model.api.SignUpRequest;

import java.util.Base64;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@AllArgsConstructor
public class AuthService {

    static final Pattern NAME_PATTERN = Pattern.compile("^[\\p{IsCyrillic}\\p{IsLatin}-_\\d]+$");

    private final HumanInterface humanRepository;
    private final WebMapperComponent webMapper;
    private final PasswordEncoder passwordEncoder;
    private final SessionService sessionService;

    public HumanEntity createHuman(String name, String rawPassword) {
        UUID id = UUID.randomUUID();
        // BCrypt сам генерирует и хранит соль
        String bcryptHash = passwordEncoder.encode(rawPassword);
        HumanEntity human = new HumanEntity(id, bcryptHash, name);
        humanRepository.save(human);
        return human;
    }

    public boolean verifyPassword(HumanEntity human, String rawPassword) {
        // BCrypt автоматически извлекает соль из хеша
        return passwordEncoder.matches(rawPassword, human.getPassword());
    }

    /**
     * Возвращае инстанс датамодели либо создаёт новый.
     * Если Логин есть, а пароль не тот,
     * а так-же при плохих данных возвращает пустой опшнал
     * @param name - Имя(логин)
     * @param password - Пароль как есть
     */
    public Optional<HumanEntity> HEbyNamePass(String name, String password, boolean createIfNotExist) {
        if (name == null || password == null) return Optional.empty();
        if (name.trim().isEmpty() || password.trim().isEmpty()) return Optional.empty();
        if (!NAME_PATTERN.matcher(name.strip()).matches()) return Optional.empty();
        Optional<HumanEntity> ohe = humanRepository.findByName(name);
        if (ohe.isPresent()) {
            if (verifyPassword(ohe.get(), password)) return ohe;
            return Optional.empty();
        }
        if (!createIfNotExist) return Optional.empty();
        HumanEntity newHuman = createHuman(name, password);
        return Optional.of(newHuman);
    }

    /**
     * Материализует НОВЫЙ Имя|Пароль в БД.
     * @return факт успешной регистрации;
     */
    public MR<Boolean> register(SignUpRequest request) {
        if (request.getLogin() == null || request.getLogin().trim().isEmpty())
            return new MR<>(false).status(HttpStatus.BAD_REQUEST)
                    .error("Имя пользователя отсутствуeт");
        if (request.getPassword() == null || request.getPassword().trim().isEmpty())
            return new MR<>(false).status(HttpStatus.BAD_REQUEST)
                    .error("Пароль отсутствуeт");
        if (!NAME_PATTERN.matcher(request.getLogin()).matches())
            return new MR<>(false).status(HttpStatus.BAD_REQUEST)
                    .error("Недопустимые символы имени");
        if (humanRepository.findByName(request.getLogin()).isPresent())
            return new MR<>(false).status(HttpStatus.BAD_REQUEST)
                    .error("Игрок с таким именем уже существует");
        createHuman(request.getLogin(), request.getPassword());
        return new MR<>(true);
    }

    /**
     * Авторизация|Регистрация по заголовку
     */
    public Optional<HashHuman> authenticate(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Basic "))
            return Optional.empty();
        try {
            String base64Credentials = authHeader.substring(6);
            String credentials = new String(Base64.getDecoder().decode(base64Credentials));
            String[] values = credentials.split(":", 2);
            if (values.length != 2) {
                return Optional.empty();
            }
            String login = values[0];
            String password = values[1];

            Optional<HumanEntity> ohe = HEbyNamePass(login, password, false);
            return ohe.map(webMapper::heTohh);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public HashHuman checkAuth (Model model,
                            HttpServletRequest request,
                            HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        Optional<HashHuman> ohh = sessionService.getHumanFromSession(cookies, request);
        if (ohh.isEmpty()) {
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    cookie.setValue(null);
                    cookie.setMaxAge(0);
                    cookie.setPath("/");
                    response.addCookie(cookie);
                }
            }
            throw new RuntimeException ("Ошибка авторизации");
        }
        return ohh.get();
    }

}
