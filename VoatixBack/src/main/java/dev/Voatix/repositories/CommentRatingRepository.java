package dev.Voatix.repositories;

import dev.Voatix.entity.CommentRatingEntity;
import dev.Voatix.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CommentRatingRepository extends JpaRepository<CommentRatingEntity, Long> {
    Optional<CommentRatingEntity> findByUserIdAndCommentId(Long userId, Long commentId);

    @Modifying
    @Query("DELETE FROM CommentRatingEntity c WHERE c.id.commentId = :commentId AND c.id.userId = :userId")
    void deleteByCommentIdAndUserId(Long commentId, Long userId);

    Long user(UserEntity user);
}
