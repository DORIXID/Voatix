package dev.Voatix.repositories;

import dev.Voatix.entity.UserEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByNickname(String userName);

    Optional<UserEntity> findById(Long id);

    @EntityGraph(attributePaths = {"avatar"})
    @Query("""
        SELECT u
           FROM UserEntity u
           JOIN u.avatar a
           WHERE u.id IN :userIds
        """)
    List<UserEntity> findUsersByUserIds(
            @Param("userIds") List<Long> userIds);

    @Query("select u.id from UserEntity u where u.nickname = :nickname")
    Optional<Long> getIdByNickname(String nickname);
}
