package dev.Voatix.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "files")
public class FileEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String bucket;

    @Column(nullable = false)
    private String key;

    @Column(nullable = false)
    private String contentType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "uploader_id", insertable = false, updatable = false)
    private UserEntity uploader;

    @Column(name = "uploader_id")
    private Long uploaderId;
}
