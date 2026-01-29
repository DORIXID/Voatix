package dev.Voatix.entity;

import dev.Voatix.entity.enums.RoleOfUserEnum;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "credentials")
public class CredentialsEntity {

    @Id
    @Column()
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", unique = true)
    private UserEntity user;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "password_id", nullable = false)
    private PasswordEntity password;

    @Column(nullable = true)
    private String eMail;

    @Column(nullable = false)
    private boolean isActive;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoleOfUserEnum role;
}
