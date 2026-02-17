package dev.Voatix.service;

import dev.Voatix.dto.MessageChatDTO;
import dev.Voatix.dto.MessageDTO;
import dev.Voatix.dto.SendMessageDTO;
import dev.Voatix.dto.projection.LastMessageOfChatProjection;
import dev.Voatix.dto.projection.MessageUserOfChatProjection;
import dev.Voatix.entity.FileEntity;
import dev.Voatix.entity.MessageEntity;
import dev.Voatix.entity.UserEntity;
import dev.Voatix.mapper.MessageMapper;
import dev.Voatix.repositories.FileRepository;
import dev.Voatix.repositories.MessageRepository;
import dev.Voatix.repositories.UserRepository;
import dev.Voatix.utils.exceptions.commonException.UserUnauthorizedException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final MessageMapper messageMapper;
    private final FileRepository fileRepository;

    public Page<MessageChatDTO> getChats(Integer page, Integer limit, Principal principal) {
        Long userId = userRepository.findIdByNickname(principal.getName())
                .orElseThrow(() -> new UserUnauthorizedException(principal.getName()));
        Pageable pageParam = PageRequest.of(page, limit);
        Page<MessageUserOfChatProjection> userOfChatProjections = messageRepository.findChatsByUserId(userId, pageParam);

        List<Long> ids = userOfChatProjections.getContent().stream().map(MessageUserOfChatProjection::getUserId).toList();

        List<UserEntity> users = userRepository.findUsersByUserIds(ids);
        List<LastMessageOfChatProjection> lastMessageOfChatProjections = messageRepository.findMessagesOfChatsByUserIds(ids, userId);

        return messageMapper.toPageDto(userOfChatProjections, lastMessageOfChatProjections, users);
    }

    public List<MessageDTO> getChat(String companionNickname, Principal principal) {
        Long userId = userRepository.findIdByNickname(principal.getName())
                .orElseThrow(() -> new UserUnauthorizedException(principal.getName()));
        Long companionId = userRepository.findIdByNickname(companionNickname)
                .orElseThrow(() -> new UserUnauthorizedException(principal.getName()));
        //Помечаем сообщения от собеседника как прочитанные
        messageRepository.markMessagesAsRead(userId, companionId);
        List<MessageEntity> messages = messageRepository.getMessages(userId, companionId);
        return messageMapper.toMessageDto(messages);
    }

    public void markMessagesAsRead(String companionNickname, Principal principal) {
        Long userId = userRepository.findIdByNickname(principal.getName())
                .orElseThrow(() -> new UserUnauthorizedException(principal.getName()));
        Long companionId = userRepository.findIdByNickname(companionNickname)
                .orElseThrow(() -> new UserUnauthorizedException(principal.getName()));
        messageRepository.markMessagesAsRead(userId, companionId);
    }

    public MessageDTO saveMessage(SendMessageDTO dto, Principal principal) {
        Long userId = userRepository.findIdByNickname(principal.getName())
                .orElseThrow(() -> new UserUnauthorizedException(principal.getName()));
        Long companionId = userRepository.findIdByNickname(dto.getReceiver())
                .orElseThrow(() -> new UserUnauthorizedException(principal.getName()));
        List<FileEntity> files = fileRepository.findByFileKeys(dto.getFiles());
        MessageEntity message = messageMapper.toMessageEntity(dto, userId, companionId, files);
        messageRepository.save(message);
        return messageMapper.toMessageDto(message);
    }
}
