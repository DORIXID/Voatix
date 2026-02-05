package dev.Voatix.repositories;

import dev.Voatix.entity.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FileRepository extends JpaRepository<FileEntity, Long> {


    @Query(value = """
                        SELECT *
                            FROM files
                            WHERE files.key IN :keys
            """, nativeQuery = true)
    List<FileEntity> findByFileKeys(
            @Param("keys") List<String> keys
    );

    @Query(value = """
    SELECT EXISTS (
        SELECT 1 FROM files
        WHERE key IN :keys
        AND uploader_id != :uploaderId
    )
    """, nativeQuery = true)
    boolean existsByOwner(
            @Param("keys") List<String> keys,
            @Param("uploaderId") Long uploaderId
    );

    Optional<FileEntity> findByKey(String key);
}
