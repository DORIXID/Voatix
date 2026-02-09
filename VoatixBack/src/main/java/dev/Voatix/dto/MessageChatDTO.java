package dev.Voatix.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageChatDTO {
    String userNickname;
    String avatarKey;
    String senderNickname;
    String text;
    LocalDateTime dateTime;
    Long unreadCount;
}
