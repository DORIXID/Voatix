package dev.Voatix.mapper;


import dev.Voatix.dto.ProjectOfUserDTO;
import dev.Voatix.entity.ProjectEntity;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.List;


@Mapper(componentModel = "spring")
public abstract class ProjectMapper {

    public abstract ProjectOfUserDTO toDto(ProjectEntity project);

    public abstract List<ProjectOfUserDTO> toDto(List<ProjectEntity> projectEntities);

}
