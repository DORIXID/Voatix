package dev.Voatix.service;

import dev.Voatix.dto.CommentDTO;
import dev.Voatix.dto.CommentUpdateDTO;
import dev.Voatix.dto.CommentWithStatsDTO;
import dev.Voatix.dto.projection.CommentStatsProjection;
import dev.Voatix.entity.*;
import dev.Voatix.mapper.CommentMapper;
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
public class CommentsService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final CommentMapper commentMapper;
    private final IdeaRepository ideaRepository;
    private final FilesRepository filesRepository;
    private final ModeratorRepository moderatorRepository;

    public Page<CommentWithStatsDTO> getComments(Long ideaId, Integer page, Integer limit, Principal principal) {
        Pageable pageParam = PageRequest.of(page, limit);
        UserEntity userEntity = userRepository.findByNickname(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User \"" + principal.getName() + "\" not found"));
        Page<CommentEntity> comment = commentRepository.findByIdea(ideaId, pageParam);

        List<Long> ids = comment.getContent().stream().map(CommentEntity::getId).toList();

        List<CommentStatsProjection> commStatsProj = commentRepository.getCommentsRaitingsStats(ids, userEntity.getId());
        return commentMapper.toPageDto(comment, commStatsProj);
    }

    //todo: файлы добавить сюда и в идеи
    public void createComment(CommentDTO dto, Principal principal) {
        UserEntity userEntity = userRepository.findByNickname(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User \"" + principal.getName() + "\" not found"));
        IdeaEntity ideaEntity = ideaRepository.findById(dto.getIdeaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment with id = " + dto.getIdeaId() + " not found"));
        if (filesRepository.existsByOwner(dto.getFileKeys(), userEntity.getId())){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied: Some files don't belong to you");
        }
        List<FileEntity> files = filesRepository.findByFileKeys(dto.getFileKeys());
        commentRepository.save(commentMapper.toEntity(dto, userEntity, ideaEntity, files));
    }

    public void updateComment(CommentUpdateDTO dto, Long commentId, Principal principal) {
        UserEntity userEntity = userRepository.findByNickname(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User \"" + principal.getName() + "\" not found"));
        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment " + commentId + " not found"));
        if (!comment.getUser().getId().equals(userEntity.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User \"" + principal.getName() + "\" does not have access");
        }
        commentMapper.updateEntity(dto, comment);
    }

    public void deleteComment(Long commentId, Principal principal) {
        UserEntity userEntity = userRepository.findByNickname(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User \"" + principal.getName() + "\" not found"));
        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment " + commentId + " not found"));
        ProjectEntity projectEntity = comment.getIdea().getProject();
        if (!comment.getUser().getId().equals(userEntity.getId())) {
            ModeratorEntity moderator = moderatorRepository.findByUserIdAndProjectId(userEntity.getId(), projectEntity.getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "User \"" + principal.getName() + "\" does not have access"));
        }
        commentRepository.delete(comment);
    }


}
