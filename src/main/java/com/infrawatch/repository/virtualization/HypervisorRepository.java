package com.infrawatch.repository.virtualization;

import com.infrawatch.model.virtualization.Hypervisor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface HypervisorRepository extends JpaRepository<Hypervisor, UUID> {

    Optional<Hypervisor> findByHostname(String hostname);

    boolean existsByHostname(String hostname);
}
