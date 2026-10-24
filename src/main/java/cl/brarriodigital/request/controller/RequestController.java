package cl.brarriodigital.request.controller;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import cl.brarriodigital.request.dto.request.CreateRequestRequest;
import cl.brarriodigital.request.dto.request.UpdateRequestStatusRequest;
import cl.brarriodigital.request.dto.response.RequestResponse;
import cl.brarriodigital.request.model.RequestStatus;
import cl.brarriodigital.request.service.RequestService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/requests")
@RequiredArgsConstructor
@Validated
public class RequestController {

    private final RequestService service;

    @GetMapping
    public List<RequestResponse> findAll(
        @RequestParam(required = false) RequestStatus status,
        @RequestParam(required = false) UUID procedureTypeId
    ) {
        return service.findAll(status, procedureTypeId);
    }

    @GetMapping("/me")
    public List<RequestResponse> findMine(
        @RequestHeader("X-Citizen-Id") @NotBlank String citizenId
    ) {
        return service.findMine(citizenId);
    }

    @GetMapping("/{id}")
    public RequestResponse findById(@PathVariable UUID id) {
        return service.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RequestResponse create(
        @Valid @RequestBody CreateRequestRequest request,
        @RequestHeader("X-Citizen-Id") @NotBlank String citizenId
    ) {
        return service.create(request, citizenId);
    }

    @PatchMapping("/{id}/status")
    public RequestResponse updateStatus(
        @PathVariable UUID id,
        @Valid @RequestBody UpdateRequestStatusRequest request
    ) {
        return service.updateStatus(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }
}
