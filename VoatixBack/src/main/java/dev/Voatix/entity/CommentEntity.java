package dev.Voatix.entity;

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
@Table(name = "comments")
public class CommentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String text;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "user_id")
    private UserEntity user;

    @Column(nullable = false)
    private Timestamp dateTime;

    @OneToMany(fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "comment_id")
    private List<FileEntity> files;

    @OneToMany( mappedBy = "comment", fetch = FetchType.LAZY, orphanRemoval = true )
    private List<CommentRaitingsEntity> commentRaitings;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idea_id",  nullable = false)
    private IdeaEntity idea;
}
