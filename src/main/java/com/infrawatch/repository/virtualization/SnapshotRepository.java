package com.infrawatch.repository.virtualization;

import com.infrawatch.model.virtualization.Snapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface SnapshotRepository extends JpaRepository<Snapshot, UUID> {

    List<Snapshot> findByVmId(UUID vmId);

    List<Snapshot> findByCreatedAtBefore(LocalDateTime dateTime);
}
