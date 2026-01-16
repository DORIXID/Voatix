package dev.Voatix.repositories;

import dev.Voatix.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByNickname(String userName);

    Optional<UserEntity> findById(Long id);
}
