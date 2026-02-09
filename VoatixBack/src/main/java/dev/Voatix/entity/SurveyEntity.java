package dev.Voatix.entity;

import dev.Voatix.entity.enums.TypeOfSurveyEnum;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "surveys")
public class SurveyEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = true, length = 300)
    private String description;

    @Column(nullable = false, name = "start_date")
    private LocalDateTime startDate;

    @Column(nullable = false, name = "end_date")
    private LocalDateTime endDate;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TypeOfSurveyEnum type;

    @OneToMany( mappedBy = "survey", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true )
    private List<VotingPointEntity> votingPoints;

    @ManyToOne(fetch =  FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, insertable = false, updatable = false)
    private UserEntity user;

    @Column(name = "user_id")
    private Long userId;

    @ManyToOne(fetch =  FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false, insertable = false, updatable = false)
    private ProjectEntity project;

    @Column(name = "project_id")
    private Long projectId;

}
