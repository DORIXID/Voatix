package dev.Voatix.service;

import dev.Voatix.dto.IdeaWithStatsDTO;
import dev.Voatix.entity.IdeaEntity;
import dev.Voatix.entity.UserEntity;
import dev.Voatix.entity.VotingEstimatesEntity;
import dev.Voatix.entity.enums.IdeaStatusEnum;
import dev.Voatix.mapper.IdeaMapper;
import dev.Voatix.repositories.IdeaRepository;
import dev.Voatix.repositories.UserRepository;
import dev.Voatix.repositories.VotingEstimatesRepository;
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

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class IdeaService {

    private final IdeaRepository ideaRepository;
    private final IdeaMapper ideaMapper;
    private final UserRepository userRepository;
    private final VotingEstimatesRepository votingEstimatesRepository;

    public Page<IdeaWithStatsDTO> getIdeas(String project, Integer page, Integer limit, String filterBy, String searchedValue, Principal principal) {
        IdeaStatusEnum statusEnum;
        if (filterBy.isBlank() || filterBy.equals("ALL")) {
            statusEnum = null;
        } else {
            try {
                statusEnum = IdeaStatusEnum.valueOf(filterBy);
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown status: " + filterBy);
            }
        }
        Pageable pageParam = PageRequest.of(page, limit);
        UserEntity userEntity = userRepository.findByNickname(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User \"" + principal.getName() + "\" not found"));
        Page<Object[]> result = ideaRepository.findIdeas(project, userEntity, statusEnum, searchedValue, pageParam);
        return ideaMapper.toStatsPage(result);
    }

    public void upsertLike(Long ideaId, Long like, Principal principal) {
        IdeaEntity ideaEntity = ideaRepository.findById(ideaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Idea not found"));
        UserEntity userEntity = userRepository.findByNickname(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User \"" + principal.getName() + "\" not found"));
        VotingEstimatesEntity votingEstimatesEntity = votingEstimatesRepository.findByUserIdAndIdeaId(userEntity.getId(), ideaEntity.getId())
                .orElse(null);
        if (like == 0L){
            if(votingEstimatesEntity != null) {
                votingEstimatesRepository.delete(votingEstimatesEntity);
            } return;
        } else if (votingEstimatesEntity == null) {
            votingEstimatesEntity = new VotingEstimatesEntity();
            votingEstimatesEntity.setIdea(ideaEntity);
            votingEstimatesEntity.setUser(userEntity);
        }
        votingEstimatesEntity.setIsLike(like == 1L);
        votingEstimatesRepository.save(votingEstimatesEntity);
    }
}
