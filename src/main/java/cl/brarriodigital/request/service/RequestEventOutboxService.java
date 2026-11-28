package cl.brarriodigital.request.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;

import cl.brarriodigital.request.dto.event.RequestBusinessEvent;
import cl.brarriodigital.request.model.Request;
import cl.brarriodigital.request.repository.RequestOutboxRepository;
import lombok.RequiredArgsConstructor;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class RequestEventOutboxService {

    private final RequestOutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public void enqueueCreated(Request request) {
        enqueue("REQUEST_CREATED", request);
    }

    public void enqueueStatusChanged(Request request) {
        enqueue("REQUEST_STATUS_CHANGED", request);
    }

    private void enqueue(String eventType, Request request) {
        UUID eventId = UUID.randomUUID();
        var event = new RequestBusinessEvent(
            1,
            eventId,
            eventType,
            Instant.now(),
            request.getId(),
            request.getProcedureTypeId(),
            request.getStatus()
        );

        String payload;
        try {
            payload = objectMapper.writeValueAsString(event);
        } catch (JacksonException exception) {
            throw new IllegalStateException("Could not serialize request event", exception);
        }

        var outboxEvent = new cl.brarriodigital.request.model.RequestOutboxEvent();
        outboxEvent.setEventId(eventId);
        outboxEvent.setRequestId(request.getId());
        outboxEvent.setPayload(payload);
        outboxRepository.save(outboxEvent);
    }
}
