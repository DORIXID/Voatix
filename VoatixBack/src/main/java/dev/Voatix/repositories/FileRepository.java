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


    @Query(value = """
                        SELECT f
                            FROM FileEntity f
                            WHERE f.id IN :ids
            """)
    List<FileEntity> findById(
            @Param("ids") List<Long> ids
    );



    Optional<FileEntity> findById(Long id);



}
