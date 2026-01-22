package dev.Voatix.repositories;

import dev.Voatix.dto.IdeaWithStatsDTO;
import dev.Voatix.dto.projection.CommentCountProjection;
import dev.Voatix.dto.projection.IdeaProjection;
import dev.Voatix.dto.projection.VoteStatsProjection;
import dev.Voatix.entity.IdeaEntity;

import dev.Voatix.entity.UserEntity;
import dev.Voatix.entity.enums.IdeaStatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface IdeaRepository extends JpaRepository<IdeaEntity, Long> {


    Optional<IdeaEntity> findById(Long id);


    @Query(value = """
            select i.*
                from ideas i
                join projects p on p.id = i.project_id
                where (i.description ilike CONCAT('%', :search, '%')
                   or i.title ilike CONCAT('%', :search, '%'))
                  and (i.status = :status or :status = '')
                  and p.title like :project
                order by i.id
            """, nativeQuery = true)
    Page<IdeaProjection> findIdeas(
            @Param("project") String project,
            @Param("status") String status,
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
            select i.*
                from ideas i
                join projects p on p.id = i.project_id
                where i.id = :id
            """, nativeQuery = true)
    Optional<IdeaProjection> findIdeaById(
            @Param("id") Long id
    );

    @Query(value = """ 
            SELECT
               v.idea_id as ideaId,
               COUNT(CASE WHEN v.is_like = true THEN 1 END) as likes,
               COUNT(CASE WHEN v.is_like = false THEN 1 END) as dislikes,
               COALESCE(MAX(CASE WHEN v.user_id = :userId THEN (CASE WHEN v.is_like = true THEN 1 ELSE -1 END) END), 0) as userVote
                   FROM votingestimates v
                   WHERE v.idea_id = :ideaId
                   GROUP BY v.idea_id
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
