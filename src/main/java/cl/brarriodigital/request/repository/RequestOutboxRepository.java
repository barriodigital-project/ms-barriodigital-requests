package cl.brarriodigital.request.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.brarriodigital.request.model.RequestOutboxEvent;

public interface RequestOutboxRepository extends JpaRepository<RequestOutboxEvent, UUID> {

    List<RequestOutboxEvent> findTop20ByPublishedAtIsNullAndNextAttemptAtLessThanEqualOrderByCreatedAtAsc(
        Instant now
    );
}
