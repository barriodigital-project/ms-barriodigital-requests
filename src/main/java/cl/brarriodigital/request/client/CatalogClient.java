package cl.brarriodigital.request.client;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.ResourceAccessException;

import cl.brarriodigital.request.dto.response.AvailabilityResponse;
import cl.brarriodigital.request.exception.CatalogServiceUnavailableException;
import cl.brarriodigital.request.exception.ResourceNotFoundException;

@Component
public class CatalogClient {

    private final RestClient restClient;

    public CatalogClient(
        @Qualifier("loadBalancedRestClientBuilder") RestClient.Builder builder,
        @Value("${app.services.catalog.base-url}") String baseUrl
    ) {
        this.restClient = builder.clone().baseUrl(baseUrl).build();
    }

    public AvailabilityResponse getAvailability(UUID procedureTypeId) {
        try {
            AvailabilityResponse response = restClient.get()
                .uri("/api/v1/procedure-types/{id}/availability", procedureTypeId)
                .retrieve()
                .body(AvailabilityResponse.class);
            if (response == null) {
                throw new CatalogServiceUnavailableException("Catalog devolvió una respuesta vacía");
            }
            return response;
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 404) {
                throw new ResourceNotFoundException("Tipo de trámite no encontrado: " + procedureTypeId);
            }
            throw new CatalogServiceUnavailableException("No fue posible validar el tipo de trámite", exception);
        } catch (ResourceAccessException exception) {
            throw new CatalogServiceUnavailableException("Servicio Catalog no disponible", exception);
        } catch (IllegalStateException exception) {
            if (exception.getMessage() != null
                && exception.getMessage().startsWith("No instances available for ")) {
                throw new CatalogServiceUnavailableException("Servicio Catalog no disponible", exception);
            }
            throw exception;
        }
    }
}
