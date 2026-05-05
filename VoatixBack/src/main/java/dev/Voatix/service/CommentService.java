package dev.Voatix.service;

import dev.Voatix.dto.comment.*;
import dev.Voatix.entity.*;
import dev.Voatix.mapper.CommentMapper;
import dev.Voatix.mapper.CommentRatingMapper;
import dev.Voatix.repositories.*;
import dev.Voatix.utils.exceptions.commentException.CommentNotFoundException;
import dev.Voatix.utils.exceptions.commonException.AccessDeniedException;
import dev.Voatix.utils.exceptions.fileException.FileOwnershipException;
import dev.Voatix.utils.exceptions.ideaException.IdeaNotFoundException;
import dev.Voatix.utils.exceptions.moderatorException.ModeratorAccessDeniedException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

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

    public Page<CommentWithStatsDTO> getComments(CommentSearchDTO dto, Long userId) {
        Pageable pageParam = PageRequest.of(dto.getPage(), dto.getLimit());

        Page<CommentEntity> comment = commentRepository.findByIdea(dto.getIdeaId(), pageParam);

        List<Long> ids = comment.getContent().stream().map(CommentEntity::getId).toList();

        List<CommentFileIdProjection> fileIdsProjs = commentRepository.findFilesByCommentIds(ids);
        List<CommentStatsProjection> commStatsProjs = commentRepository.getCommentsRaitingsStats(ids, userId);
        return commentMapper.toPageDto(comment, commStatsProjs, fileIdsProjs);
    }

    public void createComment(CommentDTO dto, Long userId) {
        Long ideaId = ideaRepository.findIdById(dto.getIdeaId())
                .orElseThrow(() -> new IdeaNotFoundException(dto.getIdeaId()));
        List<FileEntity> files = fileRepository.findById(dto.getFileIds());
        commentRepository.save(commentMapper.toEntity(dto, userId, ideaId, files));
    }

    public void updateComment(CommentUpdateDTO dto, Long userId) {
        if (!commentRepository.existsByIdAndUserId(dto.getCommentId(), userId)) {
            throw new AccessDeniedException("You are not author of this comment");
        }
        commentMapper.updateEntity(dto, commentRepository.getReferenceById(dto.getCommentId()));
    }

    public void upsertLike(CommentLikeDTO dto, Long userId) {
        commentRatingRepository.deleteByCommentIdAndUserId(dto.getCommentId(), userId);
        if (dto.getLike() != 0) {
            UserEntity user = userRepository.getReferenceById(userId);
            CommentEntity comment = commentRepository.getReferenceById(dto.getCommentId());
            commentRatingRepository.save(commentRaitingMapper
                    .toEntity(user, comment, userId, dto.getCommentId(), dto.getLike() == 1));
        }
    }

    public void deleteComment(Long commentId, boolean isAdmin, Long userId) {
        if (!commentRepository.existsByIdAndUserId(commentId, userId)
                && !isAdmin) {
            Long projectId = commentRepository.findProjectIdByCommentId(commentId)
                    .orElseThrow(() -> new CommentNotFoundException(commentId));
            if(!moderatorRepository.existsByUserIdAndProjectId(userId, projectId)){
                throw new ModeratorAccessDeniedException(userId);
            }
        }
        commentRepository.deleteById(commentId);
    }
}
