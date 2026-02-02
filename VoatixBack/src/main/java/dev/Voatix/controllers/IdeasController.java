package dev.Voatix.controllers;

import dev.Voatix.dto.*;
import dev.Voatix.entity.enums.IdeaStatusEnum;
import dev.Voatix.service.FileService;
import dev.Voatix.service.IdeaService;
import dev.Voatix.service.ProjectService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("api/ideas")
public class IdeasController {

    @Autowired
    private IdeaService ideaService;


    @GetMapping("")
    public Page<IdeaWithStatsDTO> getIdeas(
            @RequestParam(required = true) String project,
            @RequestParam() Integer page,
            @RequestParam(defaultValue = "12") Integer limit,
            @RequestParam(defaultValue = "", required = false) String filterBy,
            @RequestParam(required = false) String searchedValue,
            Principal principal) {
        log.info("n\\n\n\n\n\n\n\n\n\n\\n\n\n\n\n\n\\n\n\n\n\n\n\n\\n");
        return ideaService.getIdeas(
                project,
                page,
                limit,
                filterBy,
                searchedValue,
                principal);
    }

    //http://localhost:8080/api/base/idea/1/likes?like=1
    @PutMapping("{ideaId}/likes")
    public ResponseEntity<Void> upsertIdeaLike(
            @PathVariable() Long ideaId,
            @RequestParam() Long like,
            Principal principal) {
        log.info("\nlike = {}\n", like);
        ideaService.upsertLike(ideaId, like, principal);
        return ResponseEntity.ok().build();
    }

    //http://localhost:8080/api/base/idea/1
    @GetMapping("{ideaId}")
    public IdeaWithStatsDTO getIdea(
            @PathVariable() Long ideaId,
            Principal principal) {
        return ideaService.getIdea(ideaId, principal);
    }

    @PostMapping("")
    public ResponseEntity<Void> createIdea(
            @Valid @RequestBody() IdeaCreateDTO dto,
            Principal principal
            ){
        ideaService.createIdea(dto, principal);
        return ResponseEntity.ok().build();
    }

    //todo: по возвращаемому значению разобраться что лучше
    @PatchMapping("{ideaId}")
    public ResponseEntity<Void> updateIdea(
            @RequestBody() IdeaUpdateDTO ideaDTO,
            @Valid @PathVariable() Long ideaId,
            Principal principal
    ){
        ideaService.updateIdea(ideaDTO, ideaId, principal);
        return ResponseEntity.ok().build();
    }

    //Project owner/manager operations

    @PatchMapping("{ideaId}/status")
    public ResponseEntity<Void> updateIdea(
            @PathVariable() Long ideaId,
            @Valid @RequestBody() IdeaStatusDTO dto,
            Principal principal
            ){
        ideaService.updateStatus(ideaId, dto, principal);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("{ideaId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable() Long ideaId,
            Principal principal
    ){
        ideaService.deleteIdea(ideaId, principal);
        return ResponseEntity.ok().build();
    }

}
