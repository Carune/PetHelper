package com.pethelper.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Builder;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    private String password;
    
    private String name;
    
    private String imageUrl;
    
    @Enumerated(EnumType.STRING)
    private AuthProvider provider;
    
    private String providerId;
    
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    @Builder
    public User(String email, String password, String name, String imageUrl, 
               AuthProvider provider, String providerId) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.imageUrl = imageUrl;
        this.provider = provider;
        this.providerId = providerId;
    }
    
    public enum AuthProvider {
        LOCAL,
        KAKAO,
        NAVER,
        GOOGLE
    }
    
    public void updateOAuth2Info(AuthProvider provider, String providerId) {
        this.provider = provider;
        this.providerId = providerId;
    }
} 