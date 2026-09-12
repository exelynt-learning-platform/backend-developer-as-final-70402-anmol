package com.anmol.bookingsystem.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "resources")
@Getter
@Setter
public class Resource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private Boolean available = true;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Resource)) return false;
        Resource resource = (Resource) o;
        return id != null && id.equals(resource.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}