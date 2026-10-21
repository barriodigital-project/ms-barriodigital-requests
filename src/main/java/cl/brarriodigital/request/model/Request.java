package cl.brarriodigital.request.model;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "requests",
    indexes = {
        @Index(name = "idx_requests_citizen_created", columnList = "citizen_id, created_at"),
        @Index(name = "idx_requests_status", columnList = "status"),
        @Index(name = "idx_requests_procedure_type", columnList = "procedure_type_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
public class Request {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(name = "tracking_code", nullable = false, unique = true, length = 40)
    private String trackingCode;

    @Column(name = "procedure_type_id", nullable = false)
    private UUID procedureTypeId;

    @Column(name = "citizen_id", nullable = false, length = 128)
    private String citizenId;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(nullable = false, length = 500)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private RequestStatus status;

    @Column(name = "assigned_crew_id")
    private UUID assignedCrewId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
