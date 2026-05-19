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
        select c
        from CredentialsEntity c
        where c.user.id = :userId
        """)
    Optional<CredentialsEntity> findByUserId(Long userId);

    @Query(value = """
            SELECT c.role
            FROM credentials c
            Where c.user_id = :userId
            """, nativeQuery = true)
    Optional<RoleOfUserEnum> findRoleOfUserByUserId(Long userId);
}
