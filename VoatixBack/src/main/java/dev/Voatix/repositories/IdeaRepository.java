package dev.Voatix.repositories;

import dev.Voatix.dto.comment.CommentCountProjection;
import dev.Voatix.dto.idea.IdeaFileIdProjection;
import dev.Voatix.dto.idea.VoteStatsProjection;
import dev.Voatix.entity.IdeaEntity;

import dev.Voatix.entity.enums.IdeaStatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface IdeaRepository extends JpaRepository<IdeaEntity, Long> {

    Optional<IdeaEntity> findEntityById(Long id);

    @Modifying
    @Query("DELETE FROM IdeaEntity i WHERE i.id = :ideaId")
    void deleteByIdeaId(Long ideaId);

    @Query("select i.id from IdeaEntity i where i.id = :id")
    Optional<Long> findIdById(Long id);

    @Query("select i.projectId from IdeaEntity i where i.id = :id")
    Optional<Long> findProjectIdById(Long id);

    @Query("select i.userId from IdeaEntity i where i.id = :id")
    Optional<Long> findUserIdById(Long id);

    @Query(value = """
        select i
            from IdeaEntity i
            join fetch i.project p
            join fetch i.user u
            join fetch u.credentials c
            left join fetch u.avatar a
            where (i.description ilike CONCAT('%', :search, '%')
               or i.title ilike CONCAT('%', :search, '%'))
              and (:status is null or i.status = :status)
              and p.id = :projectId
            order by i.dateTime DESC
        """)
    Page<IdeaEntity> findIdeas(
            @Param("projectId") Long projectId,
            @Param("status") IdeaStatusEnum status,
            @Param("search") String search,
            Pageable pageable
    );

    @Query(value = """ 
            SELECT
               v.idea_id as ideaId,
               COUNT(CASE WHEN v.is_like = true THEN 1 END) as likes,
               COUNT(CASE WHEN v.is_like = false THEN 1 END) as dislikes,
               COALESCE(MAX(CASE WHEN v.user_id = :userId THEN (CASE WHEN v.is_like = true THEN 1 ELSE -1 END) END), 0) as userVote
                   FROM votingestimates v
                   WHERE v.idea_id IN :ideaIds
                   GROUP BY v.idea_id
            """, nativeQuery = true)
    List<VoteStatsProjection> getVoteStats(
            @Param("ideaIds") List<Long> ideaIds,
            @Param("userId") Long userId
    );

    @Query(value = """
            SELECT f.idea_id as ideaId, f.id as fileId
            FROM files f
            WHERE f.idea_id IN :ideaIds
            ORDER BY ideaId
            """, nativeQuery = true)
    List<IdeaFileIdProjection> findFilesByIdeaIds(@Param("ideaIds") List<Long> ideaIds);


    @Query(value = """ 
            SELECT
                c.idea_id as ideaId,
                COUNT(c.id) as count
                    FROM comments c
                    WHERE c.idea_id IN :ideaIds
                    GROUP BY c.idea_id
            """, nativeQuery = true)
    List<CommentCountProjection> getCommentCounts(
            @Param("ideaIds") List<Long> ideaIds
    );

    @Query(value = """
            select i
                from IdeaEntity i
                join i.project p
                where i.id = :id
            """)
    Optional<IdeaEntity> findIdeaById(
            @Param("id") Long id
    );

    @Query(value = """ 
            SELECT
               i.id as ideaId,
               COUNT(CASE WHEN v.is_like = true THEN 1 END) as likes,
               COUNT(CASE WHEN v.is_like = false THEN 1 END) as dislikes,
               COALESCE(MAX(CASE WHEN v.user_id = :userId THEN (CASE WHEN v.is_like = true THEN 1 ELSE -1 END) END), 0) as userVote
                FROM ideas i
                LEFT JOIN votingestimates v ON i.id = v.idea_id
                WHERE i.id = :ideaId
                GROUP BY i.id
            """, nativeQuery = true)
    Optional<VoteStatsProjection> getVoteStatsIdeaById(
            @Param("ideaId") Long ideaId,
            @Param("userId") Long userId
    );

    @Query(value = """ 
            SELECT
                c.idea_id as ideaId,
                COUNT(c.id) as count
                    FROM comments c
                    WHERE c.idea_id = :id
                    GROUP BY c.idea_id
            """, nativeQuery = true)
    Optional<CommentCountProjection> getCommentCountByIdeaId(
            @Param("id") Long id
    );

    @Query(value = """
            SELECT f.id
            FROM files f
            WHERE f.idea_id = :ideaId
            """, nativeQuery = true)
    List<Long> findFileIdsByIdeaId(@Param("ideaId") Long ideaId);

}
