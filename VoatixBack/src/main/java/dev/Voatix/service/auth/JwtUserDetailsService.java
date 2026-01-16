package dev.Voatix.service.auth;


import dev.Voatix.dto.auth.AuthUserDTO;
import dev.Voatix.entity.CredentialsEntity;
import dev.Voatix.entity.UserEntity;
import dev.Voatix.repositories.CredentialsRepository;
import dev.Voatix.repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@AllArgsConstructor
public class JwtUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final CredentialsRepository credentialsRepository;

    @Override
    //Метод по поиску пользователя по username и возвращению егов виде объекта AuthUser, который поддерживает интерфейс UserDetails
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        UserEntity user = userRepository.findByNickname(username)
                .orElseThrow(
                () -> new UsernameNotFoundException(String.format("User %s not found", username))
        );
        CredentialsEntity credentials = credentialsRepository.findById(user.getId())
                .orElseThrow(
                        () -> new UsernameNotFoundException(String.format("User`s credentials %s not found", username))
                );
        //вот тут прикол был с прокчи затычкой - сущности password


        return AuthUserDTO.builder()
                .username(user.getNickname())
                .password(credentials.getPassword().getPassword())
                .authorities(List.of(new SimpleGrantedAuthority(credentials.getRole().name())))
                .build();
    }
}
