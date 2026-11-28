package cl.brarriodigital.request.dto.event;

import java.time.Instant;
import java.util.UUID;

import cl.brarriodigital.request.model.RequestStatus;

public record RequestBusinessEvent(
    int schemaVersion,
    UUID eventId,
    String eventType,
    Instant occurredAt,
    UUID requestId,
    UUID procedureTypeId,
    RequestStatus status
) {
}
