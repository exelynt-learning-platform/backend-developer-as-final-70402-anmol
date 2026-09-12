package com.anmol.bookingsystem.dto;

import com.anmol.bookingsystem.entity.ReservationStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ReservationResponseDTO {
    private Long id;
    private Long resourceId;
    private String resourceName;
    private Long userId;
    private String username;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private ReservationStatus status;
    private BigDecimal price;
}
