package dev.Voatix.entity.embeddable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.Id;
import lombok.Data;

import java.io.Serializable;

@Data
@Embeddable
public class CommentsRaitingId implements Serializable {
    @Column(name = "comment_id", insertable=false, updatable=false)
    private Long commentId;

    @Column(name = "user_id", insertable=false, updatable=false)
    private Long userId;
}
