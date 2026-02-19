package dev.Voatix.service;

import dev.Voatix.dto.ProjectCreateDTO;
import dev.Voatix.dto.ProjectOfUserDTO;
import dev.Voatix.dto.ProjectProfileDTO;
import dev.Voatix.dto.projection.ProjectProfileWithoutModeratorsProjection;
import dev.Voatix.dto.projection.UserModeratorProjection;
import dev.Voatix.entity.ModeratorEntity;
import dev.Voatix.entity.ProjectEntity;
import dev.Voatix.entity.UserEntity;
import dev.Voatix.entity.enums.RoleOfProjectManager;
import dev.Voatix.mapper.ModeratorMapper;
import dev.Voatix.mapper.ProjectMapper;
import dev.Voatix.repositories.FileRepository;
import dev.Voatix.repositories.ModeratorRepository;
import dev.Voatix.repositories.ProjectRepository;
import dev.Voatix.repositories.UserRepository;
import dev.Voatix.utils.exceptions.commonException.UserUnauthorizedException;
import dev.Voatix.utils.exceptions.fileException.FileNotFoundException;
import dev.Voatix.utils.exceptions.moderatorException.ModeratorAccessDeniedException;
import dev.Voatix.utils.exceptions.projectException.ProjectNotFoundException;
import dev.Voatix.utils.exceptions.userException.UserNotFoundException;
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
    private final ModeratorRepository moderatorRepository;
    private final FileRepository fileRepository;
    private final ModeratorMapper moderatorMapper;

    public List<ProjectOfUserDTO> getProjectsOfUser(Principal principal){
        Long userId = userRepository.findIdByNickname(principal.getName())
                .orElseThrow(() -> new UserUnauthorizedException(principal.getName()));
        return projectMapper.toDto(projectRepository.findProjectsOfUserByUserId(userId));
    }

    public ProjectOfUserDTO getProject(String title){
        ProjectEntity project = projectRepository.findByTitle(title)
                .orElseThrow(() -> new ProjectNotFoundException(title));
        return projectMapper.toDto(project);
    }

    public ProjectProfileDTO getProjectProfile(String title, Principal principal){
        if (!moderatorRepository.existsByUserNicknameAndProjectTitleAndRole(principal.getName(), title, RoleOfProjectManager.OWNER)){
            throw new ModeratorAccessDeniedException(principal.getName());
        }
        List<UserModeratorProjection> moderatorProjections = moderatorRepository.findManagersByProjectTitle(title);

        ProjectProfileWithoutModeratorsProjection projectProjection = projectRepository
                .findProjectProfileWithoutModeratorsProjection(title)
                .orElseThrow(() -> new ProjectNotFoundException(title));

        return projectMapper.toDto(projectProjection, moderatorProjections);
    }

    public void setAvatar(Principal principal, String title, String key) {
        if (!moderatorRepository.existsByUserNicknameAndProjectTitleAndRole(principal.getName(), title, RoleOfProjectManager.OWNER)){
            throw new ModeratorAccessDeniedException(principal.getName());
        }
        Long fileId = fileRepository.findIdByKey(key)
                .orElseThrow(() -> new FileNotFoundException(key));
        projectRepository.setAvatar(title, fileId);
    }

    public void deleteModerator(String title, String nickname, Principal principal){
        if (!moderatorRepository
                .existsByUserNicknameAndProjectTitleAndRole(principal.getName(), title, RoleOfProjectManager.OWNER)
                ||
           moderatorRepository
                .existsByUserNicknameAndProjectTitleAndRole(nickname, title, RoleOfProjectManager.OWNER)){
            throw new ModeratorAccessDeniedException(principal.getName());
        }
        moderatorRepository.deleteByNicknameAndTitle(nickname, title);
    }

    public void addModerator(String title, String nickname, Principal principal){
        if (!moderatorRepository
                .existsByUserNicknameAndProjectTitleAndRole(principal.getName(), title, RoleOfProjectManager.OWNER)
                ||
                moderatorRepository
                        .existsByUserNicknameAndProjectTitleAndRole(nickname, title, RoleOfProjectManager.OWNER)){
            throw new ModeratorAccessDeniedException(principal.getName());
        }
        UserEntity user = userRepository.findByNickname(nickname)
                .orElseThrow(() -> new UserNotFoundException(nickname));
        ProjectEntity project = projectRepository.findByTitle(title)
                .orElseThrow(() -> new ProjectNotFoundException(title));
        moderatorRepository.save(moderatorMapper.toEntity(user, project, RoleOfProjectManager.MANAGER));
    }

    public void createProject(ProjectCreateDTO dto, Principal principal){
        Long avatarId = fileRepository.findIdByKey(dto.getKey())
                .orElseThrow(() -> new FileNotFoundException(dto.getKey()));
        UserEntity user = userRepository.findByNickname(principal.getName())
                        .orElseThrow(() -> new UserNotFoundException(principal.getName()));
        ProjectEntity project = projectMapper.toEntity(dto, avatarId);
        projectRepository.save(project);

        ModeratorEntity moderator = moderatorMapper.toEntity(user, project, RoleOfProjectManager.OWNER);
        moderatorRepository.save(moderator);
    }

    public void deleteProject(String title, Principal principal){
        if (!moderatorRepository.existsByUserNicknameAndProjectTitleAndRole(principal.getName(), title, RoleOfProjectManager.OWNER)){
            throw new ModeratorAccessDeniedException(principal.getName());
        }
        ProjectEntity project = projectRepository.findByTitle(title)
                .orElseThrow(() -> new ProjectNotFoundException(title));
        projectRepository.delete(project);
    }

}
