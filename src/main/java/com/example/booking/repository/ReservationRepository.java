package com.example.booking.repository;

import com.example.booking.model.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.*;
import java.math.BigDecimal;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    @Query("select r from Reservation r where (:username is null or r.user.username = :username) and (:status is null or r.status = :status) and (:minPrice is null or r.price >= :minPrice) and (:maxPrice is null or r.price <= :maxPrice)")
    Page<Reservation> search(@Param("username") String username, @Param("status") ReservationStatus status,
            @Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice, Pageable pageable);

    @Query("select count(r) > 0 from Reservation r where r.resource.id = :resourceId and r.status <> :cancelled and r.startTime < :endTime and r.endTime > :startTime and (:excludedId is null or r.id <> :excludedId)")
    boolean existsOverlapping(@Param("resourceId") Long resourceId, @Param("startTime") java.time.Instant startTime,
            @Param("endTime") java.time.Instant endTime, @Param("cancelled") ReservationStatus cancelled,
            @Param("excludedId") Long excludedId);
}
