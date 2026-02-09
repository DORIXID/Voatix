package dev.Voatix.repositories;

import dev.Voatix.entity.VotingPointEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VotingPointRepository extends JpaRepository<VotingPointEntity, Long> {

    @EntityGraph(attributePaths = {"survey"})
    Optional<VotingPointEntity> findById(Long id);
}
