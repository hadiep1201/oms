package com.example.aims.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "track")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Track {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trackId")
    Integer trackId;

    @ManyToOne
    @JoinColumn(name = "cd_id", nullable = false, referencedColumnName = "id")
    @lombok.ToString.Exclude
    @lombok.EqualsAndHashCode.Exclude
    CD cd;

    @Column(name = "title")
    String title;

    @Column(name = "length")
    Integer length;
}
