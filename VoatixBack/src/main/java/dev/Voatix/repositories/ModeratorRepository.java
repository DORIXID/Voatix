package dev.Voatix.repositories;

import dev.Voatix.dto.user.UserModeratorProjection;
import dev.Voatix.entity.ModeratorEntity;
import dev.Voatix.entity.UserEntity;
import dev.Voatix.entity.enums.RoleOfProjectManager;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ModeratorRepository extends JpaRepository<ModeratorEntity, Long> {

    @EntityGraph(attributePaths = {"project"})
    @Query("SELECT m " +
            "FROM ModeratorEntity m " +
            "WHERE m.user.id = :userId")
    List<ModeratorEntity> findByUserId(Long userId);

    boolean existsByUserIdAndProjectId(Long userId, Long projectId);

    boolean existsByUserIdAndProjectIdAndRole(Long userId, Long projectId, RoleOfProjectManager role);

    @Query(value = """
        SELECT u.id as userId, u.nickname as nickname, f.id as fileId
        FROM moderators m
        JOIN users u on m.user_id = u.id
        LEFT JOIN files f on u.avatar_id = f.id
        JOIN projects p on m.project_id = p.id
        WHERE p.id = :projectId and m.role = 'MANAGER'
        """, nativeQuery = true)
    List<UserModeratorProjection> findManagersByProjectId(Long projectId);

    @Modifying
    @Query("""
    DELETE FROM ModeratorEntity m
    WHERE m.user.id = :userId
    AND m.project.id = :projectId
    """)
    void deleteByUserIdAndProjectId(Long userId, Long projectId);

    Long user(UserEntity user);
}
