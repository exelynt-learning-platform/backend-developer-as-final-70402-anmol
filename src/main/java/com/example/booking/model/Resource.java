package com.example.booking.model;

import jakarta.persistence.*;

@Entity
@Table(name = "resources")
public class Resource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false, length = 2000)
    private String description;
    @Column(nullable = false)
    private boolean available = true;

    protected Resource() {
    }

    public Resource(String name, String description, boolean available) {
        this.name = name;
        this.description = description;
        this.available = available;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean isAvailable() {
        return available;
    }

    public void update(String name, String description, boolean available) {
        this.name = name;
        this.description = description;
        this.available = available;
    }
}
