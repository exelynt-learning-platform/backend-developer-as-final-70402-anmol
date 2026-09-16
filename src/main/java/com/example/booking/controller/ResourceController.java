package com.example.booking.controller;

import com.example.booking.dto.ResourceDtos;
import com.example.booking.service.ResourceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/resources")
public class ResourceController {
    private final ResourceService resources;
    public ResourceController(ResourceService resources) { this.resources = resources; }
    @GetMapping public List<ResourceDtos.Response> all() { return resources.all(); }
    @GetMapping("/{id}") public ResourceDtos.Response get(@PathVariable Long id) { return resources.get(id); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("hasRole('ADMIN')") public ResourceDtos.Response create(@Valid @RequestBody ResourceDtos.Request request) { return resources.create(request); }
    @PutMapping("/{id}") @PreAuthorize("hasRole('ADMIN')") public ResourceDtos.Response update(@PathVariable Long id, @Valid @RequestBody ResourceDtos.Request request) { return resources.update(id, request); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) @PreAuthorize("hasRole('ADMIN')") public void delete(@PathVariable Long id) { resources.delete(id); }
}
