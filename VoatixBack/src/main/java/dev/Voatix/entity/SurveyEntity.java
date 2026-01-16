package dev.Voatix.entity;

import dev.Voatix.entity.enums.TypeOfSurveyEnum;
import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;
import java.util.List;

@Setter
@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
    private Timestamp startDate;

    @Column(nullable = false, name = "end_date")
    private Timestamp endDate;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TypeOfSurveyEnum type;

    @OneToMany( mappedBy = "survey", fetch = FetchType.LAZY, orphanRemoval = true )
    private List<VotingPointEntity> votingPoints;

    @ManyToOne(fetch =  FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private ProjectEntity project;

}
