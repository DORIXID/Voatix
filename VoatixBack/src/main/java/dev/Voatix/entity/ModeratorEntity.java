package dev.Voatix.entity;

import dev.Voatix.entity.embeddable.ModeratorId;
import dev.Voatix.entity.enums.RoleOfProjectManager;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "moderators")
public class ModeratorEntity {

    @EmbeddedId
    private ModeratorId id = new ModeratorId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("projectId")
    @JoinColumn(name = "project_id", nullable = false)
    private ProjectEntity project;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoleOfProjectManager role;

}


