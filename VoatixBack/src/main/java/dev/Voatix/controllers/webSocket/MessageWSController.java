package dev.Voatix.controllers.webSocket;

import dev.Voatix.dto.MessageDTO;
import dev.Voatix.dto.SendMessageDTO;
import dev.Voatix.service.MessageService;
import lombok.Locked;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class MessageWSController {

    private final MessageService messageService;
    private final SimpMessagingTemplate messaging;

    @MessageMapping("/messages.send")
    public void sendMessage(SendMessageDTO dto, Principal principal) {

        MessageDTO saved = messageService.saveMessage(dto, principal);

        messaging.convertAndSendToUser(
                dto.getReceiver(),
                "/queue/chat." + principal.getName(),
                saved
        );

        messaging.convertAndSendToUser(
                principal.getName(),
                "/queue/chat." + dto.getReceiver(),
                saved
        );

        messaging.convertAndSendToUser(
                dto.getReceiver(),
                "/queue/chats",
                saved
        );
    }

    @MessageMapping("/messages.read")
    public void markAsRead(MessageDTO dto, Principal principal) {

        messageService.markMessagesAsRead(
                dto.getReceiver(),
                principal
        );

        // уведомляем собеседника
        messaging.convertAndSendToUser(
                dto.getReceiver(),
                "/queue/read",
                "read"
        );
    }
}

