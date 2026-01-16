package dev.Voatix.repositories;

import dev.Voatix.dto.IdeaWithStatsDTO;
import dev.Voatix.entity.IdeaEntity;

import dev.Voatix.entity.enums.IdeaStatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface IdeaRepository extends JpaRepository<IdeaEntity, Long> {

    @Query("""
                select i,
                sum(case when v.isLike = true then 1 else 0 end) as likes,
                sum(case when v.isLike = false then 1 else 0 end) as disLikes
                from IdeaEntity i
                join i.project p
                left join i.votingEstimates v
                where (i.description ilike CONCAT('%', :search, '%')
                   or i.title ilike CONCAT('%', :search, '%'))
                  and (i.status = :status or :status is null)
                  and p.title like :project
                group by i
            """)
    Page<Object[]> findIdeas(
            String project,
            IdeaStatusEnum status,
            String search,
            Pageable pageable
    );

}
