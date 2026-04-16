package com.kaldi.dto;

import com.kaldi.entity.Chat;
import com.kaldi.enums.ChatStatus;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Compact chat representation used in operator dashboard lists.")
public class ChatSummaryDTO {

    @Schema(description = "Chat ID.", examples = {"42"})
    public Long idChat;

    @Schema(description = "Current chat status.", examples = {"WAITING"})
    public ChatStatus status;

    @Schema(description = "When the chat was created.", examples = {"2026-04-16T10:15:30"})
    public LocalDateTime createdAt;

    public ChatSummaryDTO(Chat chat) {
        this.idChat = chat.idChat;
        this.status = chat.status;
        this.createdAt = chat.createdAt;
    }
}
