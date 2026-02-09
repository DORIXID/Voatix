package dev.Voatix.dto.projection;

import java.time.LocalDateTime;

public interface MessageUserOfChatProjection {
    Long getUserId();
    LocalDateTime getDateTime();
    Long getUnreadCount();
}
