package com._cortex.url_management.model;

import java.time.Instant;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "urls", indexes = {
        @Index(name = "idx_urls_shortcode", columnList = "short_code"),
        @Index(name = "idx_urls_createdby", columnList = "created_by")
})
@Getter
@Setter
@ToString(exclude = "createdBy")
@NoArgsConstructor
@AllArgsConstructor
public class Url {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "short_code", nullable = false, unique = true)
    private String shortCode;

    @Column(name = "original_url", nullable = false, length = 2048)
    private String originalUrl;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "created_by", foreignKey = @ForeignKey(name = "fk_url_user"))
    private User createdBy;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "last_accessed_at")
    private Instant lastAccessedAt;

    @Column(name = "expire_at")
    private Instant expireAt;

    @Column(name = "hits", nullable = false)
    private Long hits = 0L;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        if (hits == null) {
            hits = 0L;
        }
    }
}