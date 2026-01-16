package dev.Voatix.service.auth;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@AllArgsConstructor
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUserDetailsService userDetailsService;
    private final TokenManager tokenManager;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain
    ) throws ServletException, IOException {
        //Вот тут кароч JwtFilter просматривает тупо все пути, а не только для авторизованные
        String requestURI = request.getRequestURI();
        if (requestURI.startsWith("/api/login") ||
                requestURI.startsWith("/swagger-ui") ||  // Убрал /**
                requestURI.startsWith("/v3/api-docs") || // Убрал /**
                requestURI.startsWith("/webjars") // Добавил webjars
        ) {
            log.info(request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }
        String tokenHeader = request.getHeader("Authorization");
        String username = null;
        String token = null;
            // if bearer token is provided, get the username
        if (tokenHeader != null && tokenHeader.startsWith("Bearer ")) {
            token = tokenHeader.substring(7);
            try {
                username = tokenManager.getUsernameFromToken(token);
            }
            catch (IllegalArgumentException e) {
                log.error("Unable to get JWT Token {}", e.getMessage());
            }
            catch (ExpiredJwtException e) {
                log.error("JWT Token has expired", e.getMessage());
            }
        }
        else {
            log.debug("Bearer String not found in token");
        }

        // validate the JWT Token and create a new authentication token and set in security context
        if (username != null  && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            if (tokenManager.validateJwtToken(token, userDetails)) {
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                );
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }
        filterChain.doFilter(request, response);
    }
}
