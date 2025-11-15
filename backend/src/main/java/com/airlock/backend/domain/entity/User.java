package com.airlock.backend.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity @Table(name = "users")
public class User {

    @Getter @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Getter @Setter @Column(unique = true, nullable = false, length = 190)
    private String username;

    @Getter @Setter @Column(length = 190)
    private String displayName;

    @Setter
    private Boolean active;

    @Getter @Column(updatable = false, nullable = false)
    private Instant createdAt = Instant.now();

    public User() {}

    public User(String username, String displayName) {
        this.username = username;
        this.displayName = displayName;
    }

    public boolean isActive() { return active; }
}