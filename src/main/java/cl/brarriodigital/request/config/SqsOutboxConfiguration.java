package cl.brarriodigital.request.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import cl.brarriodigital.request.repository.RequestOutboxRepository;
import cl.brarriodigital.request.service.SqsOutboxDispatcher;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "app.events.sqs.enabled", havingValue = "true")
public class SqsOutboxConfiguration {

    @Bean
    SqsClient sqsClient(@Value("${app.events.sqs.region}") String region) {
        return SqsClient.builder()
            .region(Region.of(region))
            .build();
    }

    @Bean
    SqsOutboxDispatcher sqsOutboxDispatcher(
        RequestOutboxRepository outboxRepository,
        SqsClient sqsClient,
        @Value("${app.events.sqs.queue-url}") String queueUrl
    ) {
        if (queueUrl.isBlank()) {
            throw new IllegalArgumentException("SQS queue URL is required when SQS is enabled");
        }
        return new SqsOutboxDispatcher(outboxRepository, sqsClient, queueUrl);
    }
}
