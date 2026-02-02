package dev.Voatix.entity;

import dev.Voatix.entity.enums.IdeaStatusEnum;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Cascade;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ideas")
public class IdeaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = true, length = 1600)
    private String description;

    @Column(nullable = true, length = 1600)
    private LocalDateTime dateTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IdeaStatusEnum status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private ProjectEntity project;


    @OneToMany( mappedBy = "idea", fetch = FetchType.LAZY, orphanRemoval = true )
    private List<CommentEntity> comments;

    @OneToMany( mappedBy = "idea", fetch = FetchType.LAZY, orphanRemoval = true )
    private List<VotingEstimatesEntity> votingEstimates;


    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 10)
    @JoinColumn(name = "idea_id")
    private List<FileEntity> files;
}
