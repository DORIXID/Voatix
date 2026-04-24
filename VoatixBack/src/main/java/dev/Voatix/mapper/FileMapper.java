package dev.Voatix.mapper;

import dev.Voatix.dto.file.FileRequestDTO;
import dev.Voatix.dto.file.FileResponseDTO;
import dev.Voatix.entity.FileEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FileMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", source = "dto.name")
    @Mapping(target = "bucket", source = "dto.bucket")
    @Mapping(target = "uploaderId", source = "uploaderId")
    @Mapping(target = "contentType", source = "dto.contentType")
    FileEntity toEntity(FileRequestDTO dto, Long uploaderId);

    FileResponseDTO toDTO(FileEntity fileEntity);
}
