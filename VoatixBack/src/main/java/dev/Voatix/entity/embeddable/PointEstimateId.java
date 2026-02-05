package dev.Voatix.entity.embeddable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;

import java.io.Serializable;

@Data
@Embeddable
public class PointEstimateId implements Serializable {

    @Column(name = "user_id", insertable=false, updatable=false)
    private Long userId;

    @Column(name = "point_id", insertable=false, updatable=false)
    private Long pointId;
}
