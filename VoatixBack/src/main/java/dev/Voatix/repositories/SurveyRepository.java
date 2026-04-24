package dev.Voatix.repositories;

import dev.Voatix.dto.survey.VotingEstimatesProjection;
import dev.Voatix.entity.SurveyEntity;
import dev.Voatix.entity.enums.TypeOfSurveyEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SurveyRepository extends JpaRepository<SurveyEntity, Long> {


    @EntityGraph(attributePaths = {"project"})
    @Query(value = """
            select s
                from SurveyEntity s
                join s.project p
                    where (s.title ilike CONCAT('%', :search, '%')
                    or s.description ilike CONCAT('%', :search, '%'))
                    and p.id = :projectId
                    and (:status is null or s.type = :status)
                order by s.startDate DESC
            """)
    Page<SurveyEntity> findSurveys(
            @Param("projectId") Long projectId,
            @Param("status") TypeOfSurveyEnum status,
            @Param("search") String search,
            Pageable pageable
    );

    @Query(value = """
    SELECT
        vp.survey_id as surveyId,
        vp.id as id,
        vp.title as title,
        COUNT(pe_all.user_id) as votesCount,
        CASE WHEN COUNT(pe_user.user_id) > 0 THEN true ELSE false END as isVoted
    FROM votingpoints vp
    LEFT JOIN pointestimates pe_all ON vp.id = pe_all.point_id
    LEFT JOIN pointestimates pe_user ON vp.id = pe_user.point_id AND pe_user.user_id = :userId
    WHERE vp.survey_id IN :surveyIds
    GROUP BY vp.survey_id, vp.id, vp.title
    """, nativeQuery = true)
    List<VotingEstimatesProjection> findAllPointsWithVotes(
            @Param("surveyIds") List<Long> surveyIds,
            @Param("userId") Long userId
    );

    Optional<SurveyEntity> findSurveyById(@Param("surveyId") Long surveyId);
}
