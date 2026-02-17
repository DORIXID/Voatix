package dev.Voatix.mapper;


import dev.Voatix.dto.UpdateUserCredentialsPasswordDTO;
import dev.Voatix.dto.UserCredentialsPasswordDTO;
import dev.Voatix.entity.UserEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "avatar", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    UserEntity toEntity(UserCredentialsPasswordDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "avatar", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(UpdateUserCredentialsPasswordDTO dto,@MappingTarget UserEntity userEntity);
}
