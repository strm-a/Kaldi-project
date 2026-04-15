package com.kaldi.dto;

import com.kaldi.entity.Chat;
import com.kaldi.enums.ChatStatus;

import java.time.LocalDateTime;

public class ChatSummaryDTO {
    public Long idChat;
    public ChatStatus status;
    public LocalDateTime createdAt;

    public ChatSummaryDTO(Chat chat) {
        this.idChat = chat.idChat;
        this.status = chat.status;
        this.createdAt = chat.createdAt;
    }
}