package dev.Voatix.mapper;

import dev.Voatix.dto.FileRequestDTO;
import dev.Voatix.dto.FileResponseDTO;
import dev.Voatix.entity.FileEntity;
import dev.Voatix.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FileMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", source = "dto.name")
    @Mapping(target = "key", source = "dto.key")
    @Mapping(target = "bucket", source = "dto.bucket")
    @Mapping(target = "uploaderId", source = "uploaderId")
    @Mapping(target = "contentType", source = "dto.contentType")
    FileEntity toEntity(FileRequestDTO dto, Long uploaderId);

    FileResponseDTO toDTO(FileEntity fileEntity);
}
