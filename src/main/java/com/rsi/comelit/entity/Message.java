// src/main/java/com/rsi/comelit/entity/Message.java
package com.rsi.comelit.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "messages")
@Data
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne()
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne()
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    private String content;
    private Instant timestamp;
    @Column(name = "`read`")
    private boolean read;
}