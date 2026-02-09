package dev.Voatix.service;

import dev.Voatix.dto.ProjectOfUserDTO;
import dev.Voatix.mapper.ProjectMapper;
import dev.Voatix.repositories.ProjectRepository;
import dev.Voatix.repositories.UserRepository;
import dev.Voatix.utils.exceptions.commonException.UserUnauthorizedException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;
    private final UserRepository userRepository;

    public List<ProjectOfUserDTO> getProjectsOfUser(Principal principal){
        Long userId = userRepository.getIdByNickname(principal.getName())
                .orElseThrow(() -> new UserUnauthorizedException(principal.getName()));
        return projectMapper.toDto(projectRepository.findProjectsOfUserByUserId(userId));
    }

}
