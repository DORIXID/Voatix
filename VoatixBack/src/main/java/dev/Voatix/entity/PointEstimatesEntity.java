package dev.Voatix.entity;

import dev.Voatix.entity.embeddable.PointEstimatesId;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Setter
@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "pointestimates")
public class PointEstimatesEntity {

    @EmbeddedId
    private PointEstimatesId id = new PointEstimatesId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id",nullable = false)
    private UserEntity user = new UserEntity();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("pointId")
    @JoinColumn(name = "point_id", nullable = false)
    private VotingPointEntity votingPoint = new VotingPointEntity();

}
