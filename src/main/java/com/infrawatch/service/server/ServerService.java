package com.infrawatch.service.server;

import com.infrawatch.dto.request.ServerCreateRequest;
import com.infrawatch.exception.BusinessValidationException;
import com.infrawatch.exception.ResourceNotFoundException;
import com.infrawatch.model.server.Server;
import com.infrawatch.model.server.enums.Environment;
import com.infrawatch.model.server.enums.ServerStatus;
import com.infrawatch.repository.server.ServerRepository;
import com.infrawatch.service.auth.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class ServerService {

    private final ServerRepository serverRepository;
    private final AuditService auditService;

    public Page<Server> findAll(Pageable pageable) {
        return serverRepository.findAll(pageable);
    }

    public Server findById(UUID id) {
        return serverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Server", id));
    }

    public Page<Server> findByStatus(ServerStatus status, Pageable pageable) {
        return serverRepository.findByStatus(status, pageable);
    }

    public Page<Server> findByEnvironment(Environment environment, Pageable pageable) {
        return serverRepository.findByEnvironment(environment, pageable);
    }

    public Page<Server> search(String hostname, Pageable pageable) {
        return serverRepository.findByHostnameContainingIgnoreCase(hostname, pageable);
    }

    public Server create(ServerCreateRequest request) {
        if (serverRepository.existsByHostname(request.getHostname())) {
            throw new BusinessValidationException("Server hostname already exists: " + request.getHostname());
        }

        Server server = Server.builder()
                .hostname(request.getHostname())
                .ipAddress(request.getIpAddress())
                .os(request.getOs())
                .cpuCores(request.getCpuCores())
                .ramGb(request.getRamGb())
                .diskGb(request.getDiskGb())
                .location(request.getLocation())
                .status(request.getStatus())
                .environment(request.getEnvironment())
                .description(request.getDescription())
                .build();

        server = serverRepository.save(server);
        auditService.log("CREATE", "Server", server.getId(), "Created server: " + server.getHostname());
        return server;
    }

    public Server update(UUID id, ServerCreateRequest request) {
        Server server = findById(id);

        if (!server.getHostname().equals(request.getHostname())
                && serverRepository.existsByHostname(request.getHostname())) {
            throw new BusinessValidationException("Server hostname already exists: " + request.getHostname());
        }

        server.setHostname(request.getHostname());
        server.setIpAddress(request.getIpAddress());
        server.setOs(request.getOs());
        server.setCpuCores(request.getCpuCores());
        server.setRamGb(request.getRamGb());
        server.setDiskGb(request.getDiskGb());
        server.setLocation(request.getLocation());
        server.setStatus(request.getStatus());
        server.setEnvironment(request.getEnvironment());
        server.setDescription(request.getDescription());

        server = serverRepository.save(server);
        auditService.log("UPDATE", "Server", server.getId(), "Updated server: " + server.getHostname());
        return server;
    }

    public void delete(UUID id) {
        Server server = findById(id);
        serverRepository.delete(server);
        auditService.log("DELETE", "Server", id, "Deleted server: " + server.getHostname());
    }

    public long countByStatus(ServerStatus status) {
        return serverRepository.countByStatus(status);
    }

    public long count() {
        return serverRepository.count();
    }
}
