package dev.Voatix.mapper;


import dev.Voatix.dto.IdeaDTO;
import dev.Voatix.dto.IdeaWithStatsDTO;
import dev.Voatix.entity.IdeaEntity;
import org.mapstruct.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.List;

@Mapper(componentModel = "spring")
public abstract class IdeaMapper {


    public abstract IdeaDTO toDto(IdeaEntity idea);

    public IdeaWithStatsDTO toStatsDto(Object[] row) {
        IdeaEntity idea = (IdeaEntity) row[0];
        Long likes = (Long) row[1];
        Long dislikes = (Long) row[2];

        return new IdeaWithStatsDTO(
                toDto(idea),
                likes,
                dislikes
        );
    }

    public Page<IdeaWithStatsDTO> toStatsPage(Page<Object[]> page) {
        List<IdeaWithStatsDTO> list = page.getContent()
                .stream()
                .map(this::toStatsDto)
                .toList();

        return new PageImpl<>(list, page.getPageable(), page.getTotalElements());
    }
}
