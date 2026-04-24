package com.pricehawk.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Basic user record used across the system.
 *
 * Current scope:
 * - identification (email)
 * - lightweight profile (displayName, phone)
 * - notification preferences
 *
 * Designed to be extended later (auth providers, roles, etc.)
 * without forcing early complexity into the schema.
 */
@Entity
@Table(
        name = "app_user",
        indexes = {
                // email is used frequently for lookup/login → index helps
                @Index(name = "idx_user_email", columnList = "email")
        }
)
public class User
{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Primary identifier for the user.
     * Enforced unique to avoid duplicate accounts.
     */
    @Column(name = "email", nullable = false, unique = true, length = 256)
    private String email;

    // Optional display name (UI friendly, not used for identity)
    @Column(name = "display_name", length = 128)
    private String displayName;

    /**
     * Optional phone number.
     * Not mandatory because not all users will opt for SMS notifications.
     */
    @Column(name = "phone", length = 15)
    private String phone;

    /**
     * Notification preference.
     * Kept as String for flexibility (instead of enum) during early stage.
     * Example values: "email", "sms", "both"
     */
    @Column(name = "notification_pref", length = 32)
    private String notificationPref;

    /**
     * Basic account state.
     * Useful for soft-blocking users without deleting data.
     * Example: "active", "blocked"
     */
    @Column(name = "status", length = 16)
    private String status;

    /**
     * Creation timestamp.
     * Set at application level instead of DB default for consistency.
     */
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public User()
    {
    }

    /**
     * Primary constructor used during registration.
     * Keeps initialization logic centralized.
     */
    public User(String email,
                String displayName,
                String phone,
                String notificationPref,
                String status)
    {
        this.email = email;
        this.displayName = displayName;
        this.phone = phone;
        this.notificationPref = notificationPref;
        this.status = status;

        // capturing creation time explicitly
        this.createdAt = Instant.now();
    }

    // --- Getters / Setters ---

    public Long getId()
    {
        return id;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    public void setDisplayName(String displayName)
    {
        this.displayName = displayName;
    }

    public String getPhone()
    {
        return phone;
    }

    public void setPhone(String phone)
    {
        this.phone = phone;
    }

    public String getNotificationPref()
    {
        return notificationPref;
    }

    public void setNotificationPref(String notificationPref)
    {
        this.notificationPref = notificationPref;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public Instant getCreatedAt()
    {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt)
    {
        this.createdAt = createdAt;
    }
}