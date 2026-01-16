package dev.Voatix.service;

import dev.Voatix.dto.IdeaDTO;
import dev.Voatix.dto.IdeaWithStatsDTO;
import dev.Voatix.entity.IdeaEntity;
import dev.Voatix.entity.enums.IdeaStatusEnum;
import dev.Voatix.mapper.IdeaMapper;
import dev.Voatix.repositories.IdeaRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class IdeaService {

    private final IdeaRepository ideaRepository;
    private final IdeaMapper ideaMapper;

    public Page<IdeaWithStatsDTO> getIdeas(String project, Integer page, Integer limit, String filterBy, String searchedValue) {
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
        Page<Object[]> result = ideaRepository.findIdeas(project, statusEnum, searchedValue, pageParam);
        return ideaMapper.toStatsPage(result);
    }
}
