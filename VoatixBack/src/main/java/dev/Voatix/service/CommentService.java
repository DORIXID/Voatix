package dev.Voatix.service;

import dev.Voatix.dto.CommentDTO;
import dev.Voatix.dto.CommentUpdateDTO;
import dev.Voatix.dto.CommentWithStatsDTO;
import dev.Voatix.dto.projection.CommentStatsProjection;
import dev.Voatix.entity.*;
import dev.Voatix.entity.enums.RoleOfUserEnum;
import dev.Voatix.mapper.CommentMapper;
import dev.Voatix.mapper.CommentRatingMapper;
import dev.Voatix.repositories.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final CommentMapper commentMapper;
    private final IdeaRepository ideaRepository;
    private final FileRepository fileRepository;
    private final ModeratorRepository moderatorRepository;
    private final CommentRatingRepository commentRatingRepository;
    private final CommentRatingMapper commentRaitingMapper;

    public Page<CommentWithStatsDTO> getComments(Long ideaId, Integer page, Integer limit, Principal principal) {
        Pageable pageParam = PageRequest.of(page, limit);
        UserEntity user = userRepository.findByNickname(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User \"" + principal.getName() + "\" not found"));
        Page<CommentEntity> comment = commentRepository.findByIdea(ideaId, pageParam);

        List<Long> ids = comment.getContent().stream().map(CommentEntity::getId).toList();

        List<CommentStatsProjection> commStatsProj = commentRepository.getCommentsRaitingsStats(ids, user.getId());
        return commentMapper.toPageDto(comment, commStatsProj);
    }

    public void createComment(CommentDTO dto, Principal principal) {
        UserEntity user = userRepository.findByNickname(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User \"" + principal.getName() + "\" not found"));
        IdeaEntity idea = ideaRepository.findById(dto.getIdeaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment with id = " + dto.getIdeaId() + " not found"));
        if (fileRepository.existsByOwner(dto.getFileKeys(), user.getId())){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied: Some files don't belong to you");
        }
        List<FileEntity> files = fileRepository.findByFileKeys(dto.getFileKeys());
        commentRepository.save(commentMapper.toEntity(dto, user, idea, files));
    }

    public void updateComment(CommentUpdateDTO dto, Long commentId, Principal principal) {
        UserEntity user = userRepository.findByNickname(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User \"" + principal.getName() + "\" not found"));
        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment " + commentId + " not found"));
        log.info("\n\n\n\n\n\n" + comment.getUser().getId() + " что в комменте и что в пользователе " + user.getId() + "\n\n\n\n\n\n");
        if (!comment.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User \"" + principal.getName() + "\" does not have access");
        }
        commentMapper.updateEntity(dto, comment);
    }

    public void upsertLike(Long commentId, Long like, Principal principal) {
        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));
        UserEntity user = userRepository.findByNickname(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User \"" + principal.getName() + "\" not found"));
        CommentRatingEntity commentRating = commentRatingRepository.findByUserIdAndCommentId(user.getId(), comment.getId())
                .orElse(null);
        if (like == 0L) {
            if (commentRating != null) {
                commentRatingRepository.delete(commentRating);
            }
        } else if (commentRating == null) {
            commentRatingRepository.save(commentRaitingMapper.toEntity(user, comment, like == 1L));
        } else {
            commentRaitingMapper.updateEntity(commentRating, like == 1L);
        }
    }

    public void deleteComment(Long commentId, Principal principal) {
        UserEntity user = userRepository.findByNickname(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User \"" + principal.getName() + "\" not found"));
        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment " + commentId + " not found"));
        ProjectEntity projectEntity = comment.getIdea().getProject();
        if (!comment.getUser().getId().equals(user.getId()) && !user.getCredentials().getRole().equals(RoleOfUserEnum.ADMIN)) {
            ModeratorEntity moderator = moderatorRepository.findByUserIdAndProjectId(user.getId(), projectEntity.getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "User \"" + principal.getName() + "\" does not have access"));
        }
        commentRepository.delete(comment);
    }


}
