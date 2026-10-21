package cl.brarriodigital.request.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateRequestRequest(
    @NotNull UUID procedureTypeId,
    @NotBlank @Size(max = 2000) String description,
    @NotBlank @Size(max = 500) String address
) {
}
