package dev.Voatix.repositories;

import dev.Voatix.dto.projection.ProjectOfUserProjection;
import dev.Voatix.entity.ProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<ProjectEntity, Long> {

    Optional<ProjectEntity> findById(Long projectId);

    @Query("SELECT p " +
            "FROM ProjectEntity p " +
            "WHERE p.title LIKE :name")
    Optional<ProjectEntity> findByName(String name);

    @Query(value = """
    SELECT
        p.id as projectId,
        p.title as title,
        f.key as key,
        p.active as active,
        m.role as roleOfUser
    FROM projects p
    JOIN files f ON p.avatar = f.id
    JOIN moderators m ON p.id = m.project_id
    WHERE m.user_id = :userId
    """, nativeQuery = true)
    List<ProjectOfUserProjection> findProjectsOfUserByUserId(
            @Param("userId") Long userId
    );

    @Query("""
        SELECT p.id
        from ProjectEntity p
        where p.title = :title
        """)
    Optional<Long> findIdByTitle(String title);
}
