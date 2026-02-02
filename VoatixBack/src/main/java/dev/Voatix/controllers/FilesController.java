package dev.Voatix.controllers;


import dev.Voatix.dto.FileResponseDTO;
import dev.Voatix.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

@Slf4j
@RestController
@RequestMapping("api/files")
public class FilesController {

    @Autowired
    private FileService fileService;

    @PostMapping("upload")
    public ResponseEntity<FileResponseDTO> upload(
            @RequestPart("file") MultipartFile file,
            Principal principal)
    {
        return ResponseEntity.ok(fileService.upload(file, principal));
    }

    @GetMapping("/{key}/view")
    public ResponseEntity<Resource> getFile(@PathVariable String key) {
        log.info("\n\n"+ key +"\n\n");
        return fileService.download(key);
    }
}
