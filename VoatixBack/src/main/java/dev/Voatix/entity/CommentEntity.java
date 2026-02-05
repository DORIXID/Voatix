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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "user_id")
    private UserEntity user;

    @Column(nullable = false)
    private LocalDateTime dateTime;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 10)
    @JoinColumn(name = "comment_id")
    private List<FileEntity> files;

    @OneToMany( mappedBy = "comment", fetch = FetchType.LAZY, orphanRemoval = true )
    private List<CommentRatingEntity> commentRaitings;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idea_id",  nullable = false)
    private IdeaEntity idea;
}
