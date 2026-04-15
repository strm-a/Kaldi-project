package com.kaldi.dto;

import com.kaldi.entity.Chat;
import com.kaldi.entity.Message;
import com.kaldi.enums.ChatStatus;
import com.kaldi.enums.Room;

import java.time.LocalDateTime;
import java.util.List;

public class ChatDetailDTO {
    public Long idChat;
    public ChatStatus status;
    public Room room;
    public String username;
    public LocalDateTime createdAt;
    public LocalDateTime acquiredAt;
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