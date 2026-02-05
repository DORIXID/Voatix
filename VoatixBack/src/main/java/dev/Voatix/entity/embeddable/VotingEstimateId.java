package dev.Voatix.entity.embeddable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;

import java.io.Serializable;

@Data
@Embeddable
public class VotingEstimateId implements Serializable {

    @Column(name = "idea_id", insertable=false, updatable=false)
    private Long ideaId;

    @Column(name = "user_id", insertable=false, updatable=false)
    private Long userId;
}
