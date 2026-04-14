package t03.datasource.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "session")
public class SessionEntity {

    @Id
    @Column(name = "token", nullable = false, unique = true)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "human_id", nullable = false)
    private HumanEntity human;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "last_accessed_at", nullable = false)
    private LocalDateTime lastAccessedAt;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    public SessionEntity(HumanEntity humanEntity, String ipAddress, String userAgent) {
        this.id = UUID.randomUUID();
        this.human = humanEntity;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.createdAt = LocalDateTime.now();
        touch();
    }

    public void touch() {
        this.lastAccessedAt = LocalDateTime.now();
        this.expiresAt = newExpiresDate();
    }

    public boolean expired() { return !notExpired(); }
    public boolean notExpired() {
        return LocalDateTime.now().isBefore(expiresAt);
    }

    private LocalDateTime newExpiresDate() {
        return LocalDateTime.now().plusHours(7);
    }

}
