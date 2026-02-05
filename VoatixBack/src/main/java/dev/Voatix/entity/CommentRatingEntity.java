package dev.Voatix.entity;

import dev.Voatix.entity.embeddable.CommentsRaitingId;
import jakarta.persistence.*;
import lombok.*;


@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "commentraitings")
public class CommentRatingEntity {
    @EmbeddedId
    private CommentsRaitingId id =  new CommentsRaitingId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user = new UserEntity();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("commentId")
    @JoinColumn(name = "comment_id", nullable = false)
    private CommentEntity comment = new CommentEntity();


    @Column(nullable = false, name = "is_like")
    private Boolean isLike;
}
