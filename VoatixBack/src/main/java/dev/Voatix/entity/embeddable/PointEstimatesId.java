package dev.Voatix.entity.embeddable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import lombok.Data;

import java.io.Serializable;

@Data
@Embeddable
public class PointEstimatesId implements Serializable {

    @Column(name = "user_id", insertable=false, updatable=false)
    private Long userId;

    @Column(name = "point_id", insertable=false, updatable=false)
    private Long pointId;
}
