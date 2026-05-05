package dev.Voatix.service;

import dev.Voatix.dto.message.*;
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

    public Page<MessageChatDTO> getChats(ChatsRequestDTO dto, Long userId) {
        Pageable pageParam = PageRequest.of(dto.getPage(), dto.getLimit());
        Page<MessageUserOfChatProjection> userOfChatProjections = messageRepository.findChatsByUserId(userId, pageParam);

        List<Long> ids = userOfChatProjections.getContent().stream().map(MessageUserOfChatProjection::getUserId).toList();

        List<UserEntity> users = userRepository.findUsersByUserIds(ids);
        List<LastMessageOfChatProjection> lastMessageOfChatProjections = messageRepository.findMessagesOfChatsByUserIds(ids, userId);

        return messageMapper.toPageDto(userOfChatProjections, lastMessageOfChatProjections, users);
    }

    public Page<MessageDTO> getChat(ChatRequestDTO dto, Long userId) {
        Pageable pageParam = PageRequest.of(dto.getPage(), dto.getLimit());
        Page<MessageEntity> messages = messageRepository.getMessages(userId, dto.getCompanionId(), pageParam);
        return messages.map(messageMapper::toMessageDto);
    }

    public MessageDTO saveMessage(SendMessageDTO dto, Long userId) {
        List<FileEntity> files = fileRepository.findById(dto.getFiles());
        MessageEntity message = messageMapper.toMessageEntity(dto, userId, dto.getReceiverId(), files);
        messageRepository.save(message);
        return messageMapper.toMessageDto(message);
    }
}
