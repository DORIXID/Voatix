package dev.Voatix.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "votingpoints")
public class VotingPointEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id", nullable = false)
    private SurveyEntity survey;

    @OneToMany( mappedBy = "votingPoint", fetch = FetchType.LAZY, orphanRemoval = true )
    private List<PointEstimateEntity> pointEstimates;
}
