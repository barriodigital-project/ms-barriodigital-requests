package cl.brarriodigital.request.service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.brarriodigital.request.client.CatalogClient;
import cl.brarriodigital.request.dto.request.CreateRequestRequest;
import cl.brarriodigital.request.dto.request.UpdateRequestStatusRequest;
import cl.brarriodigital.request.dto.response.RequestResponse;
import cl.brarriodigital.request.exception.RequestConflictException;
import cl.brarriodigital.request.exception.ResourceNotFoundException;
import cl.brarriodigital.request.model.Request;
import cl.brarriodigital.request.model.RequestStatus;
import cl.brarriodigital.request.repository.RequestRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RequestService {

    private static final Set<RequestStatus> INITIAL_STATUS_TRANSITIONS =
        Set.of(RequestStatus.ADMITIDO, RequestStatus.RECHAZADO);
    private static final Set<RequestStatus> ADMITTED_STATUS_TRANSITIONS =
        Set.of(RequestStatus.EN_GESTION, RequestStatus.RECHAZADO);
    private static final Set<RequestStatus> IN_PROGRESS_STATUS_TRANSITIONS =
        Set.of(RequestStatus.EN_TERRENO, RequestStatus.RESUELTO, RequestStatus.RECHAZADO);
    private static final Set<RequestStatus> ON_SITE_STATUS_TRANSITIONS =
        Set.of(RequestStatus.EN_GESTION, RequestStatus.RESUELTO);

    private final RequestRepository repository;
    private final CatalogClient catalogClient;

    public List<RequestResponse> findAll(RequestStatus status, UUID procedureTypeId) {
        return repository.findAllByFilters(status, procedureTypeId).stream()
            .map(this::toResponse)
            .toList();
    }

    public List<RequestResponse> findMine(String citizenId) {
        return repository.findAllByCitizenIdOrderByCreatedAtDescIdDesc(requireCitizenId(citizenId)).stream()
            .map(this::toResponse)
            .toList();
    }

    public RequestResponse findById(UUID id) {
        return toResponse(getRequest(id));
    }

    @Transactional
    public RequestResponse create(CreateRequestRequest request, String citizenId) {
        String validatedCitizenId = requireCitizenId(citizenId);
        var availability = catalogClient.getAvailability(request.procedureTypeId());
        if (!availability.available()) {
            throw new RequestConflictException("No hay cupos disponibles para el tipo de trámite");
        }

        var serviceRequest = new Request();
        serviceRequest.setTrackingCode("REQ-" + UUID.randomUUID().toString().replace("-", ""));
        serviceRequest.setProcedureTypeId(request.procedureTypeId());
        serviceRequest.setCitizenId(validatedCitizenId);
        serviceRequest.setDescription(request.description().trim());
        serviceRequest.setAddress(request.address().trim());
        serviceRequest.setStatus(RequestStatus.INGRESADO);
        return toResponse(repository.save(serviceRequest));
    }

    @Transactional
    public RequestResponse updateStatus(UUID id, UpdateRequestStatusRequest request) {
        var serviceRequest = getRequest(id);
        if (!isAllowedTransition(serviceRequest.getStatus(), request.status())) {
            throw new RequestConflictException(
                "No se permite cambiar el estado de " + serviceRequest.getStatus() + " a " + request.status()
            );
        }

        serviceRequest.setStatus(request.status());
        return toResponse(repository.save(serviceRequest));
    }

    @Transactional
    public void delete(UUID id) {
        var serviceRequest = getRequest(id);
        repository.delete(serviceRequest);
    }

    private boolean isAllowedTransition(RequestStatus current, RequestStatus next) {
        return switch (current) {
            case INGRESADO -> INITIAL_STATUS_TRANSITIONS.contains(next);
            case ADMITIDO -> ADMITTED_STATUS_TRANSITIONS.contains(next);
            case EN_GESTION -> IN_PROGRESS_STATUS_TRANSITIONS.contains(next);
            case EN_TERRENO -> ON_SITE_STATUS_TRANSITIONS.contains(next);
            case RESUELTO, RECHAZADO -> false;
        };
    }

    private Request getRequest(UUID id) {
        return repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Trámite no encontrado: " + id));
    }

    private String requireCitizenId(String citizenId) {
        if (citizenId == null || citizenId.isBlank()) {
            throw new IllegalArgumentException("El identificador del ciudadano es obligatorio");
        }
        return citizenId.trim();
    }

    private RequestResponse toResponse(Request serviceRequest) {
        return new RequestResponse(
            serviceRequest.getId(),
            serviceRequest.getTrackingCode(),
            serviceRequest.getProcedureTypeId(),
            serviceRequest.getCitizenId(),
            serviceRequest.getDescription(),
            serviceRequest.getAddress(),
            serviceRequest.getStatus(),
            serviceRequest.getAssignedCrewId(),
            serviceRequest.getCreatedAt(),
            serviceRequest.getUpdatedAt()
        );
    }
}
