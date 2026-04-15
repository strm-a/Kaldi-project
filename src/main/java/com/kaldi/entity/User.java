package com.kaldi.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User extends PanacheEntityBase { // PanacheEntityBase - gives findById, listAll, etc.

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_user")
    public Long idUser;

    @Column(nullable = false, unique = true)
    public String username;

    @Column(name = "created_at")
    public LocalDateTime createdAt;

    @PrePersist // automatically sets createdAt to now whenever a new User is saved
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }

}