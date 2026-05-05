package dev.Voatix.dto.auth;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

@Builder
@Setter @Getter
public class AuthUserDTO implements UserDetails {

    private Long id;
    private String password;
    private String username;

    private List<SimpleGrantedAuthority> authorities;
}