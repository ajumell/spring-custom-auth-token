package com.xeoscript.modules.customauthtoken.jpa.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.time.LocalDateTime;

@Entity
@Table(name = "TOKEN_SESSION", indexes = {
        @Index(name = "IDX_TOKEN_SESSION_TOKEN", columnList = "TOKEN_VALUE"),
        @Index(name = "IDX_TOKEN_SESSION_CREATED", columnList = "CREATED_AT")
}, uniqueConstraints = {
        @UniqueConstraint(name = "UC_TOKEN_SESSION_KEY", columnNames = {"TOKEN_VALUE", "SESSION_KEY"})
})
@Getter
@Setter
public class TokenSessionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "TOKEN_VALUE", nullable = false, length = 512)
    private String tokenValue;

    @Column(name = "SESSION_KEY", nullable = false, length = 255)
    private String sessionKey;

    @Column(name = "SESSION_VALUE", nullable = false, length = 4000)
    private String sessionValue;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

