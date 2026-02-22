package com.xeoscript.modules.customauthtoken.jpa.entity;

import com.xeoscript.modules.customauthtoken.model.enums.TokenStatus;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "CUSTOM_AUTH_TOKEN", indexes = {
        @Index(name = "IDX_TOKEN_VALUE", columnList = "TOKEN_VALUE", unique = true),
        @Index(name = "IDX_PARAM_TYPE", columnList = "PARAMETER_VALUE, TOKEN_TYPE"),
        @Index(name = "IDX_STATUS", columnList = "STATUS"),
        @Index(name = "IDX_EXPIRY_TIME", columnList = "EXPIRY_TIME")
})
@Getter
@Setter
public class TokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "TOKEN_VALUE", nullable = false, length = 512, unique = true)
    private String tokenValue;

    @Column(name = "PARAMETER_VALUE", nullable = false, length = 512)
    private String parameterValue;

    @Column(name = "TOKEN_TYPE", length = 100)
    private String tokenType;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 20)
    private TokenStatus status;

    @Column(name = "EXPIRY_TIME", nullable = false)
    private LocalDateTime expiryTime;

    @Column(name = "USAGE_LIMIT")
    private Integer usageLimit;

    @Column(name = "USAGE_COUNT", nullable = false)
    private int usageCount;

    @Column(name = "HASHING_MODE", nullable = false, length = 20)
    private String hashingMode;

    @Column(name = "METADATA", length = 2000)
    private String metadata;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "USED_AT")
    private LocalDateTime usedAt;

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
