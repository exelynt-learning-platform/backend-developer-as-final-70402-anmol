package com.example.booking.service;

import com.example.booking.dto.ReservationDtos;
import com.example.booking.model.*;
import com.example.booking.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.Instant;

@Service
public class ReservationService {
    private final ReservationRepository reservations;
    private final ResourceRepository resources;
    private final UserRepository users;

    public ReservationService(ReservationRepository reservations, ResourceRepository resources,
            UserRepository users) {
        this.reservations = reservations;
        this.resources = resources;
        this.users = users;
    }

    public Page<ReservationDtos.Response> search(String username, ReservationStatus status, BigDecimal minPrice,
            BigDecimal maxPrice, int page, int size, String sort) {
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0)
            throw new IllegalArgumentException("minPrice cannot exceed maxPrice");
        Sort ordering = sort == null || sort.isBlank() ? Sort.by("id").descending() : parseSort(sort);
        return reservations.search(username, status, minPrice, maxPrice, PageRequest.of(page, size, ordering))
                .map(this::response);
    }

    @Transactional
    public ReservationDtos.Response create(String username, ReservationDtos.Request request, boolean admin) {
        validateTimes(request.startTime(), request.endTime());
        AppUser user = users.findByUsername(username).orElseThrow(() -> new EntityNotFoundException("User not found"));
        ReservationStatus status = request.status() == null ? ReservationStatus.PENDING : request.status();
        if (!admin && status != ReservationStatus.PENDING)
            throw new IllegalArgumentException("Users may only create PENDING reservations");
        Resource resource = resources.findByIdForUpdate(request.resourceId())
                .orElseThrow(() -> new EntityNotFoundException("Resource not found: " + request.resourceId()));
        ensureBookable(resource, request.startTime(), request.endTime(), null);
        return response(reservations.save(new Reservation(resource, user,
                request.price(), request.startTime(), request.endTime(), status)));
    }

    @Transactional
    public ReservationDtos.Response update(Long id, ReservationDtos.Request request) {
        validateTimes(request.startTime(), request.endTime());
        Reservation reservation = find(id);
        Resource resource = resources.findByIdForUpdate(request.resourceId())
                .orElseThrow(() -> new EntityNotFoundException("Resource not found: " + request.resourceId()));
        ensureBookable(resource, request.startTime(), request.endTime(), id);
        reservation.update(resource, request.price(), request.startTime(),
                request.endTime(), request.status() == null ? reservation.getStatus() : request.status());
        return response(reservations.save(reservation));
    }

    @Transactional
    public void cancel(String username, Long id) {
        Reservation reservation = find(id);
        if (!reservation.getUser().getUsername().equals(username))
            throw new EntityNotFoundException("Reservation not found: " + id);
        if (reservation.getStatus() != ReservationStatus.CANCELLED) {
            reservation.update(reservation.getResource(), reservation.getPrice(), reservation.getStartTime(),
                    reservation.getEndTime(), ReservationStatus.CANCELLED);
            reservations.save(reservation);
        }
    }

    public void delete(Long id) {
        reservations.delete(find(id));
    }

    public Reservation find(Long id) {
        return reservations.findById(id).orElseThrow(() -> new EntityNotFoundException("Reservation not found: " + id));
    }

    private void validateTimes(Instant start, Instant end) {
        if (!end.isAfter(start))
            throw new IllegalArgumentException("endTime must be after startTime");
    }

    private void ensureBookable(Resource resource, Instant start, Instant end, Long excludedId) {
        if (!resource.isAvailable())
            throw new IllegalArgumentException("Resource is not available");
        if (reservations.existsOverlapping(resource.getId(), start, end, ReservationStatus.CANCELLED, excludedId))
            throw new IllegalArgumentException("Resource is already reserved for the requested time");
    }

    private Sort parseSort(String sort) {
        String[] parts = sort.split(",");
        if (parts.length > 2 || !parts[0].matches("id|price|startTime|endTime|status"))
            throw new IllegalArgumentException("sort must use id, price, startTime, endTime, or status");
        return Sort.by(
                parts.length == 2 && "desc".equalsIgnoreCase(parts[1]) ? Sort.Direction.DESC : Sort.Direction.ASC,
                parts[0]);
    }

    private ReservationDtos.Response response(Reservation r) {
        return new ReservationDtos.Response(r.getId(), r.getResource().getId(), r.getResource().getName(),
                r.getUser().getUsername(), r.getPrice(), r.getStartTime(), r.getEndTime(), r.getStatus());
    }
}
