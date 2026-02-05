package dev.Voatix.repositories;

import dev.Voatix.entity.PointEstimateEntity;
import dev.Voatix.entity.embeddable.PointEstimateId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PointEstimateRepository extends JpaRepository<PointEstimateEntity, PointEstimateId> {
    Optional<PointEstimateEntity> findByUserIdAndVotingPointId(Long userId, Long votingPointId);

    @Query("""
            SELECT p FROM PointEstimateEntity p
            WHERE p.user.id = :userId
            AND p.votingPoint.survey.id = :surveyId
            """)
    Optional<PointEstimateEntity> findByUserIdAndSurveyId(@Param("userId") Long userId, @Param("surveyId") Long surveyId);
}
