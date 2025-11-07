package com.pricehawk.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * User
 *
 * Minimal user table for authentication/notifications.
 * In early stage we store only email (unique), displayName and when created.
 * Later you can add:
 *  - password (hashed) / OAuth fields
 *  - notification preferences (email/sms)
 *  - role/permissions
 */
@Entity
@Table(name = "app_user",
        indexes = {
                @Index(name = "idx_user_email", columnList = "email")
        })
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Unique email (primary method of contact). */
    @Column(name = "email", nullable = false, unique = true, length = 256)
    private String email;

    /** Optional display name. */
    @Column(name = "display_name", length = 128)
    private String displayName;

    /** When the user registered. */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public User() {}

    public User(String email, String displayName) {
        this.email = email;
        this.displayName = displayName;
        this.createdAt = LocalDateTime.now();
    }

    // getters & setters
    public Long getId() { return id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
