package com.airlock.backend.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity @Table(name = "rooms") @Getter @NoArgsConstructor @AllArgsConstructor
public class Room {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user1_id", nullable = false)
    private User user1;

    @ManyToOne
    @JoinColumn(name = "user2_id", nullable = false)
    private User user2;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public Room(User user1, User user2) {
        this.user1 = user1;
        this.user2 = user2;
    }
}
