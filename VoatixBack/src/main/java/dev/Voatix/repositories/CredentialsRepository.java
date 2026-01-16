package dev.Voatix.repositories;

import dev.Voatix.entity.CredentialsEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CredentialsRepository extends JpaRepository<CredentialsEntity, Long> {

    @EntityGraph(attributePaths = {"password"})
    Optional<CredentialsEntity> findById(Long userId);
}
