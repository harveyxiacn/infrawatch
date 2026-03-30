package com.infrawatch.model.testing;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.infrawatch.model.base.BaseEntity;
import com.infrawatch.model.server.Server;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "test_cases")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestCase extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 30)
    private String category;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id")
    private Server server;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String steps;

    @Column(name = "expected_result", nullable = false, columnDefinition = "TEXT")
    private String expectedResult;

    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = true;
}
