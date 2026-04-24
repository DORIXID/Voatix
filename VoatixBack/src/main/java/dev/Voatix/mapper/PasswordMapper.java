package dev.Voatix.mapper;


import dev.Voatix.dto.user.UpdateUserCredentialsPasswordDTO;
import dev.Voatix.dto.user.UserCredentialsPasswordDTO;
import dev.Voatix.entity.PasswordEntity;
import dev.Voatix.utils.PasswordEncoding;
import lombok.RequiredArgsConstructor;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring")
public abstract class PasswordMapper {

    @Autowired
    protected PasswordEncoding encoder;


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", expression = "java(encoder.encode(dto.getPassword()))")
    @Mapping(target = "dateOfChange", expression = "java(java.time.LocalDateTime.now())")
    public abstract PasswordEntity toEntity(UserCredentialsPasswordDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", expression = "java(encodeIfExists(dto, password))")
    @Mapping(target = "dateOfChange", expression = "java(updateTimestamp(dto, password))")
    public abstract void updateEntity(UpdateUserCredentialsPasswordDTO dto,@MappingTarget PasswordEntity password);

    protected String encodeIfExists(UpdateUserCredentialsPasswordDTO dto, PasswordEntity password) {
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            return encoder.encode(dto.getPassword());
        }
        // Если пароля в DTO нет, возвращаем то, что уже лежит в базе
        return password.getPassword();
    }

    //Это проверка меняется ли в этом апдейте пароль. Если да - ставим дату изменения пароля - now
    protected LocalDateTime updateTimestamp(UpdateUserCredentialsPasswordDTO dto, PasswordEntity password) {
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            return LocalDateTime.now();
        }
        // Если пароль не меняем, возвращаем старую дату из базы
        return password.getDateOfChange();
    }
}
