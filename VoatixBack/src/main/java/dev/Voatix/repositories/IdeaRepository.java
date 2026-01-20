package dev.Voatix.repositories;

import dev.Voatix.dto.IdeaWithStatsDTO;
import dev.Voatix.entity.IdeaEntity;

import dev.Voatix.entity.UserEntity;
import dev.Voatix.entity.enums.IdeaStatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface IdeaRepository extends JpaRepository<IdeaEntity, Long> {

    @Query("""
                select i,
                sum(case when v.isLike = true then 1 else 0 end) as likes,
                sum(case when v.isLike = false then 1 else 0 end) as disLikes,
                COALESCE((select case when ve.isLike = true then 1L
                             when ve.isLike = false then -1L
                                         else 0L end
                from VotingEstimatesEntity ve
                where ve.idea = i and ve.user = :user
                ), 0L) as vote,
                (
                select count(c)
                from CommentEntity c
                where c.idea = i
                ) as commentsCount
                from IdeaEntity i
                join i.project p
                left join i.votingEstimates v
                where (i.description ilike CONCAT('%', :search, '%')
                   or i.title ilike CONCAT('%', :search, '%'))
                  and (i.status = :status or :status is null)
                  and p.title like :project
                group by i
                order by i.id
            """)
    Page<Object[]> findIdeas(
            String project,
            UserEntity user,
            IdeaStatusEnum status,
            String search,
            Pageable pageable
    );

    Optional<IdeaEntity> findById(Long id);

    UserEntity user(UserEntity user);
}
