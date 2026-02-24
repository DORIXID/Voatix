package dev.Voatix.entity;

import dev.Voatix.entity.enums.RoleOfUserEnum;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = true, unique = true)
    private String nickname;

    @OneToOne(fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "avatar_id", insertable = false, updatable = false)
    private FileEntity avatar;

    @Column(name = "avatar_id")
    private Long avatarId;

    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, mappedBy = "user")
    private CredentialsEntity credentials;


}