package dev.Voatix.entity;

import dev.Voatix.entity.embeddable.VotingEstimateId;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "votingestimates")
public class VotingEstimateEntity {
    @EmbeddedId
    private VotingEstimateId id = new VotingEstimateId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user = new UserEntity();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("ideaId")
    @JoinColumn(name = "idea_id", nullable = false)
    private IdeaEntity idea = new IdeaEntity();

    @Column(nullable = false, name = "is_like")
    private Boolean isLike;
}
