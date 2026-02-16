package dev.Voatix.service;

import dev.Voatix.dto.FileRequestDTO;
import dev.Voatix.dto.FileResponseDTO;
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
    private final UserRepository userRepository;
    private final FileMapper fileMapper;

    private final List<String> ALLOWED_TYPES = List.of("image/png", "image/jpeg", "image/jpg");

    @Transactional
    public FileResponseDTO upload(MultipartFile file, Principal principal) {
        validateFileType(file);

        String storageKey = UUID.randomUUID() + "_" + file.getOriginalFilename();

        try (InputStream inputStream = file.getInputStream()) {
            minioService.putObject("images", storageKey, inputStream, file.getContentType());

            FileRequestDTO requestDto = new FileRequestDTO();
            requestDto.setName(file.getOriginalFilename());
            requestDto.setKey(storageKey);
            requestDto.setContentType(file.getContentType());
            requestDto.setBucket("images");

            Long uploaderId = userRepository.getIdByNickname(principal.getName())
                    .orElseThrow(() -> new UserUnauthorizedException(principal.getName()));
            FileEntity fileEntity = fileMapper.toEntity(requestDto, uploaderId);
            FileEntity savedFile = fileRepository.save(fileEntity);
            FileResponseDTO responseDto = fileMapper.toDTO(savedFile);
            log.info("File uploaded successfully" + responseDto.getName() + " "+ responseDto.getKey() + " "+ responseDto.getBucket());
            return responseDto;

        } catch (IOException e) {
            throw new FileProcessingException(e.getMessage());
        }
    }

    public ResponseEntity<Resource> download(String key) {
        FileEntity file = fileRepository.findByKey(key)
                .orElseThrow(() -> new FileNotFoundException(key));

        InputStream stream = minioService.getObject(file.getBucket(), file.getKey());
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

    public void delete(String key) {
        FileEntity file = fileRepository.findByKey(key)
                .orElseThrow(() -> new FileNotFoundException(key));

        fileRepository.delete(file);

        try {
            minioService.removeObject(file.getBucket(), file.getKey());
        } catch (Exception e) {
            throw new FileProcessingException(file.getName());
        }
    }
}
