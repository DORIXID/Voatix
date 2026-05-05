package dev.Voatix.controllers;


import dev.Voatix.dto.file.FileResponseDTO;
import dev.Voatix.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/files")
public class FilesController extends BaseController {

    final private FileService fileService;

    @PostMapping("upload")
    public FileResponseDTO upload(
            @RequestPart("file") MultipartFile file,
            Authentication auth)
    {
        return fileService.upload(file, getUserId(auth));
    }

    @GetMapping("{id}/view")
    public ResponseEntity<Resource> getFile(@PathVariable Long id) {
        return fileService.download(id);
    }

    @DeleteMapping("{id}")
    public void deleteFile(@PathVariable Long id) {
        fileService.delete(id);
    }
}
