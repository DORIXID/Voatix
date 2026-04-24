package dev.Voatix.repositories;

import dev.Voatix.entity.FileEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FileRepository extends JpaRepository<FileEntity, Long> {


    @EntityGraph(attributePaths = {})
    @Query(value = """
                        SELECT f
                            FROM FileEntity f
                            WHERE f.id IN :ids
            """)
    List<FileEntity> findById(
            @Param("ids") List<Long> ids
    );

    @Query(value = """
    SELECT EXISTS (
        SELECT 1 FROM files
        WHERE id IN :fileIds
        AND uploader_id != :uploaderId
    )
    """, nativeQuery = true)
    boolean existsByOwner(
            @Param("keys") List<Long> fileIds,
            @Param("uploaderId") Long uploaderId
    );

    Optional<FileEntity> findById(Long id);

    @Modifying
    @Query(value = """
                    DELETE FROM files
                    WHERE comment_id IN (SELECT id FROM comments WHERE idea_id = :ideaId)
                    """, nativeQuery = true)
    void deleteByIdeaIdFromComments(Long ideaId);


}
