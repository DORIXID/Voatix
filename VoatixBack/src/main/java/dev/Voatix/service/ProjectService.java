package dev.Voatix.service;

import dev.Voatix.dto.project.*;
import dev.Voatix.dto.user.UserModeratorProjection;
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
    private final ModeratorMapper moderatorMapper;

    public List<ProjectOfUserDTO> getProjectsOfUser(Long userId) {
        return projectMapper.toDto(projectRepository.findProjectsOfUserByUserId(userId));
    }

    public ProjectOfUserDTO getProject(Long id){
        ProjectEntity project = projectRepository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException(id));
        return projectMapper.toDto(project);
    }

    public ProjectProfileDTO getProjectProfile(Long id, Long userId) {
        if (!moderatorRepository.existsByUserIdAndProjectIdAndRole(userId, id, RoleOfProjectManager.OWNER)){
            throw new ModeratorAccessDeniedException(userId);
        }
        List<UserModeratorProjection> moderatorProjections = moderatorRepository.findManagersByProjectId(id);

        ProjectProfileWithoutModeratorsProjection projectProjection = projectRepository
                .findProjectProfileWithoutModeratorsProjection(id)
                .orElseThrow(() -> new ProjectNotFoundException(id));

        return projectMapper.toDto(projectProjection, moderatorProjections);
    }

    public void setAvatar(Long userId, ProjectAvatarUpdateDTO dto) {
        if (!moderatorRepository.existsByUserIdAndProjectIdAndRole(userId, dto.getProjectId(), RoleOfProjectManager.OWNER)){
            throw new ModeratorAccessDeniedException(userId);
        }
        projectRepository.setAvatar(dto.getProjectId(), dto.getFileId());
    }

    public void deleteModerator(Long userId, ProjectModeratorDTO dto){
        if (!moderatorRepository
                .existsByUserIdAndProjectIdAndRole(userId, dto.getProjectId(), RoleOfProjectManager.OWNER)){
            throw new ModeratorAccessDeniedException(userId);
        }
        moderatorRepository.deleteByUserIdAndProjectId(userId, dto.getProjectId());
    }

    public void addModerator(Long userId, ProjectModeratorDTO dto){
        if (!moderatorRepository
                .existsByUserIdAndProjectIdAndRole(userId, dto.getProjectId(), RoleOfProjectManager.OWNER)) {
            throw new ModeratorAccessDeniedException(userId);
        }
        UserEntity user = userRepository.getReferenceById(dto.getModeratorId());
        ProjectEntity project = projectRepository.getReferenceById(dto.getProjectId());
        moderatorRepository.save(moderatorMapper.toEntity(user, project, RoleOfProjectManager.MANAGER));
    }

    public void createProject(Long userId, ProjectCreateDTO dto){
        UserEntity user = userRepository.getReferenceById(userId);
        ProjectEntity project = projectMapper.toEntity(dto, dto.getFileId());
        projectRepository.save(project);

        ModeratorEntity moderator = moderatorMapper.toEntity(user, project, RoleOfProjectManager.OWNER);
        projectRepository.save(project);
        moderatorRepository.save(moderator);
    }

    public void deleteProject(Long userId, Long projectId){
        if (!moderatorRepository
                .existsByUserIdAndProjectIdAndRole(userId, projectId, RoleOfProjectManager.OWNER)) {
            throw new ModeratorAccessDeniedException(userId);
        }
        projectRepository.deleteById(projectId);
    }

}
