package dev.Voatix.mapper;

import dev.Voatix.entity.ModeratorEntity;
import dev.Voatix.entity.ProjectEntity;
import dev.Voatix.entity.UserEntity;
import dev.Voatix.entity.enums.RoleOfProjectManager;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ModeratorMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", source = "user")
    @Mapping(target = "project", source = "project")
    @Mapping(target = "role", source = "role")
    ModeratorEntity toEntity(UserEntity user, ProjectEntity project, RoleOfProjectManager role);
}
