package com.indore.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Secret {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Column(columnDefinition = "TEXT")
    private String encryptedValue;
    private String type;
    private LocalDateTime createdAt;
    private LocalDateTime expiryTime;

    @ManyToOne
    @JoinColumn(name = "vault_id")
    private Vault vault;
}
