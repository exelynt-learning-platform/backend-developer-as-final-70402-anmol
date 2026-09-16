package com.example.booking.dto;

import jakarta.validation.constraints.NotBlank;

public final class ResourceDtos {
    private ResourceDtos() { }
    public record Request(@NotBlank String name, @NotBlank String description, boolean available) { }
    public record Response(Long id, String name, String description, boolean available) { }
}
