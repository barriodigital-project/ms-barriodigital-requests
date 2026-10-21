package cl.brarriodigital.request.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import cl.brarriodigital.request.model.Request;
import cl.brarriodigital.request.model.RequestStatus;

public interface RequestRepository extends JpaRepository<Request, UUID> {

    @Query("""
        select request from Request request
        where (:status is null or request.status = :status)
          and (:procedureTypeId is null or request.procedureTypeId = :procedureTypeId)
        order by request.createdAt desc, request.id desc
        """)
    List<Request> findAllByFilters(
        @Param("status") RequestStatus status,
        @Param("procedureTypeId") UUID procedureTypeId
    );

    List<Request> findAllByCitizenIdOrderByCreatedAtDescIdDesc(String citizenId);
}
