package dev.Voatix.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Setter
@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "projects")
public class ProjectEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String title;

    private Boolean active;

    @OneToOne(fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "avatar")
    private FileEntity avatar;

    @OneToMany( mappedBy = "project", fetch = FetchType.LAZY, orphanRemoval = true )
    private List<ModeratorEntity> moderators;

    @OneToMany( mappedBy = "project", fetch = FetchType.LAZY, orphanRemoval = true )
    private List<IdeaEntity> ideas;

    @OneToMany( mappedBy = "project", fetch = FetchType.LAZY, orphanRemoval = true )
    private List<SurveyEntity> surveys;
}
