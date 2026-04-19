package com.indore.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "invite")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Invite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;
    private String role;
    private String scope;
    private Long vaultId;

    private String invitedBy;
    private String vaultName;

    // 🔥 FIXED PART
    @ElementCollection
    @CollectionTable(
            name = "invite_secret_ids",
            joinColumns = @JoinColumn(name = "invite_id")
    )
    @Column(name = "secret_ids")
    private List<Long> secretIds;

    private String status;
    private LocalDateTime createdAt;
}