package dev.Voatix.mapper;


import dev.Voatix.dto.project.ProjectCreateDTO;
import dev.Voatix.dto.project.ProjectOfUserDTO;
import dev.Voatix.dto.project.ProjectProfileDTO;
import dev.Voatix.dto.project.ProjectOfUserProjection;
import dev.Voatix.dto.project.ProjectProfileWithoutModeratorsProjection;
import dev.Voatix.dto.user.UserModeratorProjection;
import dev.Voatix.entity.ProjectEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;


@Mapper(componentModel = "spring")
public interface ProjectMapper {


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", expression = "java(true)")
    @Mapping(target = "title", source = "dto.title")
    @Mapping(target = "avatarId", source = "avatarId")
    ProjectEntity toEntity(ProjectCreateDTO dto, Long avatarId);

    ProjectOfUserDTO toDto(ProjectOfUserProjection project);

    List<ProjectOfUserDTO> toDto(List<ProjectOfUserProjection> projection);

    @Mapping(target = "fileId", source = "avatar.id")
    ProjectOfUserDTO toDto(ProjectEntity project);

    @Mapping(target = "title", source = "project.title")
    @Mapping(target = "fileId", source = "project.fileId")
    @Mapping(target = "moderators", source = "moderators")
    ProjectProfileDTO toDto(ProjectProfileWithoutModeratorsProjection project, List<UserModeratorProjection> moderators);
}
