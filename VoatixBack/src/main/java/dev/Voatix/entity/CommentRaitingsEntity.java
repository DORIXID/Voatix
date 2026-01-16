package dev.Voatix.entity;

import dev.Voatix.entity.embeddable.CommentsRaitingId;
import jakarta.persistence.*;
import lombok.*;

@Setter
@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "commentraitings")
public class CommentRaitingsEntity {
    @EmbeddedId
    private CommentsRaitingId commentsRaitingId =  new CommentsRaitingId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user = new UserEntity();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("commentId")
    @JoinColumn(name = "comment_id", nullable = false)
    private CommentEntity comment = new CommentEntity();


    @Column(nullable = false)
    private Boolean isLike;
}
