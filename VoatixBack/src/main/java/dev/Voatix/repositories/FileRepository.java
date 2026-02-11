package dev.Voatix.repositories;

import dev.Voatix.entity.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

    @Modifying
    @Query(value = """
                    DELETE FROM files
                    WHERE comment_id = :commentId
                    """, nativeQuery = true)
    void deleteByCommentId(Long commentId);

    @Modifying
    @Query(value = """
                    DELETE FROM files
                    WHERE idea_id = :ideaId
                    """, nativeQuery = true)
    void deleteByIdeaId(Long ideaId);

    @Modifying
    @Query(value = """
                    DELETE FROM files
                    WHERE comment_id IN (SELECT id FROM comments WHERE idea_id = :ideaId)
                    """, nativeQuery = true)
    void deleteByIdeaIdFromComments(Long ideaId);

    @Query(value = """
        SELECT f.id FROM files f
        WHERE f.key = :key
        """, nativeQuery = true)
    Optional<Long> findIdByKey(String key);


}
