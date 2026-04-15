package com.kaldi.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "operators")
public class Operator extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_operator")
    public Long idOperator;

    @Column(nullable = false, unique = true)
    public String username;

    @Column(name = "password_hash", nullable = false)
    public String passwordHash;

    @Column(name = "created_at")
    public LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }

    // Helper method to find operator by username (needed for login)
    public static Operator findByUsername(String username) {
        return find("username", username).firstResult();
    }
}