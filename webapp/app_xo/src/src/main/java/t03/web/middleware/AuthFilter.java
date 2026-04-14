package t03.web.middleware;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;
import t03.web.model.HashHuman;
import t03.web.service.AuthService;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class AuthFilter extends GenericFilterBean {

    private static final Logger logger = LoggerFactory.getLogger(AuthFilter.class.getName());

    @Autowired
    private AuthService authService;

    private static final List<String> uriPrefix = List.of(
            "/api/"
    );
    private static final List<String> excludeUriPath = List.of(
            "/api/register",
            "/api/login"
    );

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String path = httpRequest.getRequestURI();

        Optional<HashHuman> ohh = authService.authenticate(httpRequest.getHeader("Authorization"));

        if ((uriPrefix.stream().anyMatch(path::startsWith) && ohh.isPresent()) ||
                (uriPrefix.stream().noneMatch(path::startsWith)) ||
                (excludeUriPath.contains(path))) {
            ohh.ifPresent(hh -> request.setAttribute("hashhuman", hh));
            chain.doFilter(request, response);
            return;
        }

        httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        httpResponse.setContentType("application/json");
        httpResponse.setCharacterEncoding("UTF-8");
        httpResponse.getWriter().write("{\"error\":\"Запрос должен быть авторизован\"}");
        httpResponse.getWriter().flush();
    }

}
