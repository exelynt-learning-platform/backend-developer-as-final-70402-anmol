package com.anmol.bookingsystem.controller;

import com.anmol.bookingsystem.dto.ReservationRequestDTO;
import com.anmol.bookingsystem.dto.ReservationResponseDTO;
import com.anmol.bookingsystem.entity.ReservationStatus;
import com.anmol.bookingsystem.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
@Tag(name = "Reservations")
@SecurityRequirement(name = "bearerAuth")
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    @Operation(summary = "Create a reservation (USER)")
    public ResponseEntity<ReservationResponseDTO> createReservation(
            @Valid @RequestBody ReservationRequestDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reservationService.createReservation(dto, userDetails.getUsername()));
    }

    @GetMapping
    @Operation(summary = "List reservations with filtering and pagination")
    public ResponseEntity<Page<ReservationResponseDTO>> getReservations(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) ReservationStatus status,
            @RequestParam(required = false) java.math.BigDecimal minPrice,
            @RequestParam(required = false) java.math.BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "startTime") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        return ResponseEntity.ok(reservationService.getReservations(
                userDetails.getUsername(), status, minPrice, maxPrice,
                page, size, sortBy, sortDir));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get reservation by ID")
    public ResponseEntity<ReservationResponseDTO> getReservationById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                reservationService.getReservationById(id, userDetails.getUsername()));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update reservation status")
    public ResponseEntity<ReservationResponseDTO> updateStatus(
            @PathVariable Long id,
            @RequestParam ReservationStatus status,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                reservationService.updateStatus(id, status, userDetails.getUsername()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete reservation")
    public ResponseEntity<Void> deleteReservation(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        reservationService.deleteReservation(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}