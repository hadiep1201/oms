package com.example.aims.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.util.Set;
import java.util.HashSet;

@Entity
@Table(name = "cd")
@DiscriminatorValue("CD")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CD extends DiscProduct {

    @Column(name = "artists")
    String artists;

    @Column(name = "recordLabel")
    String recordLabel;

    // One CD can have many Tracks
    @OneToMany(mappedBy = "cd", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @lombok.ToString.Exclude
    @lombok.EqualsAndHashCode.Exclude
    Set<Track> tracks = new HashSet<>();
}
