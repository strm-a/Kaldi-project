package com.kaldi.entity;

import com.kaldi.enums.SenderType;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
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

    // Get all messages for a chat, ordered oldest to newest
    public static List<Message> findByChat(Chat chat) {
    return find("FROM Message m WHERE m.chat = ?1 ORDER BY m.timeSent ASC", chat).list();
}
}