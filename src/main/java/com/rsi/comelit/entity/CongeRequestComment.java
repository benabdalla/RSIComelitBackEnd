package com.rsi.comelit.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
public class CongeRequestComment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private CongeRequest congeRequest;

    @ManyToOne
    private User author;

    private String comment;

    private LocalDateTime createdAt;

}

