package cl.brarriodigital.request.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import cl.brarriodigital.request.model.RequestOutboxEvent;
import cl.brarriodigital.request.repository.RequestOutboxRepository;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SqsException;

@ExtendWith(MockitoExtension.class)
class SqsOutboxDispatcherTest {

    @Mock
    private RequestOutboxRepository outboxRepository;

    @Mock
    private SqsClient sqsClient;

    private SqsOutboxDispatcher dispatcher;
    private RequestOutboxEvent event;

    @BeforeEach
    void setUp() {
        dispatcher = new SqsOutboxDispatcher(outboxRepository, sqsClient, "https://sqs.us-east-1.amazonaws.com/queue");
        event = new RequestOutboxEvent();
        event.setEventId(UUID.randomUUID());
        event.setRequestId(UUID.randomUUID());
        event.setPayload("{\"eventType\":\"REQUEST_CREATED\"}");
        event.setNextAttemptAt(Instant.now());
        when(outboxRepository
            .findTop20ByPublishedAtIsNullAndNextAttemptAtLessThanEqualOrderByCreatedAtAsc(any()))
            .thenReturn(List.of(event));
    }

    @Test
    void marksEventsPublishedAfterSuccessfulSend() {
        dispatcher.dispatchPendingEvents();

        assertThat(event.getPublishedAt()).isNotNull();
        verify(sqsClient).sendMessage(any(SendMessageRequest.class));
        verify(outboxRepository).save(event);
    }

    @Test
    void schedulesRetryAfterSqsFailure() {
        when(sqsClient.sendMessage(any(SendMessageRequest.class)))
            .thenThrow(SqsException.builder().message("temporarily unavailable").build());

        dispatcher.dispatchPendingEvents();

        assertThat(event.getPublishedAt()).isNull();
        assertThat(event.getAttemptCount()).isEqualTo(1);
        assertThat(event.getNextAttemptAt()).isAfter(Instant.now());
        assertThat(event.getLastError()).isEqualTo("SqsException");
        verify(outboxRepository).save(event);
    }
}
