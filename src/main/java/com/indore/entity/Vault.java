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
public class Vault {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private String encryptionType;
    @Column(columnDefinition = "TEXT")
    private String masterKey;
    private String status;
    private String vaultCode;
    private LocalDateTime createdAt;
    private LocalDateTime lastAccessTime;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

}
