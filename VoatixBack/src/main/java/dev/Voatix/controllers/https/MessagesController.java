package dev.Voatix.controllers.https;

import dev.Voatix.dto.MessageChatDTO;
import dev.Voatix.dto.MessageDTO;
import dev.Voatix.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("api/messages")
public class MessagesController {

    @Autowired
    private MessageService messageService;

    @GetMapping("chats")
    public Page<MessageChatDTO> getLastMessagesOfChats(
            @RequestParam() Integer page,
            @RequestParam(defaultValue = "12") Integer limit,
            Principal principal
    ) {
        return messageService.getChats(page, limit, principal);
    }

    @GetMapping("chat/{companionNickname}")
    public List<MessageDTO> getChat(
            @PathVariable("companionNickname") String companionNickname,
            Principal principal) {
        return messageService.getChat(companionNickname, principal);
    }
}
