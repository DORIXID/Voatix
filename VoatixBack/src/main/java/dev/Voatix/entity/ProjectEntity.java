package dev.Voatix.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "projects")
public class ProjectEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String title;

    private Boolean active;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "avatar", insertable = false, updatable = false)
    private FileEntity avatar;

    @Column(name = "avatar")
    private Long avatarId;

    @OneToMany( mappedBy = "project", fetch = FetchType.LAZY)
    private List<ModeratorEntity> moderators;

    @OneToMany( mappedBy = "project", fetch = FetchType.LAZY)
    private List<IdeaEntity> ideas;

    @OneToMany( mappedBy = "project", fetch = FetchType.LAZY)
    private List<SurveyEntity> surveys;
}
