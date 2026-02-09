package dev.Voatix.controllers.https;


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
    public void createComment(
            @Valid @RequestBody() CommentDTO dto,
            Principal principal
    ){
        commentsService.createComment(dto, principal);
    }

    @DeleteMapping("{commentId}")
    public void deleteComment(
            @PathVariable() Long commentId,
            Principal principal
    ){
        commentsService.deleteComment(commentId, principal);
    }

    @PatchMapping("{commentId}")
    public void updateComment(
            @RequestBody() CommentUpdateDTO dto,
            @Valid @PathVariable() Long commentId,
            Principal principal
    ){
        commentsService.updateComment(dto, commentId, principal);
    }

    @PutMapping("{commentId}/likes")
    public void upsertLike(
            @PathVariable("commentId") Long commentId,
            @RequestParam() Long like,
            Principal principal) {
        log.info("\nlike = {}\n", like);
        commentsService.upsertLike(commentId, like, principal);
    }
}
