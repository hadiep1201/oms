package com.example.aims.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.sql.Timestamp;

@Entity
@Table(name = "admin_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "logId")
    Integer logId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "adminUserId", nullable = false)
    User adminUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "targetUserId", nullable = false)
    User targetUser;

    @Column(name = "action", nullable = false)
    String action;

    @Column(name = "timeStamp", nullable = false)
    Timestamp timeStamp;
}
