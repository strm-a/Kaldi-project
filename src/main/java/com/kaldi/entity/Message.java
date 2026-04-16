package com.kaldi.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kaldi.enums.SenderType;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "messages")
public class Message extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_message")
    public Long idMessage;

    @ManyToOne
    @JoinColumn(name = "id_chat", nullable = false)
    @JsonIgnore
    @Schema(hidden = true)
    public Chat chat;

    @Enumerated(EnumType.STRING)
    @Column(name = "sender_type", nullable = false)
    public SenderType senderType;

    @Column(name = "sender_id", nullable = false)
    public Long senderId;

    @Column(nullable = false, columnDefinition = "TEXT")
    public String content;

    @Column(name = "time_sent")
    public LocalDateTime timeSent;

    @PrePersist
    public void prePersist() {
        timeSent = LocalDateTime.now();
    }

    @JsonProperty("chatId")
    @Schema(description = "ID of the chat this message belongs to.", examples = {"42"})
    public Long getChatId() {
        return chat != null ? chat.idChat : null;
    }

    // Get all messages for a chat, ordered oldest to newest
    public static List<Message> findByChat(Chat chat) {
        return find("FROM Message m WHERE m.chat = ?1 ORDER BY m.timeSent ASC", chat).list();
    }
}
