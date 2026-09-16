package com.example.booking.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "reservations")
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Resource resource;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private AppUser user;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;
    @Column(nullable = false)
    private Instant startTime;
    @Column(nullable = false)
    private Instant endTime;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status = ReservationStatus.PENDING;

    protected Reservation() {
    }

    public Reservation(Resource resource, AppUser user, BigDecimal price, Instant startTime, Instant endTime,
            ReservationStatus status) {
        this.resource = resource;
        this.user = user;
        this.price = price;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public Resource getResource() {
        return resource;
    }

    public AppUser getUser() {
        return user;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void update(Resource resource, BigDecimal price, Instant startTime, Instant endTime,
            ReservationStatus status) {
        this.resource = resource;
        this.price = price;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
    }
}
