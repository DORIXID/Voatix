package dev.Voatix.service;

import dev.Voatix.dto.*;
import dev.Voatix.dto.projection.CommentCountProjection;
import dev.Voatix.dto.projection.VoteStatsProjection;
import dev.Voatix.entity.*;
import dev.Voatix.entity.enums.IdeaStatusEnum;
import dev.Voatix.entity.enums.RoleOfUserEnum;
import dev.Voatix.mapper.IdeaMapper;
import dev.Voatix.mapper.VotingEstimateMapper;
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
public class IdeaService {

    private final IdeaRepository ideaRepository;
    private final IdeaMapper ideaMapper;
    private final UserRepository userRepository;
    private final VotingEstimateRepository votingEstimatesRepository;
    private final ProjectRepository projectRepository;
    private final ModeratorRepository moderatorRepository;
    private final FileRepository fileRepository;
    private final VotingEstimateMapper votingEstimateMapper;


    //todo: сделать кастом exceptions
    public Page<IdeaWithStatsDTO> getIdeas(String project, Integer page, Integer limit, String filterBy, String searchedValue, Principal principal) {
        IdeaStatusEnum status = null;
        if (!filterBy.isBlank() && !filterBy.equals("ALL")) {
            try {
                log.info("\"" + filterBy + "\"");
                status = IdeaStatusEnum.valueOf(filterBy);
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown status: " + filterBy);
            }
        }
        Pageable pageParam = PageRequest.of(page, limit);
        UserEntity user = userRepository.findByNickname(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User \"" + principal.getName() + "\" not found"));
        Page<IdeaEntity> ideas = ideaRepository.findIdeas(project, status, searchedValue, pageParam);

        List<Long> ids = ideas.getContent().stream().map(IdeaEntity::getId).toList();

        List<VoteStatsProjection> voteStatsProj = ideaRepository.getVoteStats(ids, user.getId());
        List<CommentCountProjection> commentsProj = ideaRepository.getCommentCounts(ids);
        return ideaMapper.toPageDto(ideas, voteStatsProj, commentsProj);
    }

    public IdeaWithStatsDTO getIdea(Long ideaId, Principal principal) {
        UserEntity user = userRepository.findByNickname(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User \"" + principal.getName() + "\" not found"));

        IdeaEntity ideaProjection = ideaRepository.findIdeaById(ideaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Idea with id " + ideaId + " not found"));
        VoteStatsProjection voteStatsProjection = ideaRepository.getVoteStatsIdeaById(ideaId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "VoteStats for ideaId " + ideaId + " not found"));
        CommentCountProjection commentCountProjection = ideaRepository.getCommentCountByIdeaId(ideaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "CommentCount for ideaId " + ideaId + " not found"));
        return ideaMapper.toStatsDto(ideaProjection, voteStatsProjection, commentCountProjection);
    }

    public void upsertLike(Long ideaId, Long like, Principal principal) {
        IdeaEntity idea = ideaRepository.findById(ideaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Idea not found"));
        UserEntity user = userRepository.findByNickname(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User \"" + principal.getName() + "\" not found"));
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
        UserEntity user = userRepository.findByNickname(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User \"" + principal.getName() + "\" not found"));
        ProjectEntity project = projectRepository.findByName(ideaDTO.getProjectName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project " + ideaDTO.getProjectName() + " not found"));
        List<FileEntity> files = fileRepository.findByFileKeys(ideaDTO.getFileKeys());
        if (files.size() != ideaDTO.getFileKeys().size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Files are not found");
        }
        if (fileRepository.existsByOwner(ideaDTO.getFileKeys(), user.getId())){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied: Some files don't belong to you");
        }
        IdeaEntity idea = ideaMapper.toEntity(ideaDTO, project, user, files);
        log.info("\n\n\n\n\n\n\n\n\n\n" + idea.getFiles().toString() + "\n\n\n\n\n\n\n");
        ideaRepository.save(idea);
    }

    public void updateIdea(IdeaUpdateDTO ideaUpdateDTO, Long ideaId, Principal principal) {
        UserEntity user = userRepository.findByNickname(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User \"" + principal.getName() + "\" not found"));
        IdeaEntity idea = ideaRepository.findById(ideaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Idea " + ideaId + " not found"));
        if (idea.getUser().getId().equals(user.getId())) {
            log.info("\n\n\n" + user.getId() + " = " + idea.getUser().getId());
            ideaMapper.updateEntity(ideaUpdateDTO, idea);
        } else {
            log.info("\n\n\n" + user.getId() + " != " + idea.getUser().getId());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User \"" + principal.getName() + "\" does not have access");
        }
    }

    //todo: Возможно нужно создать отдельный метод в репозитории для удаления т к присутствует n+1
    public void deleteIdea(Long ideaId, Principal principal) {
        UserEntity user = userRepository.findByNickname(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User \"" + principal.getName() + "\" not found"));
        IdeaEntity idea = ideaRepository.findById(ideaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Idea " + ideaId + " not found"));
        ProjectEntity projectEntity = idea.getProject();
        boolean isAuthor = idea.getUser().getId().equals(user.getId());
        if (!isAuthor && !user.getCredentials().getRole().equals(RoleOfUserEnum.ADMIN)) {
            ModeratorEntity moderator = moderatorRepository.findByUserIdAndProjectId(user.getId(), projectEntity.getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "User \"" + principal.getName() + "\" does not have access"));
        }
        ideaRepository.delete(idea);
    }

    public void updateStatus(Long ideaId, IdeaStatusDTO dto, Principal principal) {
        UserEntity user = userRepository.findByNickname(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User \"" + principal.getName() + "\" not found"));
        IdeaEntity idea = ideaRepository.findById(ideaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Idea " + ideaId + " not found"));
        ModeratorEntity moderator = moderatorRepository.findByUserIdAndProjectId(user.getId(), idea.getProject().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "User \"" + principal.getName() + "\" does not have access"));
        ideaMapper.updateEntity(dto, idea);
        ideaRepository.save(idea);
    }



}
