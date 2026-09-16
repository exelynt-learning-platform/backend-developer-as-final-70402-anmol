package com.example.booking.dto;

import com.example.booking.model.ReservationStatus;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;

public final class ReservationDtos {
    private ReservationDtos() { }
    public record Request(@NotNull Long resourceId, @NotNull @DecimalMin("0.01") BigDecimal price, @NotNull Instant startTime, @NotNull Instant endTime, ReservationStatus status) { }
    public record Response(Long id, Long resourceId, String resourceName, String username, BigDecimal price, Instant startTime, Instant endTime, ReservationStatus status) { }
}
