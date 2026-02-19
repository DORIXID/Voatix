package dev.Voatix.repositories;

import dev.Voatix.dto.projection.UserModeratorProjection;
import dev.Voatix.entity.ModeratorEntity;
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

    @Query(value = """
            SELECT *
                        FROM moderators
                        WHERE user_id = :userId
                        AND project_id = :projectId
            """, nativeQuery = true)
    Optional<ModeratorEntity> findByUserIdAndProjectId(Long userId, Long projectId);

    boolean existsByUserIdAndProjectId(Long userId, Long projectId);

    boolean existsByUserNicknameAndProjectTitleAndRole(String nickname, String title, RoleOfProjectManager role);

    @Query(value = """
        SELECT u.nickname as nickname, f.key as key
        FROM moderators m
        JOIN users u on m.user_id = u.id
        LEFT JOIN files f on u.avatar_id = f.id
        JOIN projects p on m.project_id = p.id
        WHERE p.title = :projectTitle and m.role = 'MANAGER'
        """, nativeQuery = true)
    List<UserModeratorProjection> findManagersByProjectTitle(String projectTitle);

    @Modifying
    @Query("""
    DELETE FROM ModeratorEntity m
    WHERE m.user.nickname = :nickname
    AND m.project.title = :projectTitle
    """)
    void deleteByNicknameAndTitle(String nickname, String projectTitle);
}
