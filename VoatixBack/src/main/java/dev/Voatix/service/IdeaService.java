package dev.Voatix.service;

import dev.Voatix.dto.idea.*;
import dev.Voatix.dto.comment.CommentCountProjection;
import dev.Voatix.entity.*;
import dev.Voatix.entity.enums.IdeaStatusEnum;
import dev.Voatix.entity.enums.RoleOfUserEnum;
import dev.Voatix.mapper.IdeaMapper;
import dev.Voatix.mapper.VotingEstimateMapper;
import dev.Voatix.repositories.*;
import dev.Voatix.utils.exceptions.commonException.RoleOfUserNotFoundException;
import dev.Voatix.utils.exceptions.fileException.FilesNotFoundException;
import dev.Voatix.utils.exceptions.ideaException.CommentCountNotFoundException;
import dev.Voatix.utils.exceptions.ideaException.IdeaAccessDeniedException;
import dev.Voatix.utils.exceptions.commonException.UnknownStatusException;
import dev.Voatix.utils.exceptions.ideaException.VoteStatsNotFoundException;
import dev.Voatix.utils.exceptions.ideaException.IdeaNotFoundException;
import dev.Voatix.utils.exceptions.moderatorException.ModeratorAccessDeniedException;
import dev.Voatix.utils.exceptions.projectException.ProjectNotFoundException;
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
public class IdeaService {

    private final IdeaRepository ideaRepository;
    private final IdeaMapper ideaMapper;
    private final UserRepository userRepository;
    private final VotingEstimateRepository votingEstimateRepository;
    private final ModeratorRepository moderatorRepository;
    private final FileRepository fileRepository;
    private final VotingEstimateMapper votingEstimateMapper;
    private final CredentialsRepository credentialsRepository;

    public Page<IdeaWithStatsDTO> getIdeas(IdeaSearchDTO dto, Long userId) {
        IdeaStatusEnum status = null;
        if (!dto.getFilterBy().isBlank() && !dto.getFilterBy().equals("ALL")) {
            try {
                status = IdeaStatusEnum.valueOf(dto.getFilterBy());
            } catch (IllegalArgumentException e) {
                throw new UnknownStatusException(dto.getFilterBy());
            }
        }
        Pageable pageParam = PageRequest.of(dto.getPage(), dto.getLimit());
        Page<IdeaEntity> ideas = ideaRepository.findIdeas(dto.getProjectId(), status, dto.getSearch(), pageParam);

        List<Long> ids = ideas.getContent().stream().map(IdeaEntity::getId).toList();

        List<VoteStatsProjection> voteStatsProj = ideaRepository.getVoteStats(ids, userId);
        List<CommentCountProjection> commentsProj = ideaRepository.getCommentCounts(ids);
        List<IdeaFileIdProjection> filesProj = ideaRepository.findFilesByIdeaIds(ids);
        return ideaMapper.toPageDto(ideas, voteStatsProj, commentsProj, filesProj);
    }

    public IdeaWithStatsDTO getIdea(Long ideaId, Long userId) {
        IdeaEntity idea = ideaRepository.findIdeaById(ideaId)
                .orElseThrow(() -> new IdeaNotFoundException(ideaId));
        VoteStatsProjection voteStatsProjection = ideaRepository.getVoteStatsIdeaById(ideaId, userId)
                .orElseThrow(() -> new VoteStatsNotFoundException(ideaId));
        CommentCountProjection commentCountProjection = ideaRepository.getCommentCountByIdeaId(ideaId)
                .orElseThrow(() -> new CommentCountNotFoundException(ideaId));
        List<Long> files = ideaRepository.findFileIdsByIdeaId(ideaId);
        return ideaMapper.toStatsDto(idea, voteStatsProjection, commentCountProjection, files);
    }

    public void upsertLike(IdeaLikeDTO dto, Long userId) {
        votingEstimateRepository.deleteByIdeaIdAndUserId(dto.getIdeaId(), userId);
        if (dto.getLike() != 0) {
            UserEntity user = userRepository.getReferenceById(userId);
            IdeaEntity idea = ideaRepository.getReferenceById(dto.getIdeaId());
            votingEstimateRepository.save(votingEstimateMapper
                    .toEntity(user, idea, userId, dto.getIdeaId(), dto.getLike() == 1));
        }
    }

    public void createIdea(IdeaCreateDTO ideaDTO, Long userId) {
        List<FileEntity> files = fileRepository.findById(ideaDTO.getFileIds());
        if (files.size() != ideaDTO.getFileIds().size()) {
            throw new FilesNotFoundException();
        }
        IdeaEntity idea = ideaMapper.toEntity(ideaDTO, ideaDTO.getProjectId() , userId, files);
        ideaRepository.save(idea);
    }

    public void updateIdea(IdeaUpdateDTO dto, Long userId) {
        IdeaEntity idea = ideaRepository.findEntityById(dto.getIdeaId())
                .orElseThrow(() -> new IdeaNotFoundException(dto.getIdeaId()));
        if (idea.getUserId().equals(userId)) {
            ideaMapper.updateEntity(dto, idea);
        } else {
            throw new IdeaAccessDeniedException(userId);
        }
    }

    public void deleteIdea(Long ideaId, Long userId) {
        Long projectId = ideaRepository.findProjectIdById(ideaId)
                .orElseThrow(() -> new ProjectNotFoundException(ideaId));
        Long authorIdOfIdea = ideaRepository.findUserIdById(ideaId)
                .orElseThrow(() -> new IdeaNotFoundException(ideaId));
        RoleOfUserEnum role = credentialsRepository.findRoleOfUserByUserId(userId)
                .orElseThrow(() -> new RoleOfUserNotFoundException(userId));
        boolean isAuthor = authorIdOfIdea.equals(userId);
        if (!isAuthor && !role.equals(RoleOfUserEnum.ADMIN)
                && !moderatorRepository.existsByUserIdAndProjectId(userId, projectId)) {
                throw new ModeratorAccessDeniedException(userId);
        }

        ideaRepository.deleteByIdeaId(ideaId);
    }

    public void updateStatus(IdeaStatusDTO dto, Long userId) {
        IdeaEntity idea = ideaRepository.findEntityById(dto.getIdeaId())
                .orElseThrow(() -> new IdeaNotFoundException(dto.getIdeaId()));
        if(!moderatorRepository.existsByUserIdAndProjectId(userId, idea.getProjectId())){
            throw new ModeratorAccessDeniedException(userId);
        }
        ideaMapper.updateEntity(dto, idea);
        ideaRepository.save(idea);
    }
}
