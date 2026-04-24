package dev.Voatix.service;

import dev.Voatix.dto.file.FileRequestDTO;
import dev.Voatix.dto.file.FileResponseDTO;
import dev.Voatix.entity.FileEntity;
import dev.Voatix.mapper.FileMapper;
import dev.Voatix.repositories.FileRepository;
import dev.Voatix.repositories.UserRepository;
import dev.Voatix.service.minio.MinioService;
import dev.Voatix.utils.exceptions.fileException.FileNotFoundException;
import dev.Voatix.utils.exceptions.fileException.FileProcessingException;
import dev.Voatix.utils.exceptions.fileException.InvalidFileTypeException;
import dev.Voatix.utils.exceptions.commonException.UserUnauthorizedException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class FileService {
    private final MinioService minioService;
    private final FileRepository fileRepository;
    private final FileMapper fileMapper;

    private final List<String> ALLOWED_TYPES = List.of("image/png", "image/jpeg", "image/jpg");

    public FileResponseDTO upload(MultipartFile file, Long userId) {
        validateFileType(file);

        try (InputStream inputStream = file.getInputStream()) {

            FileRequestDTO requestDto = new FileRequestDTO();
            requestDto.setName(file.getOriginalFilename());
            requestDto.setContentType(file.getContentType());
            requestDto.setBucket("images");

            FileEntity fileEntity = fileMapper.toEntity(requestDto, userId);
            FileEntity savedFile = fileRepository.save(fileEntity);
            FileResponseDTO responseDto = fileMapper.toDTO(savedFile);

            minioService.putObject("images", fileEntity.getId().toString(), inputStream, file.getContentType());

            return responseDto;

        } catch (IOException e) {
            throw new FileProcessingException(e.getMessage());
        }
    }

    public ResponseEntity<Resource> download(Long id) {
        FileEntity file = fileRepository.findById(id)
                .orElseThrow(() -> new FileNotFoundException(id));

        InputStream stream = minioService.getObject(file.getBucket(), file.getId().toString());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "image/png")
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getName() + "\"")
                .body(new InputStreamResource(stream));
    }

    private void validateFileType(MultipartFile file) {
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new InvalidFileTypeException(file.getContentType());
        }
    }

    public void delete(Long id) {
        FileEntity file = fileRepository.findById(id)
                .orElseThrow(() -> new FileNotFoundException(id));

        fileRepository.delete(file);

        try {
            minioService.removeObject(file.getBucket(), file.getId().toString());
        } catch (Exception e) {
            throw new FileProcessingException(file.getName());
        }
    }
}
