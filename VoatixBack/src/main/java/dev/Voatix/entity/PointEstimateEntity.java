package dev.Voatix.entity;

import dev.Voatix.entity.embeddable.PointEstimateId;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "pointestimates")
public class PointEstimateEntity {

    @EmbeddedId
    private PointEstimateId id = new PointEstimateId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id",nullable = false)
    private UserEntity user = new UserEntity();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("pointId")
    @JoinColumn(name = "point_id", nullable = false)
    private VotingPointEntity votingPoint = new VotingPointEntity();

}
