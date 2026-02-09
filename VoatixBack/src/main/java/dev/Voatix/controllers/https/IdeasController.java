package dev.Voatix.controllers.https;

import dev.Voatix.dto.*;
import dev.Voatix.service.IdeaService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

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
    public void upsertLike(
            @PathVariable("ideaId") Long ideaId,
            @RequestParam() Long like,
            Principal principal) {
        log.info("\nlike = {}\n", like);
        ideaService.upsertLike(ideaId, like, principal);
    }

    //http://localhost:8080/api/base/idea/1
    @GetMapping("{ideaId}")
    public IdeaWithStatsDTO getIdea(
            @PathVariable() Long ideaId,
            Principal principal) {
        return ideaService.getIdea(ideaId, principal);
    }

    @PostMapping("")
    public void createIdea(
            @Valid @RequestBody() IdeaCreateDTO dto,
            Principal principal
            ){
        ideaService.createIdea(dto, principal);
    }

    //todo: по возвращаемому значению разобраться что лучше
    @PatchMapping("{ideaId}")
    public void updateIdea(
            @RequestBody() IdeaUpdateDTO ideaDTO,
            @Valid @PathVariable() Long ideaId,
            Principal principal
    ){
        ideaService.updateIdea(ideaDTO, ideaId, principal);
    }

    //Project owner/manager operations

    @PatchMapping("{ideaId}/status")
    public void updateIdea(
            @PathVariable() Long ideaId,
            @Valid @RequestBody() IdeaStatusDTO dto,
            Principal principal
            ){
        ideaService.updateStatus(ideaId, dto, principal);
    }

    @DeleteMapping("{ideaId}")
    public void deleteComment(
            @PathVariable() Long ideaId,
            Principal principal
    ){
        ideaService.deleteIdea(ideaId, principal);
    }

}
