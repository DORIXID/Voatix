package dev.Voatix.service;

import dev.Voatix.dto.UpdateUserCredentialsPasswordDTO;
import dev.Voatix.dto.UserCredentialsPasswordDTO;
import dev.Voatix.dto.projection.ResponseUserProjection;
import dev.Voatix.entity.CredentialsEntity;
import dev.Voatix.entity.UserEntity;
import dev.Voatix.mapper.CredentialsMapper;
import dev.Voatix.repositories.CredentialsRepository;
import dev.Voatix.repositories.FileRepository;
import dev.Voatix.repositories.UserRepository;
import dev.Voatix.utils.exceptions.commonException.UserUnauthorizedException;
import dev.Voatix.utils.exceptions.fileException.FileNotFoundException;
import dev.Voatix.utils.exceptions.userException.UserAlreadyExistsException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final CredentialsRepository credentialsRepository;
    private final CredentialsMapper authMapper;
    private final UserRepository userRepository;
    private final CredentialsMapper credentialsMapper;
    private final FileRepository fileRepository;

    public void createUser(UserCredentialsPasswordDTO dto) {
        Optional<Long> userId = userRepository.findIdByNickname(dto.getNickname());
        if (userId.isPresent()) {
            throw new UserAlreadyExistsException(dto.getNickname());
        }
        credentialsRepository.save(authMapper.toCredentialsEntity(dto));
    }

    public void editUser(UpdateUserCredentialsPasswordDTO dto, Principal principal) {
        CredentialsEntity credentials = credentialsRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new UserUnauthorizedException(principal.getName()));
        credentialsMapper.updateCredentialsEntity(dto, credentials);
    }

    public ResponseUserProjection getMyProfile(Principal principal) {
        return userRepository.findUserInfoByNickname(principal.getName())
                .orElseThrow(() -> new UserUnauthorizedException(principal.getName()));
    }

    public void setAvatar(Principal principal, String key) {
        Long userId = userRepository.findIdByNickname(principal.getName())
                        .orElseThrow(() -> new UserUnauthorizedException(principal.getName()));
        Long fileId = fileRepository.findIdByKey(key)
                        .orElseThrow(() -> new FileNotFoundException(key));
        userRepository.setAvatar(userId, fileId);
    }
}
