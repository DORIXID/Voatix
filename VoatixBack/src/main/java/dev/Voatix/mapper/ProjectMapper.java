package dev.Voatix.mapper;


import dev.Voatix.dto.ProjectCreateDTO;
import dev.Voatix.dto.ProjectOfUserDTO;
import dev.Voatix.dto.ProjectProfileDTO;
import dev.Voatix.dto.projection.ProjectOfUserProjection;
import dev.Voatix.dto.projection.ProjectProfileWithoutModeratorsProjection;
import dev.Voatix.dto.projection.UserModeratorProjection;
import dev.Voatix.entity.ProjectEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.List;


@Mapper(componentModel = "spring")
public interface ProjectMapper {


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", expression = "java(true)")
    @Mapping(target = "title", source = "dto.title")
    @Mapping(target = "avatarId", source = "avatarId")
    ProjectEntity toEntity(ProjectCreateDTO dto, Long avatarId);

    @Mapping(target = "key", source = "key")
    ProjectOfUserDTO toDto(ProjectOfUserProjection project);

    List<ProjectOfUserDTO> toDto(List<ProjectOfUserProjection> projection);

    @Mapping(target = "key", source = "avatar.key")
    ProjectOfUserDTO toDto(ProjectEntity project);

    @Mapping(target = "title", source = "project.title")
    @Mapping(target = "key", source = "project.key")
    @Mapping(target = "moderators", source = "moderators")
    ProjectProfileDTO toDto(ProjectProfileWithoutModeratorsProjection project, List<UserModeratorProjection> moderators);
}
