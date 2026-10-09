package com.orbin.school.academic.entity;

import com.orbin.school.common.entity.BaseEntity;
import com.orbin.school.school.entity.School;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

@Entity
@Table(name = "period_slots",
       uniqueConstraints = @UniqueConstraint(columnNames = {"school_id", "slot_number"}))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class PeriodSlot extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @Column(name = "slot_number", nullable = false)
    private Integer slotNumber;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "is_break", nullable = false)
    @Builder.Default
    private Boolean isBreak = false;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String tier = "ALL"; // PRIMARY, SECONDARY, ALL
}
