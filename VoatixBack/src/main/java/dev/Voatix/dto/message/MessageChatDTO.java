package dev.Voatix.dto.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageChatDTO {
    Long userId;
    String userNickname;
    Long avatarId;
    String senderNickname;
    String text;
    LocalDateTime dateTime;
}
