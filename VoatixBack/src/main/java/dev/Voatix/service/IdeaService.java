package dev.Voatix.service;

import dev.Voatix.dto.IdeaCreateDTO;
import dev.Voatix.dto.IdeaStatusDTO;
import dev.Voatix.dto.IdeaUpdateDTO;
import dev.Voatix.dto.IdeaWithStatsDTO;
import dev.Voatix.dto.projection.CommentCountProjection;
import dev.Voatix.dto.projection.IdeaProjection;
import dev.Voatix.dto.projection.VoteStatsProjection;
import dev.Voatix.entity.*;
import dev.Voatix.entity.enums.IdeaStatusEnum;
import dev.Voatix.mapper.IdeaMapper;
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
    private final VotingEstimatesRepository votingEstimatesRepository;
    private final ProjectRepository projectRepository;
    private final ModeratorRepository moderatorRepository;


    //todo: сделать кастом exceptions
    public Page<IdeaWithStatsDTO> getIdeas(String project, Integer page, Integer limit, String filterBy, String searchedValue, Principal principal) {
        String status = "";
        if (!filterBy.isBlank() && !filterBy.equals("ALL")) {
            try {
                log.info("\"" + filterBy + "\"");
                status = IdeaStatusEnum.valueOf(filterBy).toString();
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown status: " + filterBy);
            }
        }
        Pageable pageParam = PageRequest.of(page, limit);
        UserEntity userEntity = userRepository.findByNickname(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User \"" + principal.getName() + "\" not found"));
        Page<IdeaProjection> ideasProj = ideaRepository.findIdeas(project, status, searchedValue, pageParam);

        List<Long> ids = ideasProj.getContent().stream().map(IdeaProjection::getId).toList();

        List<VoteStatsProjection> voteStatsProj = ideaRepository.getVoteStats(ids, userEntity.getId());
        List<CommentCountProjection> commentsProj = ideaRepository.getCommentCounts(ids);
        return ideaMapper.toPageDto(ideasProj, voteStatsProj, commentsProj);
    }

    public IdeaWithStatsDTO getIdea(Long ideaId, Principal principal) {
        UserEntity userEntity = userRepository.findByNickname(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User \"" + principal.getName() + "\" not found"));

        IdeaProjection ideaProjection = ideaRepository.findIdeaById(ideaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Idea with id " + ideaId + " not found"));
        VoteStatsProjection voteStatsProjection = ideaRepository.getVoteStatsIdeaById(ideaId, userEntity.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "VoteStats for ideaId " + ideaId + " not found"));
        CommentCountProjection commentCountProjection = ideaRepository.getCommentCountByIdeaId(ideaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "CommentCount for ideaId " + ideaId + " not found"));
        return ideaMapper.toStatsDto(ideaProjection, voteStatsProjection, commentCountProjection);
    }

    public void upsertLike(Long ideaId, Long like, Principal principal) {
        IdeaEntity ideaEntity = ideaRepository.findById(ideaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Idea not found"));
        UserEntity userEntity = userRepository.findByNickname(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User \"" + principal.getName() + "\" not found"));
        VotingEstimatesEntity votingEstimatesEntity = votingEstimatesRepository.findByUserIdAndIdeaId(userEntity.getId(), ideaEntity.getId())
                .orElse(null);
        if (like == 0L) {
            if (votingEstimatesEntity != null) {
                votingEstimatesRepository.delete(votingEstimatesEntity);
            }
            return;
        } else if (votingEstimatesEntity == null) {
            votingEstimatesEntity = new VotingEstimatesEntity();
            votingEstimatesEntity.setIdea(ideaEntity);
            votingEstimatesEntity.setUser(userEntity);
        }
        votingEstimatesEntity.setIsLike(like == 1L);
        votingEstimatesRepository.save(votingEstimatesEntity);
    }

    public void createIdea(IdeaCreateDTO ideaDTO, Principal principal) {
        UserEntity userEntity = userRepository.findByNickname(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User \"" + principal.getName() + "\" not found"));
        ProjectEntity projectEntity = projectRepository.findById(ideaDTO.getProjectId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project " + ideaDTO.getProjectId() + " not found"));
        ideaRepository.save(ideaMapper.toEntity(ideaDTO, projectEntity, userEntity));
    }

    public void updateIdea(IdeaUpdateDTO ideaUpdateDTO, Long ideaId, Principal principal) {
        UserEntity userEntity = userRepository.findByNickname(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User \"" + principal.getName() + "\" not found"));
        IdeaEntity ideaEntity = ideaRepository.findById(ideaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Idea " + ideaId + " not found"));
        if (ideaEntity.getUser().getId().equals(userEntity.getId())) {
            log.info("\n\n\n" + userEntity.getId() + " = " + ideaEntity.getUser().getId());
            ideaMapper.updateEntity(ideaUpdateDTO, ideaEntity);
        } else  {
            log.info("\n\n\n" + userEntity.getId() + " != " + ideaEntity.getUser().getId());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User \"" + principal.getName() + "\" does not have access");
        }
    }

    public void deleteIdea(Long ideaId, Principal principal) {
        UserEntity userEntity = userRepository.findByNickname(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User \"" + principal.getName() + "\" not found"));
        IdeaEntity ideaEntity = ideaRepository.findById(ideaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Idea " + ideaId + " not found"));
        ProjectEntity projectEntity = ideaEntity.getProject();
        ModeratorEntity moderator = moderatorRepository.findByUserIdAndProjectId(userEntity.getId(), projectEntity.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "User \"" + principal.getName() + "\" does not have access"));
        ideaRepository.delete(ideaEntity);
    }

    public void updateStatus(Long ideaId, IdeaStatusDTO dto, Principal principal) {
        UserEntity userEntity = userRepository.findByNickname(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User \"" + principal.getName() + "\" not found"));
        IdeaEntity ideaEntity = ideaRepository.findById(ideaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Idea " + ideaId + " not found"));
        ProjectEntity projectEntity = ideaEntity.getProject();
        ModeratorEntity moderator = moderatorRepository.findByUserIdAndProjectId(userEntity.getId(), projectEntity.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "User \"" + principal.getName() + "\" does not have access"));
        ideaMapper.updateEntity(dto, ideaEntity);
        ideaRepository.save(ideaEntity);
        }

}
