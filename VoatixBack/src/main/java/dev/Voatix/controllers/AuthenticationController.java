package dev.Voatix.controllers;

import dev.Voatix.dto.auth.JwtRequestDTO;
import dev.Voatix.dto.auth.JwtResponseDTO;
import dev.Voatix.service.auth.JwtUserDetailsService;
import dev.Voatix.service.auth.TokenManager;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@AllArgsConstructor
public class AuthenticationController {

    private final JwtUserDetailsService userDetailsService;
    @Autowired
    AuthenticationManager authenticationManager;
    private final TokenManager tokenManager;

    @PostMapping("api/login/new")
    public JwtResponseDTO createToken(@RequestBody JwtRequestDTO request) throws Exception {
        log.info("BODY = " + request);
        log.info("\n\nПопытка создать токен аутентификации");
        try {
            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword());
            log.info("\n\nПытаемся аутентифицировать полученный токен");
            authenticationManager.authenticate(token);
            log.info("\n\nТокен создан");
        }
        catch (DisabledException e) {
            throw new Exception("USER_DISABLED", e);
        }
        catch (BadCredentialsException e) {
            throw new Exception("INVALID_CREDENTIALS", e);
        }
        catch (Exception e) {
            log.error(e.getMessage());
            throw new Exception(e);
        }

        final UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        log.info("\n\nпользователь найден"+userDetails.getUsername() + " " + userDetails.getPassword());
        return new JwtResponseDTO(tokenManager.generateJwtToken(userDetails));
    }
}
