package cl.brarriodigital.request.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import cl.brarriodigital.request.dto.response.ApiErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({
        MethodArgumentNotValidException.class,
        HandlerMethodValidationException.class,
        MethodArgumentTypeMismatchException.class,
        HttpMessageNotReadableException.class,
        MissingRequestHeaderException.class,
        IllegalArgumentException.class
    })
    ResponseEntity<ApiErrorResponse> handleBadRequest(Exception exception) {
        return error(HttpStatus.BAD_REQUEST, "Solicitud inválida");
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<ApiErrorResponse> handleNotFound(ResourceNotFoundException exception) {
        return error(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(RequestConflictException.class)
    ResponseEntity<ApiErrorResponse> handleConflict(RequestConflictException exception) {
        return error(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(CatalogServiceUnavailableException.class)
    ResponseEntity<ApiErrorResponse> handleCatalogUnavailable(CatalogServiceUnavailableException exception) {
        return error(HttpStatus.SERVICE_UNAVAILABLE, "Servicio Catalog no disponible");
    }

    private ResponseEntity<ApiErrorResponse> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(new ApiErrorResponse(status.value(), message));
    }
}
