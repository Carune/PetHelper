package com.pethelper.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Builder;
import lombok.AllArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class Member {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    private String password;
    
    private String name;
    
    private String profileImage;
    
    @Enumerated(EnumType.STRING)
    private AuthProvider authProvider;
    
    private String providerId;
    
    public enum AuthProvider {
        LOCAL, KAKAO, NAVER
    }
} 