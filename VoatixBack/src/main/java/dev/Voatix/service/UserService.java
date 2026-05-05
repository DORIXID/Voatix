package dev.Voatix.service;

import dev.Voatix.dto.user.UpdateUserCredentialsPasswordDTO;
import dev.Voatix.dto.user.UserCredentialsPasswordDTO;
import dev.Voatix.dto.user.ResponseUserProfileProjection;
import dev.Voatix.entity.CredentialsEntity;
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

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final CredentialsRepository credentialsRepository;
    private final CredentialsMapper authMapper;
    private final UserRepository userRepository;
    private final CredentialsMapper credentialsMapper;

    public void createUser(UserCredentialsPasswordDTO dto) {
        if(userRepository.existsByNickname(dto.getNickname())) {
                throw new UserAlreadyExistsException(dto.getNickname());
        }
        credentialsRepository.save(authMapper.toCredentialsEntity(dto));
    }

    public void editUser(UpdateUserCredentialsPasswordDTO dto, Long userId) {
        if(userRepository.existsByNickname(dto.getNickname())) {
            throw new UserAlreadyExistsException(dto.getNickname());
        }
        CredentialsEntity credentials = credentialsRepository.findByUserId(userId)
                .orElseThrow(() -> new UserUnauthorizedException(userId));
        credentialsMapper.updateCredentialsEntity(dto, credentials);
    }

    public ResponseUserProfileProjection getMyProfile(Long userId) {
        return userRepository.findUserInfoById(userId)
                .orElseThrow(() -> new UserUnauthorizedException(userId));
    }

    public void setAvatar(Long fileId, Long userId) {
        userRepository.setAvatar(userId, fileId);
    }
}
