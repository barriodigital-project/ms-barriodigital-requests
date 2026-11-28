package cl.brarriodigital.request;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import cl.brarriodigital.request.model.Request;
import cl.brarriodigital.request.model.RequestStatus;
import cl.brarriodigital.request.repository.RequestOutboxRepository;
import cl.brarriodigital.request.service.RequestEventOutboxService;

@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:requests;DB_CLOSE_DELAY=-1",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.jpa.hibernate.ddl-auto=create-drop",
		"eureka.client.enabled=false",
		"eureka.client.register-with-eureka=false",
		"eureka.client.fetch-registry=false",
		"spring.cloud.discovery.enabled=false"
})
class RequestMsApplicationTests {

	@Autowired
	private RequestEventOutboxService eventOutboxService;

	@Autowired
	private RequestOutboxRepository outboxRepository;

	@Test
	void contextLoads() {
	}

	@Test
	void persistsVersionedRequestEventsInTheOutbox() {
		var request = new Request();
		request.setId(UUID.randomUUID());
		request.setProcedureTypeId(UUID.randomUUID());
		request.setStatus(RequestStatus.INGRESADO);
		request.setUpdatedAt(Instant.parse("2026-03-01T10:00:00Z"));

		eventOutboxService.enqueueCreated(request);

		var outboxEvent = outboxRepository.findAll().getFirst();
		assertThat(outboxEvent.getRequestId()).isEqualTo(request.getId());
		assertThat(outboxEvent.getPayload())
			.contains("\"schemaVersion\":1")
			.contains("\"eventType\":\"REQUEST_CREATED\"")
			.contains("\"requestId\":\"" + request.getId() + "\"");
	}

}
