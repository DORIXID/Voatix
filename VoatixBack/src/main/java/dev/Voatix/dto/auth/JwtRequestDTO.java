package dev.Voatix.dto.auth;

import lombok.Data;

@Data
public class JwtRequestDTO {

    private String username;
    private String password;

}
