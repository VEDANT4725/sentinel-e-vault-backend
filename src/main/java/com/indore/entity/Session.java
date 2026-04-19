package com.indore.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id ;
    private String token;
    private String device;
    private String ipAddress;
    @Column(nullable = false)
    private boolean active;
    private LocalDateTime loginTime;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

}
