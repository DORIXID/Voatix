package dev.Voatix.controllers;


import dev.Voatix.configuration.security.CustomUserDetails;
import dev.Voatix.dto.comment.*;
import dev.Voatix.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/comments")
public class CommentsController {

    final private CommentService commentsService;

    @GetMapping("")
    public Page<CommentWithStatsDTO> getComments(
            @Valid @RequestBody CommentSearchDTO dto,
            Authentication auth){
        Long userId = getUserId(auth);
        return commentsService.getComments(dto, userId);
    }

    @PostMapping("new")
    public void createComment(
            @Valid @RequestBody() CommentDTO dto,
            Authentication auth
    ){
        Long userId = getUserId(auth);
        commentsService.createComment(dto, userId);
    }

    @DeleteMapping("")
    public void deleteComment(
            @Valid @RequestBody CommentDeleteDTO dto,
            Authentication auth
    ){
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ADMIN"));
        Long userId = getUserId(auth);
        commentsService.deleteComment(dto.getId(), isAdmin, userId);
    }

    @PatchMapping("")
    public void updateComment(
            @Valid @RequestBody CommentUpdateDTO dto,
            Authentication auth
    ){
        Long userId = getUserId(auth);
        commentsService.updateComment(dto, userId);
    }

    @PutMapping("like")
    public void upsertLike(
            @Valid @RequestBody CommentLikeDTO dto,
            Authentication auth) {
        Long userId = getUserId(auth);
        commentsService.upsertLike(dto, userId);
    }

    //todo Мб сделать костом аннотацию
    Long getUserId(Authentication auth) {
        CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();
        return user.getId();
    }
}
