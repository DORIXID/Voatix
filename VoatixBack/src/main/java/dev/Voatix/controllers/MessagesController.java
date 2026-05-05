package dev.Voatix.controllers;

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

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/messages")
public class MessagesController extends BaseController {

    final private MessageService messageService;

    @GetMapping("chats")
    public Page<MessageChatDTO> getLastMessagesOfChats(
            @RequestBody @Valid ChatsRequestDTO dto,
            Authentication auth
    ) {
        return messageService.getChats(dto, getUserId(auth));
    }

    @GetMapping("chat")
    public Page<MessageDTO> getChat(
            @RequestBody @Valid ChatRequestDTO dto,
            Authentication auth
    ) {
        return messageService.getChat(dto, getUserId(auth));
    }
}
