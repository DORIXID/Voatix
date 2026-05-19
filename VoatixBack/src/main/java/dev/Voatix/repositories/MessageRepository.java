package dev.Voatix.repositories;

import dev.Voatix.dto.message.LastMessageOfChatProjection;
import dev.Voatix.dto.message.MessageUserOfChatProjection;
import dev.Voatix.entity.MessageEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<MessageEntity, Long> {


    @Query(value = """
            SELECT
                    CASE
                        WHEN m.sender_user_id = :userId THEN m.receiver_user_id
                        ELSE m.sender_user_id
                    END AS user_id,
            
                    MAX(m.date) AS date_time,
            
                    COUNT(*) FILTER (
                        WHERE m.receiver_user_id = :userId
                          AND m.is_read = false
                    ) AS unread_count
            
                FROM messages m
                WHERE m.sender_user_id = :userId
                   OR m.receiver_user_id = :userId
            
                GROUP BY user_id
                ORDER BY unread_count DESC, date_time DESC
            """, nativeQuery = true)
    Page<MessageUserOfChatProjection> findChatsByUserId(
            @Param("userId") Long userId,
            Pageable pageable
    );

    @Query(value = """
                SELECT DISTINCT ON (
                    LEAST(m.sender_user_id, m.receiver_user_id),
                    GREATEST(m.sender_user_id, m.receiver_user_id)
                )
                    CASE
                        WHEN m.sender_user_id = :userId THEN m.receiver_user_id
                        ELSE m.sender_user_id
                    END AS companion_id,
                    CASE
                        WHEN m.sender_user_id = :userId THEN ur.nickname
                        ELSE us.nickname
                    END AS companion_nickname,
                	us.nickname AS "sender_nickname",
                    m.text
                FROM messages m
                JOIN users ur ON ur.id = m.receiver_user_id
                JOIN users us ON us.id = m.sender_user_id
                WHERE (
                        (m.sender_user_id = :userId AND m.receiver_user_id IN :userIds)
                     OR (m.receiver_user_id = :userId AND m.sender_user_id IN :userIds)
                )
                ORDER BY
                    LEAST(m.sender_user_id, m.receiver_user_id),
                    GREATEST(m.sender_user_id, m.receiver_user_id),
                    m.date DESC;
            """, nativeQuery = true)
    List<LastMessageOfChatProjection> findMessagesOfChatsByUserIds(
            @Param("userIds") List<Long> userIds,
            @Param("userId") Long userId
    );

    @Query("""
            SELECT m
            FROM MessageEntity m
            LEFT JOIN fetch m.files
            WHERE (m.sender.id = :userId and m.receiver.id = :companionId) or
                  (m.sender.id = :companionId and m.receiver.id = :userId)
            order by m.date DESC
            """)
    Page<MessageEntity> getMessages(
            @Param("userId") Long userId,
            @Param("companionId") Long companionId,
            Pageable pageable
    );

    @Modifying
    @Query("""
        UPDATE MessageEntity m
        SET m.isRead = true
        WHERE m.receiver.id = :userId
          AND m.sender.id = :companionId
        """)
    void markMessagesAsRead(
            @Param("userId") Long userId,
            @Param("companionId") Long companionId
    );

}
