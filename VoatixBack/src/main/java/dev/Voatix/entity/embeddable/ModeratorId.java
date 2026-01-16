package dev.Voatix.entity.embeddable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;

import java.io.Serializable;

@Data
@Embeddable
public class ModeratorId implements Serializable {

    @Column(name = "project_id", insertable=false, updatable=false)
    private Long projectId;

    @Column(name = "user_id", insertable=false, updatable=false)
    private Long userId;
}

