package dev.Voatix.repositories;

import dev.Voatix.dto.user.ResponseUserProfileProjection;
import dev.Voatix.entity.UserEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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
    Optional<Long> findIdByNickname(String nickname);

    @Query(value = """
            SELECT
                u.nickname as nickname,
                u.credentials.eMail as email,
                a.id as fileId
            FROM UserEntity u
            LEFT JOIN u.avatar a
            WHERE u.id = :userId
            """)
    Optional<ResponseUserProfileProjection> findUserInfoById(Long userId);

    @Modifying
    @Query(value = """
            UPDATE UserEntity u
            SET u.avatarId = :fileId
            WHERE u.id = :userId
        """)
    void setAvatar(Long userId, Long fileId);
}
