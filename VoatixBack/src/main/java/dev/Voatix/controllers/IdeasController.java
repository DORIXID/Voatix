package dev.Voatix.controllers;

import dev.Voatix.configuration.security.CustomUserDetails;
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
public class IdeasController {

    final private IdeaService ideaService;

    @GetMapping("")
    public Page<IdeaWithStatsDTO> getIdeas(
            @Valid @RequestBody IdeaSearchDTO dto,
            Authentication auth) {
        return ideaService.getIdeas(dto, getUserId(auth));
    }

    @PutMapping("/like")
    public void upsertLike(
            @Valid @RequestBody IdeaLikeDTO dto,
            Authentication auth) {
        ideaService.upsertLike(dto, getUserId(auth));
    }

    @GetMapping("")
    public IdeaWithStatsDTO getIdea(
            @Valid @RequestBody IdeaSingleRequestDTO dto,
            Authentication auth) {
        return ideaService.getIdea(dto.getIdeaId(), getUserId(auth));
    }

    @PostMapping("")
    public void createIdea(
            @Valid @RequestBody() IdeaCreateDTO dto,
            Authentication auth
            ){
        ideaService.createIdea(dto, getUserId(auth));
    }

    @PatchMapping("")
    public void updateIdea(
            @RequestBody() IdeaUpdateDTO ideaDTO,
            Authentication auth
    ){
        ideaService.updateIdea(ideaDTO, getUserId(auth));
    }

    //Project owner/manager operations

    @PatchMapping("status")
    public void updateIdea(
            @Valid @RequestBody() IdeaStatusDTO dto,
            Authentication auth
            ){
        ideaService.updateStatus(dto, getUserId(auth));
    }

    @DeleteMapping("")
    public void deleteComment(
            @Valid @RequestBody IdeaDeleteDTO dto,
            Authentication auth
    ){
        ideaService.deleteIdea(dto.getIdeaId(), getUserId(auth));
    }

    Long getUserId(Authentication auth) {
        CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();
        return user.getId();
    }

}
