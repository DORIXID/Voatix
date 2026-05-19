package dev.Voatix.controllers;

import dev.Voatix.controllers.other.BaseController;
import dev.Voatix.dto.idea.*;
import dev.Voatix.service.IdeaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/ideas")
public class IdeasController extends BaseController {

    final private IdeaService ideaService;

    @PostMapping("get")
    public Page<IdeaWithStatsDTO> getIdeas(
            @Valid @RequestBody IdeaSearchDTO dto,
            Authentication auth) {
        return ideaService.getIdeas(dto, getUserId(auth));
    }

    @PutMapping("like")
    public void upsertLike(
            @Valid @RequestBody IdeaLikeDTO dto,
            Authentication auth) {
        ideaService.upsertLike(dto, getUserId(auth));
    }

    @GetMapping("{ideaId}")
    public IdeaWithStatsDTO getIdea(
            @PathVariable Long ideaId,
            Authentication auth) {
        return ideaService.getIdea(ideaId, getUserId(auth));
    }

    @PostMapping("")
    public void createIdea(
            @Valid @RequestBody() IdeaCreateDTO dto,
            Authentication auth
    ) {
        ideaService.createIdea(dto, getUserId(auth));
    }

    @PatchMapping("")
    public void updateIdea(
            @RequestBody() IdeaUpdateDTO ideaDTO,
            Authentication auth
    ) {
        ideaService.updateIdea(ideaDTO, getUserId(auth));
    }

    //Project owner/manager operations

    @PatchMapping("status")
    public void updateIdea(
            @Valid @RequestBody() IdeaStatusDTO dto,
            Authentication auth
    ) {
        ideaService.updateStatus(dto, getUserId(auth));
    }

    @DeleteMapping("")
    public void deleteComment(
            @Valid @RequestBody IdeaDeleteDTO dto,
            Authentication auth
    ) {
        ideaService.deleteIdea(dto.getIdeaId(), getUserId(auth));
    }

}
