package com.indore.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Column(nullable = false,unique = true)
    private String email;
    private String password;
    private String profileImage;
    private boolean enabled=false; // email verify ke baad true hojayega

    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;
}
