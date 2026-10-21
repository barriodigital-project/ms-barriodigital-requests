package cl.brarriodigital.request.exception;

public class CatalogServiceUnavailableException extends RuntimeException {

    public CatalogServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }

    public CatalogServiceUnavailableException(String message) {
        super(message);
    }
}
