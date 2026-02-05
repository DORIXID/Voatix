package dev.Voatix.repositories;

import dev.Voatix.entity.VotingPointEntity;
import dev.Voatix.mapper.VotingPointMapper;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VotingPointRepository extends JpaRepository<VotingPointEntity, Long> {
    Optional<VotingPointEntity> findById(Long id);
}
