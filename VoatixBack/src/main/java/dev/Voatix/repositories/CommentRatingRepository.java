package dev.Voatix.repositories;

import dev.Voatix.entity.CommentRatingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommentRatingRepository extends JpaRepository<CommentRatingEntity, Long> {
    Optional<CommentRatingEntity> findByUserIdAndCommentId(Long userId, Long commentId);
}
