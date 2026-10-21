package cl.brarriodigital.request.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import cl.brarriodigital.request.model.Request;
import cl.brarriodigital.request.model.RequestStatus;

@DataJpaTest
class RequestRepositoryTest {

    @Autowired
    private RequestRepository repository;

    @Test
    void filtersRequestsByStatusAndProcedureType() {
        UUID procedureTypeId = UUID.randomUUID();
        saveRequest("citizen-1", procedureTypeId, RequestStatus.INGRESADO);
        saveRequest("citizen-2", procedureTypeId, RequestStatus.RESUELTO);
        saveRequest("citizen-3", UUID.randomUUID(), RequestStatus.INGRESADO);

        var requests = repository.findAllByFilters(RequestStatus.INGRESADO, procedureTypeId);

        assertThat(requests).hasSize(1);
        assertThat(requests.getFirst().getStatus()).isEqualTo(RequestStatus.INGRESADO);
        assertThat(requests.getFirst().getCreatedAt()).isNotNull();
        assertThat(requests.getFirst().getUpdatedAt()).isNotNull();
    }

    @Test
    void listsRequestsForOnlyTheRequestedCitizen() {
        saveRequest("citizen-1", UUID.randomUUID(), RequestStatus.INGRESADO);
        saveRequest("citizen-2", UUID.randomUUID(), RequestStatus.ADMITIDO);

        assertThat(repository.findAllByCitizenIdOrderByCreatedAtDescIdDesc("citizen-1"))
            .hasSize(1)
            .allMatch(request -> request.getCitizenId().equals("citizen-1"));
    }

    private void saveRequest(String citizenId, UUID procedureTypeId, RequestStatus status) {
        var request = new Request();
        request.setTrackingCode("REQ-" + UUID.randomUUID().toString().replace("-", ""));
        request.setCitizenId(citizenId);
        request.setProcedureTypeId(procedureTypeId);
        request.setDescription("Descripción");
        request.setAddress("Dirección");
        request.setStatus(status);
        repository.save(request);
    }
}
