package dev.Voatix.repositories;

import dev.Voatix.entity.VotingEstimateEntity;
import dev.Voatix.entity.embeddable.VotingEstimateId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface VotingEstimateRepository extends JpaRepository<VotingEstimateEntity, VotingEstimateId> {

    Optional<VotingEstimateEntity> findByUserIdAndIdeaId(Long userId, Long ideaId);

    @Modifying
    @Query(value = "DELETE FROM votingestimates v WHERE v.idea_id = :ideaId ", nativeQuery = true)
    void deleteByIdeaId(Long ideaId);
}
