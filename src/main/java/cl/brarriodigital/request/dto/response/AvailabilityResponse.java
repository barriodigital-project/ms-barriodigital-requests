package cl.brarriodigital.request.dto.response;

import java.util.UUID;

public record AvailabilityResponse(
    UUID procedureTypeId,
    int dailyQuota,
    int availableQuota,
    boolean available
) {
}
