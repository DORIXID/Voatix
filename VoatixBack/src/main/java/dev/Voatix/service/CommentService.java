package dev.Voatix.service;

import dev.Voatix.dto.CommentDTO;
import dev.Voatix.dto.CommentUpdateDTO;
import dev.Voatix.dto.CommentWithStatsDTO;
import dev.Voatix.dto.projection.CommentStatsProjection;
import dev.Voatix.dto.projection.CommentFileKeyProjection;
import dev.Voatix.entity.*;
import dev.Voatix.entity.enums.RoleOfUserEnum;
import dev.Voatix.mapper.CommentMapper;
import dev.Voatix.mapper.CommentRatingMapper;
import dev.Voatix.repositories.*;
import dev.Voatix.utils.exceptions.commentException.CommentAuthorNotFoundException;
import dev.Voatix.utils.exceptions.commentException.CommentNotFoundException;
import dev.Voatix.utils.exceptions.commentException.UserOfCommentNotFoundException;
import dev.Voatix.utils.exceptions.commonException.AccessDeniedException;
import dev.Voatix.utils.exceptions.commonException.RoleOfUserNotFoundException;
import dev.Voatix.utils.exceptions.commonException.UserUnauthorizedException;
import dev.Voatix.utils.exceptions.fileException.FileOwnershipException;
import dev.Voatix.utils.exceptions.ideaException.IdeaNotFoundException;
import dev.Voatix.utils.exceptions.ideaException.ProjectOfIdeaNotFoundException;
import dev.Voatix.utils.exceptions.moderatorException.ModeratorAccessDeniedException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

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
    private final ProjectRepository projectRepository;
    private final CredentialsRepository credentialsRepository;

    public Page<CommentWithStatsDTO> getComments(Long ideaId, Integer page, Integer limit, Principal principal) {
        Pageable pageParam = PageRequest.of(page, limit);
        Long userId = userRepository.findIdByNickname(principal.getName())
                .orElseThrow(() -> new UserUnauthorizedException(principal.getName()));
        Long findIdeaId = ideaRepository.findIdById(ideaId)
                .orElseThrow(() -> new IdeaNotFoundException(ideaId));
        Page<CommentEntity> comment = commentRepository.findByIdea(ideaId, pageParam);

        List<Long> ids = comment.getContent().stream().map(CommentEntity::getId).toList();

        List<CommentFileKeyProjection> fileKeyProjs = commentRepository.findFilesByCommentIds(ids);
        List<CommentStatsProjection> commStatsProjs = commentRepository.getCommentsRaitingsStats(ids, userId);
        return commentMapper.toPageDto(comment, commStatsProjs, fileKeyProjs);
    }

    public void createComment(CommentDTO dto, Principal principal) {
        Long userId = userRepository.findIdByNickname(principal.getName())
                .orElseThrow(() -> new UserUnauthorizedException(principal.getName()));
        Long ideaId = ideaRepository.findIdById(dto.getIdeaId())
                .orElseThrow(() -> new IdeaNotFoundException(dto.getIdeaId()));
        if (fileRepository.existsByOwner(dto.getFileKeys(), userId)){
            throw new FileOwnershipException();
        }
        List<FileEntity> files = fileRepository.findByFileKeys(dto.getFileKeys());
        commentRepository.save(commentMapper.toEntity(dto, userId, ideaId, files));
    }

    public void updateComment(CommentUpdateDTO dto, Long commentId, Principal principal) {
        Long userId = userRepository.findIdByNickname(principal.getName())
                .orElseThrow(() -> new UserUnauthorizedException(principal.getName()));
        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(commentId));
        Long authorId = commentRepository.findUserIdById(comment.getId())
                .orElseThrow(() -> new CommentAuthorNotFoundException(comment.getId()));
        if (!authorId.equals(userId)) {
            throw new AccessDeniedException("You are not author of this comment");
        }
        commentMapper.updateEntity(dto, comment);
    }

    public void upsertLike(Long commentId, Long like, Principal principal) {
        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new UserUnauthorizedException(principal.getName()));
        UserEntity user = userRepository.findByNickname(principal.getName())
                .orElseThrow(() -> new UserUnauthorizedException(principal.getName()));
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
        Long userId = userRepository.findIdByNickname(principal.getName())
                .orElseThrow(() -> new UserUnauthorizedException(principal.getName()));
        Long findCommentId = commentRepository.findIdById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(commentId));
        Long ideaId = commentRepository.findIdeaIdById(commentId)
                .orElseThrow(() -> new IdeaNotFoundException(commentId));
        Long projectId = ideaRepository.findProjectIdById(ideaId)
                .orElseThrow(() -> new ProjectOfIdeaNotFoundException(ideaId));
        Long userIdOfComment = commentRepository.findIdById(commentId)
                .orElseThrow(() -> new UserOfCommentNotFoundException(commentId));
        RoleOfUserEnum role = credentialsRepository.findRoleOfUserByUserId(userId)
                .orElseThrow(() -> new RoleOfUserNotFoundException(principal.getName()));
        if (!userIdOfComment.equals(userId) && !role.equals(RoleOfUserEnum.ADMIN)) {
            if(!moderatorRepository.existsByUserIdAndProjectId(userId, projectId)){
                throw new ModeratorAccessDeniedException(principal.getName());
            }
        }
        commentRatingRepository.deleteByCommentId(commentId);
        fileRepository.deleteByCommentId(commentId);
        commentRepository.deleteById(commentId);
    }


}
