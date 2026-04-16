package com.kaldi.dto;

import com.kaldi.entity.Chat;
import com.kaldi.entity.Message;
import com.kaldi.enums.ChatStatus;
import com.kaldi.enums.Room;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Full chat detail including user info, room, and message history.")
public class ChatDetailDTO {

    @Schema(description = "Chat ID.", examples = {"42"})
    public Long idChat;

    @Schema(description = "Current chat status.", examples = {"ACTIVE"})
    public ChatStatus status;

    @Schema(description = "Support room / category.", examples = {"TEHNIKA"})
    public Room room;

    @Schema(description = "Username of the user who started the chat.", examples = {"client1"})
    public String username;

    @Schema(description = "When the chat was created.", examples = {"2026-04-16T10:15:30"})
    public LocalDateTime createdAt;

    @Schema(description = "When an operator acquired the chat (null while WAITING).", examples = {"2026-04-16T10:16:02"}, nullable = true)
    public LocalDateTime acquiredAt;

    @Schema(description = "Messages in the chat, ordered oldest to newest.")
    public List<Message> messages;

    public ChatDetailDTO(Chat chat, List<Message> messages) {
        this.idChat = chat.idChat;
        this.status = chat.status;
        this.room = chat.room;
        this.username = chat.user.username;
        this.createdAt = chat.createdAt;
        this.acquiredAt = chat.acquiredAt;
        this.messages = messages;
    }
}
