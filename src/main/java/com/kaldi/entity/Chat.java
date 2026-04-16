package com.kaldi.entity;

import com.kaldi.enums.ChatStatus;
import com.kaldi.enums.Room;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "chats")
public class Chat extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_chat")
    public Long idChat;

    @Version
    @Column(nullable = false)
    @ColumnDefault("0")
    public Long version;

    @ManyToOne
    @JoinColumn(name = "id_user", nullable = false)
    public User user;

    @ManyToOne
    @JoinColumn(name = "id_operator")
    public Operator operator; // NULL until acquired

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public Room room;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public ChatStatus status = ChatStatus.WAITING;

    @Column(name = "created_at")
    public LocalDateTime createdAt;

    @Column(name = "acquired_at")
    public LocalDateTime acquiredAt; // NULL until operator acquires it

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }

    // Get all waiting chats (operator dashboard)
    public static List<Chat> findWaiting() {
        return list("status", ChatStatus.WAITING);
    }

    // Get all active chats for a specific operator
    public static List<Chat> findActiveByOperator(Operator operator) {
        return list("operator = ?1 and status = ?2", operator, ChatStatus.ACTIVE);
    }

    // Get all chats (waiting + active) for operator dashboard
    public static List<Chat> findAllOpen() {
        return list("status in ?1", 
            List.of(ChatStatus.WAITING, ChatStatus.ACTIVE));
    }
}
