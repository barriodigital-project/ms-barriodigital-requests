package cl.brarriodigital.request.dto.response;

import java.time.Instant;
import java.util.UUID;

import cl.brarriodigital.request.model.RequestStatus;

public record RequestResponse(
    UUID id,
    String trackingCode,
    UUID procedureTypeId,
    String citizenId,
    String description,
    String address,
    RequestStatus status,
    UUID assignedCrewId,
    Instant createdAt,
    Instant updatedAt
) {
}
