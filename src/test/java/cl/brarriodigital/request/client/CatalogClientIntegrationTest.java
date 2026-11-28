package cl.brarriodigital.request.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import cl.brarriodigital.request.dto.response.AvailabilityResponse;

@SpringBootTest(properties = {
    "eureka.client.enabled=false",
    "eureka.client.register-with-eureka=false",
    "eureka.client.fetch-registry=false",
    "spring.cloud.discovery.enabled=false"
})
class CatalogClientIntegrationTest {

    private static final UUID PROCEDURE_TYPE_ID = UUID.fromString("2cc1f37f-3f74-4411-9992-45d8537bb821");
    private static final AtomicInteger requestCount = new AtomicInteger();
    private static final AtomicReference<String> requestPath = new AtomicReference<>();
    private static HttpServer server;

    @Autowired
    private CatalogClient catalogClient;

    @DynamicPropertySource
    static void catalogServiceUrl(DynamicPropertyRegistry registry) {
        try {
            server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
            server.createContext("/api/v1/procedure-types", exchange -> {
                requestCount.incrementAndGet();
                requestPath.set(exchange.getRequestURI().getPath());
                byte[] response = """
                    {"procedureTypeId":"%s","dailyQuota":10,"availableQuota":4,"available":true}
                    """.formatted(PROCEDURE_TYPE_ID).getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.length);
                try (var output = exchange.getResponseBody()) {
                    output.write(response);
                }
            });
            server.start();
        } catch (IOException exception) {
            throw new IllegalStateException("Could not start Catalog test server", exception);
        }

        registry.add("app.services.catalog.base-url",
            () -> "http://localhost:" + server.getAddress().getPort());
    }

    @AfterAll
    static void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void callsCatalogDirectlyWithoutLoadBalancerResolution() {
        AvailabilityResponse availability = catalogClient.getAvailability(PROCEDURE_TYPE_ID);

        assertThat(availability).isEqualTo(new AvailabilityResponse(PROCEDURE_TYPE_ID, 10, 4, true));
        assertThat(requestCount).hasValue(1);
        assertThat(requestPath).hasValue(
            "/api/v1/procedure-types/" + PROCEDURE_TYPE_ID + "/availability");
    }
}
