package dev.Voatix.dto.message;

import java.time.LocalDateTime;

public interface MessageUserOfChatProjection {
    Long getUserId();
    LocalDateTime getDateTime();
}
