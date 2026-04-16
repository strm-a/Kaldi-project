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

    @Column(name = "created_at")
    public LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }

    // Case-insensitive — Keycloak lowercases usernames, but the seed uses camelCase
    // (e.g. token carries "aliceoperator" while the row is "aliceOperator").
    public static Operator findByUsername(String username) {
        return find("lower(username) = ?1", username.toLowerCase()).firstResult();
    }
}