package dev.Voatix.repositories;

import dev.Voatix.dto.projection.CommentStatsProjection;
import dev.Voatix.dto.projection.CommentFileKeyProjection;
import dev.Voatix.entity.CommentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<CommentEntity, Long> {

    @EntityGraph(attributePaths = {"user", "user.avatar", "user.credentials"})
    @Query(value = """
            SELECT c
                   FROM CommentEntity c
                   WHERE c.idea.id = :ideaId
                   ORDER BY c.dateTime DESC
            """)
    Page<CommentEntity> findByIdea(
            @Param("ideaId") Long ideaId,
            Pageable pageable);

    @Query(value = """
            SELECT f.comment_id AS commentId, f.key
            FROM files f
            WHERE comment_id IN :commentIds
            """, nativeQuery = true)
    List<CommentFileKeyProjection> findFilesByCommentIds(@Param("commentIds") List<Long> commentIds);

    @Query(value = """ 
            SELECT c.comment_id as commentId,
                  COUNT(CASE WHEN c.is_like = true THEN 1 END) as likes,
                  COUNT(CASE WHEN c.is_like = false THEN 1 END) as dislikes,
                  COALESCE(MAX(CASE WHEN c.user_id = :userId THEN (CASE WHEN c.is_like = true THEN 1 ELSE -1 END) END), 0) as userVote
                      FROM commentraitings c
                      WHERE c.comment_id IN :commentIds
                      GROUP BY c.comment_id
            """, nativeQuery = true)
    List<CommentStatsProjection> getCommentsRaitingsStats(
            @Param("commentIds") List<Long> commentIds,
            @Param("userId") Long userId
    );

    @Query(value = """
            select c
            FROM CommentEntity c
            where c.id = :commentId
            """)
    Optional<CommentEntity> findById(
            @Param("commentId") Long commentId
    );

    @Query("select c.id from CommentEntity c where c.id = :commentId")
    Optional<Long> findIdById(Long commentId);

    @Query("select c.userId from CommentEntity c where c.id = :commentId")
    Optional<Long> findUserIdById(Long commentId);

    @Query("select c.ideaId from CommentEntity c where c.id = :commentId")
    Optional<Long> findIdeaIdById(Long commentId);


    void deleteById(Long commentId);

}