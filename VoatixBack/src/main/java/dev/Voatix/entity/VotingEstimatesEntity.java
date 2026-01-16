package dev.Voatix.entity;

import dev.Voatix.entity.embeddable.VotingEstimatesId;
import jakarta.persistence.*;
import lombok.*;

@Setter
@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "votingestimates")
public class VotingEstimatesEntity {
    @EmbeddedId
    private VotingEstimatesId id = new VotingEstimatesId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user = new UserEntity();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("ideaId")
    @JoinColumn(name = "idea_id", nullable = false)
    private IdeaEntity idea = new IdeaEntity();

    @Column(nullable = false)
    private Boolean isLike;
}
