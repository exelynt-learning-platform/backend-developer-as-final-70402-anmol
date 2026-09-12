package com.anmol.bookingsystem.service;

import com.anmol.bookingsystem.dto.ReservationRequestDTO;
import com.anmol.bookingsystem.dto.ReservationResponseDTO;
import com.anmol.bookingsystem.entity.*;
import com.anmol.bookingsystem.exception.ResourceNotFoundException;
import com.anmol.bookingsystem.exception.UnauthorizedAccessException;
import com.anmol.bookingsystem.repository.ReservationRepository;
import com.anmol.bookingsystem.repository.ResourceRepository;
import com.anmol.bookingsystem.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;

    // ── Helper: get current user entity ──────────────────────────────────
    private User getCurrentUserEntity(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    // ── Helper: check ownership or admin ─────────────────────────────────
    private void assertOwnership(Reservation reservation, User currentUser) {
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        boolean isOwner = reservation.getUser().getId().equals(currentUser.getId());
        if (!isAdmin && !isOwner) {
            throw new UnauthorizedAccessException(
                    "You are not authorized to access this reservation");
        }
    }

    // ── Create ────────────────────────────────────────────────────────────
    @Transactional
    public ReservationResponseDTO createReservation(ReservationRequestDTO dto, String username) {

        // 1. Validate endTime > startTime
        if (!dto.getEndTime().isAfter(dto.getStartTime())) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        // 2. Load user from JWT — never from request body
        User user = getCurrentUserEntity(username);

        // 3. Load resource — proper 404 if not found
        Resource resource = resourceRepository.findById(dto.getResourceId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Resource not found with id: " + dto.getResourceId()));

        // 4. Check resource availability
        if (!Boolean.TRUE.equals(resource.getAvailable())) {
            throw new IllegalArgumentException(
                    "Resource is not available for booking");
        }

        // 5. Check for overlapping reservations
        List<Reservation> overlaps = reservationRepository.findOverlappingReservations(
                resource.getId(), dto.getStartTime(), dto.getEndTime());
        if (!overlaps.isEmpty()) {
            throw new IllegalArgumentException(
                    "Resource is already booked for the selected time range");
        }

        // 6. Create and save
        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setResource(resource);
        reservation.setStartTime(dto.getStartTime());
        reservation.setEndTime(dto.getEndTime());
        reservation.setPrice(dto.getPrice());
        reservation.setStatus(ReservationStatus.PENDING);

        return toDTO(reservationRepository.save(reservation));
    }

    // ── Get All (paginated, filtered) ─────────────────────────────────────
    @Transactional(readOnly = true)
    public Page<ReservationResponseDTO> getReservations(
            String username,
            ReservationStatus status,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            int page,
            int size,
            String sortBy,
            String sortDir) {

        // Validate minPrice <= maxPrice
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new IllegalArgumentException("minPrice must be less than or equal to maxPrice");
        }

        User currentUser = getCurrentUserEntity(username);
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;

        // Whitelist sortable fields to prevent DoS via expensive columns
        List<String> allowedSortFields = List.of("startTime", "endTime", "price", "status", "id");
        String safeSortBy = allowedSortFields.contains(sortBy) ? sortBy : "startTime";
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir)
                ? Sort.Direction.DESC : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, safeSortBy));

        Specification<Reservation> spec = buildSpec(
                isAdmin ? null : currentUser.getId(), status, minPrice, maxPrice);

        return reservationRepository.findAll(spec, pageable).map(this::toDTO);
    }

    // ── Get By Id ─────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public ReservationResponseDTO getReservationById(Long id, String username) {
        Reservation reservation = reservationRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Reservation not found with id: " + id));

        User currentUser = getCurrentUserEntity(username);
        assertOwnership(reservation, currentUser);

        return toDTO(reservation);
    }

    // ── Update Status ─────────────────────────────────────────────────────
    @Transactional
    public ReservationResponseDTO updateStatus(Long id, ReservationStatus status, String username) {
        Reservation reservation = reservationRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Reservation not found with id: " + id));

        User currentUser = getCurrentUserEntity(username);
        assertOwnership(reservation, currentUser);

        reservation.setStatus(status);
        return toDTO(reservationRepository.save(reservation));
    }

    // ── Delete ────────────────────────────────────────────────────────────
    @Transactional
    public void deleteReservation(Long id, String username) {
        Reservation reservation = reservationRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Reservation not found with id: " + id));

        User currentUser = getCurrentUserEntity(username);

        // Only ADMIN or owner can delete
        assertOwnership(reservation, currentUser);

        reservationRepository.deleteById(id);
    }

    // ── Specification Builder ─────────────────────────────────────────────
    private Specification<Reservation> buildSpec(
            Long userId,
            ReservationStatus status,
            BigDecimal minPrice,
            BigDecimal maxPrice) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Eagerly fetch user and resource to avoid N+1
            root.fetch("user");
            root.fetch("resource");

            if (userId != null) {
                predicates.add(cb.equal(root.get("user").get("id"), userId));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    // ── toDTO ─────────────────────────────────────────────────────────────
    private ReservationResponseDTO toDTO(Reservation r) {
        ReservationResponseDTO dto = new ReservationResponseDTO();
        dto.setId(r.getId());
        dto.setResourceId(r.getResource().getId());
        dto.setResourceName(r.getResource().getName());
        dto.setUserId(r.getUser().getId());
        dto.setUsername(r.getUser().getUsername());
        dto.setStartTime(r.getStartTime());
        dto.setEndTime(r.getEndTime());
        dto.setStatus(r.getStatus());
        dto.setPrice(r.getPrice());
        return dto;
    }
}