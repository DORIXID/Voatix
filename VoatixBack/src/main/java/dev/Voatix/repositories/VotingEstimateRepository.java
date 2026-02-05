package dev.Voatix.repositories;

import dev.Voatix.entity.VotingEstimateEntity;
import dev.Voatix.entity.embeddable.VotingEstimateId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VotingEstimateRepository extends JpaRepository<VotingEstimateEntity, VotingEstimateId> {
    Optional<VotingEstimateEntity> findByUserIdAndIdeaId(Long userId, Long ideaId);
}
