package dev.Voatix.repositories;

import dev.Voatix.entity.VotingEstimatesEntity;
import dev.Voatix.entity.embeddable.VotingEstimatesId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VotingEstimatesRepository extends JpaRepository<VotingEstimatesEntity, VotingEstimatesId> {
    Optional<VotingEstimatesEntity> findByUserIdAndIdeaId(Long userId, Long ideaId);
}
