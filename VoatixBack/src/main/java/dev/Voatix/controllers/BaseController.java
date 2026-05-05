package dev.Voatix.controllers;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;

public abstract class CustomController {

    protected Long getUserId(Authentication auth) {
        Jwt jwt = (Jwt) auth.getPrincipal();
        return jwt.getClaim("userId");
    }

}
