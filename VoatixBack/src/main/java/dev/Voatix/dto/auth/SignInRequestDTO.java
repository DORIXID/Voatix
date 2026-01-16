package dev.Voatix.dto.auth;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SignInRequestDTO {

    private String username;
    private String pwd;

}
