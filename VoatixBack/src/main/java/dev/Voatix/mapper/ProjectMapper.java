package dev.Voatix.mapper;


import dev.Voatix.dto.ProjectOfUserDTO;
import dev.Voatix.dto.projection.ProjectOfUserProjection;
import dev.Voatix.entity.ProjectEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.List;


@Mapper(componentModel = "spring")
public interface ProjectMapper {


    @Mapping(target = "key", source = "key")
    ProjectOfUserDTO toDto(ProjectOfUserProjection project);

    List<ProjectOfUserDTO> toDto(List<ProjectOfUserProjection> projection);

}
