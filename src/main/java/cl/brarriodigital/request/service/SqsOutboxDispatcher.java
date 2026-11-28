package cl.brarriodigital.request.service;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import cl.brarriodigital.request.model.RequestOutboxEvent;
import cl.brarriodigital.request.repository.RequestOutboxRepository;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

public class SqsOutboxDispatcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(SqsOutboxDispatcher.class);

    private final RequestOutboxRepository outboxRepository;
    private final SqsClient sqsClient;
    private final String queueUrl;

    public SqsOutboxDispatcher(RequestOutboxRepository outboxRepository, SqsClient sqsClient, String queueUrl) {
        this.outboxRepository = outboxRepository;
        this.sqsClient = sqsClient;
        this.queueUrl = queueUrl;
    }

    @Scheduled(fixedDelayString = "${app.events.sqs.poll-interval:1000}")
    public void dispatchPendingEvents() {
        var pendingEvents = outboxRepository
            .findTop20ByPublishedAtIsNullAndNextAttemptAtLessThanEqualOrderByCreatedAtAsc(Instant.now());
        for (var event : pendingEvents) {
            dispatch(event);
        }
    }

    private void dispatch(RequestOutboxEvent event) {
        try {
            sqsClient.sendMessage(SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .messageBody(event.getPayload())
                .build());
            event.setPublishedAt(Instant.now());
            event.setLastError(null);
            outboxRepository.save(event);
        } catch (SdkException exception) {
            scheduleRetry(event, exception);
        }
    }

    private void scheduleRetry(RequestOutboxEvent event, SdkException exception) {
        int attemptCount = event.getAttemptCount() + 1;
        long backoffSeconds = Math.min(1L << Math.min(attemptCount, 9), TimeUnit.MINUTES.toSeconds(5));
        event.setAttemptCount(attemptCount);
        event.setNextAttemptAt(Instant.now().plusSeconds(backoffSeconds));
        event.setLastError(exception.getClass().getSimpleName());
        outboxRepository.save(event);
        LOGGER.warn(
            "SQS publication failed for event {}; error type {}; retry scheduled",
            event.getEventId(),
            exception.getClass().getSimpleName()
        );
    }
}
