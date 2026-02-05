package dev.Voatix.controllers;


import dev.Voatix.dto.CommentDTO;
import dev.Voatix.dto.CommentUpdateDTO;
import dev.Voatix.dto.CommentWithStatsDTO;
import dev.Voatix.service.CommentService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Slf4j
@RestController
@RequestMapping("api/comments")
public class CommentsController {

    @Autowired
    private CommentService commentsService;

    @GetMapping("")
    public Page<CommentWithStatsDTO> getComments(
            @RequestParam(required = true) Long ideaId,
            @RequestParam() Integer page,
            @RequestParam(defaultValue = "12") Integer limit,
            Principal principal){
        return commentsService.getComments(ideaId, page, limit, principal);
    }

    @PostMapping("new")
    public ResponseEntity<Void> createComment(
            @Valid @RequestBody() CommentDTO dto,
            Principal principal
    ){
        commentsService.createComment(dto, principal);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable() Long commentId,
            Principal principal
    ){
        commentsService.deleteComment(commentId, principal);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("{commentId}")
    public ResponseEntity<Void> updateComment(
            @RequestBody() CommentUpdateDTO dto,
            @Valid @PathVariable() Long commentId,
            Principal principal
    ){
        commentsService.updateComment(dto, commentId, principal);
        return ResponseEntity.ok().build();
    }

    @PutMapping("{commentId}/likes")
    public ResponseEntity<Void> upsertLike(
            @PathVariable("commentId") Long commentId,
            @RequestParam() Long like,
            Principal principal) {
        log.info("\nlike = {}\n", like);
        commentsService.upsertLike(commentId, like, principal);
        return ResponseEntity.ok().build();
    }
}
