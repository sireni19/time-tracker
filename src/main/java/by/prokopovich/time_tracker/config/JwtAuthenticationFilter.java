package by.prokopovich.time_tracker.config;

import by.prokopovich.time_tracker.service.JwtService;
import by.prokopovich.time_tracker.service.UserService;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserService userService;
    /**
     * Фильтр обеспечивает проверку существования пользователя в системе и валиден ли его токен
     * Этот метод извлекает токен аутентификации из заголовка "Authorization" HTTP-запроса,
     * проверяет его и, если токен действителен, устанавливает соответствующий объект аутентификации
     * в контексте безопасности Spring.
     *
     * @param request - HTTP-запрос, который необходимо отфильтровать.
     * @param response - HTTP-ответ клиенту.
     * @param filterChain - цепочка фильтров.
     *
     * @throws ServletException если возникает ошибка обработки запроса.
     * @throws IOException если происходит ошибка ввода-вывода.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // Извлекаем заголовок Authorization из запроса
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;
        // Проверяем, что заголовок не пуст и начинается с "Bearer "
        if (StringUtils.isEmpty(authHeader) ||
            !org.springframework.util.StringUtils.startsWithIgnoreCase(authHeader, "Bearer ")) {
            doFilter(request, response, filterChain);
            return;
        }
        // Извлекаем JWT из заголовка, убирая "Bearer "(7 символов)
        jwt = authHeader.substring(7);
        // Извлекаем email пользователя из JWT
        userEmail = jwtService.extractUsername(jwt);

        // Если email не пуст и аутентификация еще не выполнена
        if (StringUtils.isNotEmpty(userEmail) && SecurityContextHolder.getContext().getAuthentication() == null) {
            // Загружаем детали пользователя на основе email
            UserDetails userDetails = userService.userDetailsService().loadUserByUsername(userEmail);
            // Проверяем, действителен ли токен(равенство email-ов и не просрочен) для загруженных данных пользователя
            if (jwtService.isTokenValid(jwt, userDetails)) {
                SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
                // Создаем объект аутентификации с деталями пользователя и его правами доступа
                UsernamePasswordAuthenticationToken token =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                // Устанавливаем детали аутентификации из запроса
                token.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                // Устанавливаем аутентификацию в контекст безопасности
                securityContext.setAuthentication(token);
                SecurityContextHolder.setContext(securityContext);
            }
        }
        filterChain.doFilter(request, response);
    }
}
