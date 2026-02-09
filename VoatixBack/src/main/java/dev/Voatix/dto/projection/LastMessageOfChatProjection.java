package dev.Voatix.dto.projection;

import java.time.LocalDateTime;

public interface LastMessageOfChatProjection {
    Long getCompanionId();
    String getCompanionNickname();
    String getSenderNickname();
    String getText();
}
