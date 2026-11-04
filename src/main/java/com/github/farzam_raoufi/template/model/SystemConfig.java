package com.github.farzam_raoufi.template.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(
        name = "system_config",
        indexes = {
                @Index(name = "idx_system_config_key", columnList = "config_key", unique = true)
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "configValue") // never print possibly-secret values
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class SystemConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "config_key", nullable = false, updatable = false, length = 150)
    private String configKey;

    @Column(name = "config_value", length = 4000)
    private String configValue;

    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}