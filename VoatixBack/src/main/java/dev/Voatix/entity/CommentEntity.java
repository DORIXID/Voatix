package dev.Voatix.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;


import java.time.LocalDateTime;
import java.util.List;


@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "comments")
public class CommentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String text;

    @Column(nullable = false)
    private LocalDateTime dateTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "user_id", insertable = false, updatable = false)
    private UserEntity user;

    @Column(name = "user_id")
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idea_id",  nullable = false, insertable = false, updatable = false)
    private IdeaEntity idea;

    @Column(name = "idea_id")
    private Long ideaId;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "comment_id")
    private List<FileEntity> files;

    @OneToMany( mappedBy = "comment", fetch = FetchType.LAZY, orphanRemoval = true )
    private List<CommentRatingEntity> commentRaitings;
}
