package dev.Voatix.repositories;

import dev.Voatix.dto.projection.CommentCountProjection;
import dev.Voatix.dto.projection.VoteStatsProjection;
import dev.Voatix.entity.IdeaEntity;

import dev.Voatix.entity.enums.IdeaStatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
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

    @EntityGraph(attributePaths = {"project", "user", "user.avatar", "user.credentials", "files"})
    @Query(value = """
        select i
            from IdeaEntity i
            join i.project p
            join i.user u
            join u.credentials c
            left join u.avatar a
            where (i.description ilike CONCAT('%', :search, '%')
               or i.title ilike CONCAT('%', :search, '%'))
              and (:status is null or i.status = :status)
              and p.title like :project
            order by i.dateTime DESC
        """)
    Page<IdeaEntity> findIdeas(
            @Param("project") String project,
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
            SELECT
                i.id as ideaId,
                COUNT(c.id) as count
                    FROM ideas i
                    LEFT JOIN comments c ON c.idea_id = i.id
                    WHERE i.id IN :ideaIds
                    GROUP BY i.id
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
                i.id as ideaId,
                COUNT(c.id) as count
                    FROM ideas i
                    LEFT JOIN comments c ON c.idea_id = i.id
                    WHERE i.id = :id
                    GROUP BY i.id
            """, nativeQuery = true)
    Optional<CommentCountProjection> getCommentCountByIdeaId(
            @Param("id") Long id
    );
}
