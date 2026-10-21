package cl.brarriodigital.request.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import cl.brarriodigital.request.client.CatalogClient;
import cl.brarriodigital.request.dto.request.CreateRequestRequest;
import cl.brarriodigital.request.dto.request.UpdateRequestStatusRequest;
import cl.brarriodigital.request.dto.response.AvailabilityResponse;
import cl.brarriodigital.request.exception.RequestConflictException;
import cl.brarriodigital.request.exception.ResourceNotFoundException;
import cl.brarriodigital.request.model.Request;
import cl.brarriodigital.request.model.RequestStatus;
import cl.brarriodigital.request.repository.RequestRepository;

@ExtendWith(MockitoExtension.class)
class RequestServiceTest {

    @Mock
    private RequestRepository repository;

    @Mock
    private CatalogClient catalogClient;

    @InjectMocks
    private RequestService service;

    @Test
    void createsRequestWhenCatalogHasAvailability() {
        UUID procedureTypeId = UUID.randomUUID();
        when(catalogClient.getAvailability(procedureTypeId))
            .thenReturn(new AvailabilityResponse(procedureTypeId, 10, 4, true));
        when(repository.save(any(Request.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.create(
            new CreateRequestRequest(procedureTypeId, "  Descripción  ", "  Dirección  "),
            "citizen-sub"
        );

        assertThat(response.trackingCode()).startsWith("REQ-");
        assertThat(response.procedureTypeId()).isEqualTo(procedureTypeId);
        assertThat(response.citizenId()).isEqualTo("citizen-sub");
        assertThat(response.description()).isEqualTo("Descripción");
        assertThat(response.address()).isEqualTo("Dirección");
        assertThat(response.status()).isEqualTo(RequestStatus.INGRESADO);
        verify(repository).save(any(Request.class));
    }

    @Test
    void rejectsCreationWhenCatalogHasNoAvailability() {
        UUID procedureTypeId = UUID.randomUUID();
        when(catalogClient.getAvailability(procedureTypeId))
            .thenReturn(new AvailabilityResponse(procedureTypeId, 10, 0, false));

        assertThatThrownBy(() -> service.create(
            new CreateRequestRequest(procedureTypeId, "Descripción", "Dirección"),
            "citizen-sub"
        )).isInstanceOf(RequestConflictException.class);

        verify(repository, never()).save(any(Request.class));
    }

    @Test
    void allowsValidStatusTransition() {
        UUID id = UUID.randomUUID();
        Request request = request(id, RequestStatus.INGRESADO);
        when(repository.findById(id)).thenReturn(Optional.of(request));
        when(repository.save(request)).thenReturn(request);

        var response = service.updateStatus(id, new UpdateRequestStatusRequest(RequestStatus.ADMITIDO));

        assertThat(response.status()).isEqualTo(RequestStatus.ADMITIDO);
    }

    @Test
    void rejectsInvalidStatusTransition() {
        UUID id = UUID.randomUUID();
        Request request = request(id, RequestStatus.INGRESADO);
        when(repository.findById(id)).thenReturn(Optional.of(request));

        assertThatThrownBy(() -> service.updateStatus(id, new UpdateRequestStatusRequest(RequestStatus.RESUELTO)))
            .isInstanceOf(RequestConflictException.class);

        verify(repository, never()).save(request);
    }

    @Test
    void returnsNotFoundForUnknownRequest() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(id))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    private Request request(UUID id, RequestStatus status) {
        var request = new Request();
        request.setId(id);
        request.setTrackingCode("REQ-" + id);
        request.setProcedureTypeId(UUID.randomUUID());
        request.setCitizenId("citizen-sub");
        request.setDescription("Descripción");
        request.setAddress("Dirección");
        request.setStatus(status);
        return request;
    }
}
