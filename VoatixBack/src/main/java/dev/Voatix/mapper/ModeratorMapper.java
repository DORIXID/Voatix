package dev.Voatix.mapper;

import dev.Voatix.dto.ProjectOfUserDTO;
import dev.Voatix.entity.ModeratorEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public abstract class ModeratorMapper {

    public ProjectOfUserDTO toProjectOfUserDTO(ModeratorEntity moderator){
        if ( moderator == null ) {
            return null;
        }

        ProjectOfUserDTO.ProjectOfUserDTOBuilder projectOfUserDTO = ProjectOfUserDTO.builder();

        projectOfUserDTO.projectId( moderator.getProject().getId() );
        projectOfUserDTO.title( moderator.getProject().getTitle() );
        projectOfUserDTO.avatar_id( moderator.getProject().getAvatar() != null
                ? moderator.getProject().getAvatar().getId() : null);
        projectOfUserDTO.active( moderator.getProject().getActive() );
        projectOfUserDTO.roleOfUser( moderator.getRole() );
        return projectOfUserDTO.build();
    }

    public abstract List<ProjectOfUserDTO> toProjectOfUserDTO(List<ModeratorEntity> moderatorEntities);

}