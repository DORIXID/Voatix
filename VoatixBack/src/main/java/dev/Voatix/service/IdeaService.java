package dev.Voatix.service;

import dev.Voatix.dto.*;
import dev.Voatix.dto.projection.CommentCountProjection;
import dev.Voatix.dto.projection.IdeaFileKeyProjection;
import dev.Voatix.dto.projection.VoteStatsProjection;
import dev.Voatix.entity.*;
import dev.Voatix.entity.enums.IdeaStatusEnum;
import dev.Voatix.entity.enums.RoleOfUserEnum;
import dev.Voatix.mapper.IdeaMapper;
import dev.Voatix.mapper.VotingEstimateMapper;
import dev.Voatix.repositories.*;
import dev.Voatix.utils.exceptions.commonException.RoleOfUserNotFoundException;
import dev.Voatix.utils.exceptions.commonException.UserUnauthorizedException;
import dev.Voatix.utils.exceptions.fileException.FileOwnershipException;
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

import java.security.Principal;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class IdeaService {

    private final IdeaRepository ideaRepository;
    private final IdeaMapper ideaMapper;
    private final UserRepository userRepository;
    private final VotingEstimateRepository votingEstimatesRepository;
    private final ProjectRepository projectRepository;
    private final ModeratorRepository moderatorRepository;
    private final FileRepository fileRepository;
    private final VotingEstimateMapper votingEstimateMapper;
    private final CredentialsRepository credentialsRepository;

    public Page<IdeaWithStatsDTO> getIdeas(String project, Integer page, Integer limit, String filterBy, String searchedValue, Principal principal) {
        IdeaStatusEnum status = null;
        if (!filterBy.isBlank() && !filterBy.equals("ALL")) {
            try {
                log.info("\"" + filterBy + "\"");
                status = IdeaStatusEnum.valueOf(filterBy);
            } catch (IllegalArgumentException e) {
                throw new UnknownStatusException(filterBy);
            }
        }
        Pageable pageParam = PageRequest.of(page, limit);
        Long userId = userRepository.findIdByNickname(principal.getName())
                .orElseThrow(() -> new UserUnauthorizedException(principal.getName()));
        Page<IdeaEntity> ideas = ideaRepository.findIdeas(project, status, searchedValue, pageParam);

        List<Long> ids = ideas.getContent().stream().map(IdeaEntity::getId).toList();

        List<VoteStatsProjection> voteStatsProj = ideaRepository.getVoteStats(ids, userId);
        List<CommentCountProjection> commentsProj = ideaRepository.getCommentCounts(ids);
        List<IdeaFileKeyProjection> filesProj = ideaRepository.findFilesByIdeaIds(ids);
        return ideaMapper.toPageDto(ideas, voteStatsProj, commentsProj, filesProj);
    }

    public IdeaWithStatsDTO getIdea(Long ideaId, Principal principal) {
        Long userId = userRepository.findIdByNickname(principal.getName())
                .orElseThrow(() -> new UserUnauthorizedException(principal.getName()));
        IdeaEntity idea = ideaRepository.findIdeaById(ideaId)
                .orElseThrow(() -> new IdeaNotFoundException(ideaId));
        VoteStatsProjection voteStatsProjection = ideaRepository.getVoteStatsIdeaById(ideaId, userId)
                .orElseThrow(() -> new VoteStatsNotFoundException(ideaId));
        CommentCountProjection commentCountProjection = ideaRepository.getCommentCountByIdeaId(ideaId)
                .orElseThrow(() -> new CommentCountNotFoundException(ideaId));
        List<String> files = ideaRepository.findFileByIdeaId(ideaId);
        return ideaMapper.toStatsDto(idea, voteStatsProjection, commentCountProjection, files);
    }

    public void upsertLike(Long ideaId, Long like, Principal principal) {
        IdeaEntity idea = ideaRepository.findEntityById(ideaId)
                .orElseThrow(() -> new IdeaNotFoundException(ideaId));
        UserEntity user = userRepository.findByNickname(principal.getName())
                .orElseThrow(() -> new UserUnauthorizedException(principal.getName()));
        VotingEstimateEntity votingEstimate = votingEstimatesRepository.findByUserIdAndIdeaId(user.getId(), idea.getId())
                .orElse(null);
        if (like == 0L) {
            if (votingEstimate != null) {
                votingEstimatesRepository.delete(votingEstimate);
            }
        } else if (votingEstimate == null) {
            votingEstimatesRepository.save(votingEstimateMapper.toEntity(user, idea, like == 1L));
        } else {
            votingEstimateMapper.updateEntity(votingEstimate, like == 1L);
        }
    }

    public void createIdea(IdeaCreateDTO ideaDTO, Principal principal) {
        Long userId = userRepository.findIdByNickname(principal.getName())
                .orElseThrow(() -> new UserUnauthorizedException(principal.getName()));
        Long projectId = projectRepository.findIdByTitle(ideaDTO.getProjectName())
                .orElseThrow(() -> new ProjectNotFoundException(ideaDTO.getProjectName()));
        List<FileEntity> files = fileRepository.findByFileKeys(ideaDTO.getFileKeys());
        if (files.size() != ideaDTO.getFileKeys().size()) {
            throw new FilesNotFoundException();
        }
        if (fileRepository.existsByOwner(ideaDTO.getFileKeys(), userId)){
            throw new FileOwnershipException();
        }
        IdeaEntity idea = ideaMapper.toEntity(ideaDTO, projectId, userId, files);
        ideaRepository.save(idea);
    }

    public void updateIdea(IdeaUpdateDTO ideaUpdateDTO, Long ideaId, Principal principal) {
        Long userId = userRepository.findIdByNickname(principal.getName())
                .orElseThrow(() -> new UserUnauthorizedException(principal.getName()));
        IdeaEntity idea = ideaRepository.findEntityById(ideaId)
                .orElseThrow(() -> new IdeaNotFoundException(ideaId));
        if (idea.getUser().getId().equals(userId)) {
            ideaMapper.updateEntity(ideaUpdateDTO, idea);
        } else {
            throw new IdeaAccessDeniedException(principal.getName());
        }
    }

    public void deleteIdea(Long ideaId, Principal principal) {
        Long userId = userRepository.findIdByNickname(principal.getName())
                .orElseThrow(() -> new UserUnauthorizedException(principal.getName()));
        Long findIdeaId = ideaRepository.findIdById(ideaId)
                .orElseThrow(() -> new IdeaNotFoundException(ideaId));
        Long projectId = ideaRepository.findProjectIdById(ideaId)
                .orElseThrow(() -> new ProjectNotFoundException(ideaId.toString()));
        Long authorIdOfIdea = ideaRepository.findUserIdById(ideaId)
                .orElseThrow(() -> new IdeaNotFoundException(ideaId));
        RoleOfUserEnum role = credentialsRepository.findRoleOfUserByUserId(userId)
                .orElseThrow(() -> new RoleOfUserNotFoundException(principal.getName()));
        boolean isAuthor = authorIdOfIdea.equals(userId);
        if (!isAuthor && !role.equals(RoleOfUserEnum.ADMIN)) {
            if(!moderatorRepository.existsByUserIdAndProjectId(userId, projectId)){
                throw new ModeratorAccessDeniedException(principal.getName());
            }
        }

        ideaRepository.deleteByIdeaId(ideaId);
    }

    public void updateStatus(Long ideaId, IdeaStatusDTO dto, Principal principal) {
        Long userId = userRepository.findIdByNickname(principal.getName())
                .orElseThrow(() -> new UserUnauthorizedException(principal.getName()));
        IdeaEntity idea = ideaRepository.findEntityById(ideaId)
                .orElseThrow(() -> new IdeaNotFoundException(ideaId));
        if(!moderatorRepository.existsByUserIdAndProjectId(userId, idea.getProjectId())){
            throw new ModeratorAccessDeniedException(principal.getName());
        }
        ideaMapper.updateEntity(dto, idea);
        ideaRepository.save(idea);
    }
}
