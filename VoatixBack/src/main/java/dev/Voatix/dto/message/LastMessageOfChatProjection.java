package dev.Voatix.dto.message;

public interface LastMessageOfChatProjection {
    Long getCompanionId();
    String getCompanionNickname();
    String getSenderNickname();
    String getText();
}
