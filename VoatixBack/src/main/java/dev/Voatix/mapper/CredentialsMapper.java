package dev.Voatix.mapper;


import dev.Voatix.dto.UpdateUserCredentialsPasswordDTO;
import dev.Voatix.dto.UserCredentialsPasswordDTO;
import dev.Voatix.entity.CredentialsEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {UserMapper.class, PasswordMapper.class})
public interface CredentialsMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", expression = "java(true)")
    @Mapping(target = "role", expression = "java(dev.Voatix.entity.enums.RoleOfUserEnum.USER)")
    @Mapping(target = "user", source = "dto")
    @Mapping(target = "password", source = "dto")
    CredentialsEntity toCredentialsEntity(UserCredentialsPasswordDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "user", source = "dto")
    @Mapping(target = "password", source = "dto")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateCredentialsEntity(UpdateUserCredentialsPasswordDTO dto, @MappingTarget CredentialsEntity credentialsEntity);
}
