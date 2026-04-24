package dev.Voatix.controllers.webSocket;

import dev.Voatix.configuration.security.CustomUserDetails;
import dev.Voatix.dto.message.MessageDTO;
import dev.Voatix.dto.message.SendMessageDTO;
import dev.Voatix.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class MessageWSController {

    private final MessageService messageService;
    private final SimpMessagingTemplate messaging;

    @MessageMapping("/messages.send")
    public void sendMessage(SendMessageDTO dto, Authentication auth) {

        Long userId = getUserId(auth);

        MessageDTO saved = messageService.saveMessage(dto, userId);

        messaging.convertAndSendToUser(
                dto.getReceiverId().toString(),
                "/queue/chat." + userId,
                saved
        );

        messaging.convertAndSendToUser(
                userId.toString(),
                "/queue/chat." + dto.getReceiverId(),
                saved
        );

        messaging.convertAndSendToUser(
                dto.getReceiverId().toString(),
                "/queue/chats",
                saved
        );
    }

    Long getUserId(Authentication auth) {
        CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();
        return user.getId();
    }
}

