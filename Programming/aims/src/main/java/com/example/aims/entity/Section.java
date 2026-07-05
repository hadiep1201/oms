package com.example.aims.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "section")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Section {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sectionId")
    Integer sectionId;

    @ManyToOne
    @JoinColumn(name = "newspaper_id", nullable = false, referencedColumnName = "id")
    @lombok.ToString.Exclude
    @lombok.EqualsAndHashCode.Exclude
    Newspaper newspaper;

    @Column(name = "title")
    String title;

    @Column(name = "description")
    String description;
}
