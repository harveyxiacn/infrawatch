package com.infrawatch.repository.server;

import com.infrawatch.model.server.Server;
import com.infrawatch.model.server.enums.Environment;
import com.infrawatch.model.server.enums.ServerStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ServerRepository extends JpaRepository<Server, UUID> {

    Optional<Server> findByHostname(String hostname);

    boolean existsByHostname(String hostname);

    Page<Server> findByStatus(ServerStatus status, Pageable pageable);

    Page<Server> findByEnvironment(Environment environment, Pageable pageable);

    @Query("SELECT COUNT(s) FROM Server s WHERE s.status = :status")
    long countByStatus(ServerStatus status);

    Page<Server> findByHostnameContainingIgnoreCase(String hostname, Pageable pageable);
}
