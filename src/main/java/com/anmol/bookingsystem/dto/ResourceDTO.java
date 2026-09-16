package com.anmol.bookingsystem.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResourceDTO {

    private Long id;

    @NotBlank(message = "Resource name is required")
    private String name;

    private String description;

    // available is NOT accepted from create request — system managed
    private Boolean available;
}