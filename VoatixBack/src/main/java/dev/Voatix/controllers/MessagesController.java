package dev.Voatix.controllers;

import dev.Voatix.configuration.security.CustomUserDetails;
import dev.Voatix.dto.message.ChatRequestDTO;
import dev.Voatix.dto.message.ChatsRequestDTO;
import dev.Voatix.dto.message.MessageChatDTO;
import dev.Voatix.dto.message.MessageDTO;
import dev.Voatix.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/messages")
public class MessagesController {

    final private MessageService messageService;

    @GetMapping("chats")
    public Page<MessageChatDTO> getLastMessagesOfChats(
            @RequestBody @Valid ChatsRequestDTO dto,
            Authentication auth
    ) {
        return messageService.getChats(dto, getUserId(auth));
    }

    @GetMapping("chat")
    public List<MessageDTO> getChat(
            @RequestParam @Valid ChatRequestDTO dto,
            Authentication auth
    ) {
        return messageService.getChat(dto, getUserId(auth));
    }

    Long getUserId(Authentication auth) {
        CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();
        return user.getId();
    }
}
