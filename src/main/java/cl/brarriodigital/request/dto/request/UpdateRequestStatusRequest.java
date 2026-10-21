package cl.brarriodigital.request.dto.request;

import cl.brarriodigital.request.model.RequestStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateRequestStatusRequest(@NotNull RequestStatus status) {
}
