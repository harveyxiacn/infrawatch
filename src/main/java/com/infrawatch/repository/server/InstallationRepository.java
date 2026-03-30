package com.infrawatch.repository.server;

import com.infrawatch.model.server.Installation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface InstallationRepository extends JpaRepository<Installation, UUID> {

    Page<Installation> findByServerIdOrderByCreatedAtDesc(UUID serverId, Pageable pageable);
}
