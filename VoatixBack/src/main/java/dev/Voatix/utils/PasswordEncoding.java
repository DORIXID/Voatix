package dev.Voatix.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PasswordEncoding {
    private final org.springframework.security.crypto.password.PasswordEncoder encoder;

    public String encode(String password) {
        return password.isBlank() ? null : (encoder.encode(password));
    }
}
