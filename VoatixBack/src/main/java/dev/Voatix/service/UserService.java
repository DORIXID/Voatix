package dev.Voatix.service;

import dev.Voatix.dto.UserCredentialsPasswordDTO;
import dev.Voatix.mapper.CredentialsMapper;
import dev.Voatix.repositories.CredentialsRepository;
import dev.Voatix.repositories.UserRepository;
import dev.Voatix.utils.exceptions.userException.UserAlreadyExistsException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final CredentialsRepository credentialsRepository;
    private final CredentialsMapper authMapper;
    private final UserRepository userRepository;

    public void createUser(UserCredentialsPasswordDTO dto) {
        Optional<Long> userId = userRepository.getIdByNickname(dto.getNickname());
        if (userId.isPresent()) {
            throw new UserAlreadyExistsException(dto.getNickname());
        }
        credentialsRepository.save(authMapper.toCredentialsEntity(dto));
    }
}
