package com.anmol.bookingsystem.repository;

import com.anmol.bookingsystem.entity.Reservation;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long>,
        JpaSpecificationExecutor<Reservation> {

    @EntityGraph(attributePaths = {"user", "resource"})
    @Query("SELECT r FROM Reservation r WHERE r.id = :id")
    Optional<Reservation> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT r FROM Reservation r WHERE r.resource.id = :resourceId " +
           "AND r.status != 'CANCELLED' " +
           "AND r.startTime < :endTime AND r.endTime > :startTime")
    List<Reservation> findOverlappingReservations(
            @Param("resourceId") Long resourceId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
}