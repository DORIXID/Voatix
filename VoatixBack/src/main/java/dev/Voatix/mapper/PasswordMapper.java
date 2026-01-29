package dev.Voatix.mapper;


import dev.Voatix.dto.UserCredentialsPasswordDTO;
import dev.Voatix.entity.PasswordEntity;
import dev.Voatix.utils.PasswordEncoding;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class PasswordMapper {

    @Autowired
    PasswordEncoding encoder;

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", expression = "java(encoder.encode(dto.getPassword()))")
    @Mapping(target = "dateOfChange", expression = "java(java.time.LocalDateTime.now())")
    public abstract PasswordEntity toEntity(UserCredentialsPasswordDTO dto);

}
