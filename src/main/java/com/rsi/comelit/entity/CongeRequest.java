package com.rsi.comelit.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Data
public class CongeRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private User requester;
    @ManyToOne
    private User validator;
    @Enumerated(EnumType.STRING)
    private CongeRequestStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
    @OneToMany(mappedBy = "congeRequest", cascade = CascadeType.ALL)
    private List<CongeRequestComment> comments;
}

