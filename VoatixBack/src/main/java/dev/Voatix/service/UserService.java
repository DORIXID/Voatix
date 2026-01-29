package dev.Voatix.service;

import dev.Voatix.dto.UserCredentialsPasswordDTO;
import dev.Voatix.mapper.CredentialsMapper;
import dev.Voatix.repositories.CredentialsRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final CredentialsRepository credentialsRepository;
    private final CredentialsMapper authMapper;

    public void createUser(UserCredentialsPasswordDTO dto) {
        credentialsRepository.save(authMapper.toCredentialsEntity(dto));
    }
}
