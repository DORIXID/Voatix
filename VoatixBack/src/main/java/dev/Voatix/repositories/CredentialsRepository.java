package dev.Voatix.repositories;

import dev.Voatix.entity.CredentialsEntity;
import dev.Voatix.entity.enums.RoleOfUserEnum;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CredentialsRepository extends JpaRepository<CredentialsEntity, Long> {

    @EntityGraph(attributePaths = {"password"})
    Optional<CredentialsEntity> findById(Long userId);

    @Query("""
            SELECT c.role
            FROM CredentialsEntity c
            JOIN c.user u
            where u.id = :userId
            """)
    Optional<RoleOfUserEnum> findRoleOfUserByUserId(Long userId);
}
