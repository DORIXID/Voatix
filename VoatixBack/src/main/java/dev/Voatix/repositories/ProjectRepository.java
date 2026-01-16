package dev.Voatix.repositories;

import dev.Voatix.entity.ProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ProjectRepository extends JpaRepository<ProjectEntity, Long> {

    Optional<ProjectEntity> findById(Long projectId);

    @Query("SELECT p " +
            "FROM ProjectEntity p " +
            "WHERE p.title LIKE :name")
    Optional<ProjectEntity> findByName(String name);

}
