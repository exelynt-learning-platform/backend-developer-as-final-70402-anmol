package com.anmol.bookingsystem.service;

import com.anmol.bookingsystem.dto.ResourceDTO;
import com.anmol.bookingsystem.entity.Resource;
import com.anmol.bookingsystem.exception.ResourceNotFoundException;
import com.anmol.bookingsystem.repository.ResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResourceService {

    private final ResourceRepository resourceRepository;

    @Transactional(readOnly = true)
    public List<ResourceDTO> getAllResources() {
        return resourceRepository.findAll()
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ResourceDTO getResourceById(Long id) {
        return toDTO(findOrThrow(id));
    }

    @Transactional
    public ResourceDTO createResource(ResourceDTO dto) {
        Resource resource = new Resource();
        resource.setName(dto.getName());
        resource.setDescription(dto.getDescription());
        // available is system-managed: always true on creation
        resource.setAvailable(true);
        return toDTO(resourceRepository.save(resource));
    }

    @Transactional
    public ResourceDTO updateResource(Long id, ResourceDTO dto) {
        Resource resource = findOrThrow(id);
        resource.setName(dto.getName());
        resource.setDescription(dto.getDescription());
        if (dto.getAvailable() != null) {
            resource.setAvailable(dto.getAvailable());
        }
        return toDTO(resourceRepository.save(resource));
    }

    @Transactional
    public void deleteResource(Long id) {
        findOrThrow(id);
        resourceRepository.deleteById(id);
    }

    private Resource findOrThrow(Long id) {
        return resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Resource not found with id: " + id));
    }

    private ResourceDTO toDTO(Resource resource) {
        ResourceDTO dto = new ResourceDTO();
        dto.setId(resource.getId());
        dto.setName(resource.getName());
        dto.setDescription(resource.getDescription());
        dto.setAvailable(resource.getAvailable());
        return dto;
    }
}