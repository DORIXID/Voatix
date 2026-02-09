package dev.Voatix.repositories;

import dev.Voatix.entity.CommentRatingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CommentRatingRepository extends JpaRepository<CommentRatingEntity, Long> {
    Optional<CommentRatingEntity> findByUserIdAndCommentId(Long userId, Long commentId);

    void deleteByCommentId(Long commentId);

    @Modifying
    @Query("delete from CommentRatingEntity r where r.comment.ideaId = :ideaId")
    void deleteByIdeaId(Long ideaId);
}
