package com.example.booking.service;

import com.example.booking.dto.ResourceDtos;
import com.example.booking.model.Resource;
import com.example.booking.repository.ResourceRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ResourceService {
    private final ResourceRepository resources;
    public ResourceService(ResourceRepository resources) { this.resources = resources; }
    public List<ResourceDtos.Response> all() { return resources.findAll().stream().map(this::response).toList(); }
    public ResourceDtos.Response get(Long id) { return response(find(id)); }
    public ResourceDtos.Response create(ResourceDtos.Request request) { return response(resources.save(new Resource(request.name(), request.description(), request.available()))); }
    public ResourceDtos.Response update(Long id, ResourceDtos.Request request) { Resource resource = find(id); resource.update(request.name(), request.description(), request.available()); return response(resources.save(resource)); }
    public void delete(Long id) { resources.delete(find(id)); }
    public Resource find(Long id) { return resources.findById(id).orElseThrow(() -> new EntityNotFoundException("Resource not found: " + id)); }
    private ResourceDtos.Response response(Resource r) { return new ResourceDtos.Response(r.getId(), r.getName(), r.getDescription(), r.isAvailable()); }
}
