package com.anmol.bookingsystem.controller;

import com.anmol.bookingsystem.dto.ResourceDTO;
import com.anmol.bookingsystem.service.ResourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/resources")
@RequiredArgsConstructor
@Tag(name = "Resources")
@SecurityRequirement(name = "bearerAuth")
public class ResourceController {

    private final ResourceService resourceService;

    @GetMapping
    @Operation(summary = "Get all resources (ADMIN + USER)")
    public ResponseEntity<List<ResourceDTO>> getAllResources() {
        return ResponseEntity.ok(resourceService.getAllResources());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get resource by ID (ADMIN + USER)")
    public ResponseEntity<ResourceDTO> getResourceById(@PathVariable Long id) {
        return ResponseEntity.ok(resourceService.getResourceById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create resource (ADMIN only)")
    public ResponseEntity<ResourceDTO> createResource(@Valid @RequestBody ResourceDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(resourceService.createResource(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update resource (ADMIN only)")
    public ResponseEntity<ResourceDTO> updateResource(
            @PathVariable Long id, @Valid @RequestBody ResourceDTO dto) {
        return ResponseEntity.ok(resourceService.updateResource(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete resource (ADMIN only)")
    public ResponseEntity<Void> deleteResource(@PathVariable Long id) {
        resourceService.deleteResource(id);
        return ResponseEntity.noContent().build();
    }
}