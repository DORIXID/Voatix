package dev.Voatix.entity;

import dev.Voatix.entity.enums.IdeaStatusEnum;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "ideas")
public class IdeaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 1600)
    private String description;

    @Column(nullable = false, length = 1600)
    private LocalDateTime dateTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IdeaStatusEnum status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, insertable = false, updatable = false)
    private UserEntity user;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false, insertable = false, updatable = false)
    private ProjectEntity project;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @OneToMany( mappedBy = "idea", fetch = FetchType.LAZY)
    private List<CommentEntity> comments;

    @OneToMany( mappedBy = "idea", fetch = FetchType.LAZY)
    private List<VotingEstimateEntity> votingEstimates;

    @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "idea_id")
    private List<FileEntity> files;
}
