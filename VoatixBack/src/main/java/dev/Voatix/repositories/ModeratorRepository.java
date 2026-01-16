package dev.Voatix.repositories;

import dev.Voatix.entity.ModeratorEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ModeratorRepository extends JpaRepository<ModeratorEntity, Long> {

    @EntityGraph(attributePaths = {"project"})
    @Query("SELECT m " +
            "FROM ModeratorEntity m " +
            "WHERE m.user.id = :userId")
    List<ModeratorEntity> findByUserId(Long userId);

}
