package dev.Voatix.mapper;

import dev.Voatix.dto.message.MessageChatDTO;
import dev.Voatix.dto.message.MessageDTO;
import dev.Voatix.dto.message.SendMessageDTO;
import dev.Voatix.dto.message.LastMessageOfChatProjection;
import dev.Voatix.dto.message.MessageUserOfChatProjection;
import dev.Voatix.entity.FileEntity;
import dev.Voatix.entity.MessageEntity;
import dev.Voatix.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface MessageMapper {

    default Page<MessageChatDTO> toPageDto(
            Page<MessageUserOfChatProjection> usersProj,
            List<LastMessageOfChatProjection> messagesProj,
            List<UserEntity> users) {

        Map<Long, LastMessageOfChatProjection> messagesMap = messagesProj.stream()
                .collect(Collectors.toMap(LastMessageOfChatProjection::getCompanionId, v -> v));

        Map<Long, UserEntity> usersMap = users.stream()
                .collect(Collectors.toMap(UserEntity::getId, v -> v));

        return usersProj.map(message -> toMessageDto(
                message,
                messagesMap.get(message.getUserId()),
                usersMap.get(message.getUserId())
        ));
    }

    @Mapping(target = "userNickname",
            expression = "java(messagesProj == null ? null : messagesProj.getCompanionNickname())")
    @Mapping(target = "senderNickname",
            expression = "java(messagesProj == null ? null : messagesProj.getSenderNickname())")
    @Mapping(target = "text",
            expression = "java(messagesProj == null ? null : messagesProj.getText())")
    @Mapping(target = "dateTime",
            expression = "java(usersProj == null ? null : usersProj.getDateTime())")
    @Mapping(target = "avatarId", source = "user.avatarId")
    MessageChatDTO toMessageDto(
            MessageUserOfChatProjection usersProj,
            LastMessageOfChatProjection messagesProj,
            UserEntity user
    );

    List<MessageDTO> toMessageDto(List<MessageEntity> messages);

    @Mapping(target = "sender", source = "sender.nickname")
    @Mapping(target = "files", source = "files")
    @Mapping(target = "receiver", source = "receiver.nickname")
    MessageDTO toMessageDto(MessageEntity message);

    List<Long> toDto(List<FileEntity> files);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "sender", ignore = true)
    @Mapping(target = "receiver", ignore = true)
    @Mapping(target = "text", source = "dto.text")
    @Mapping(target = "date", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "isRead", expression = "java(false)")
    @Mapping(target = "senderId", source = "sender")
    @Mapping(target = "receiverId", source = "receiver")
    @Mapping(target = "files", source = "files")
    MessageEntity toMessageEntity(SendMessageDTO dto,
                                  Long sender,
                                  Long receiver,
                                  List<FileEntity> files);

    default Long map(FileEntity file) {
        if (file == null) {
            return null;
        }
        return file.getId();
    }

}
