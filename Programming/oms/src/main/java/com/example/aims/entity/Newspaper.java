package com.example.aims.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.util.Set;
import java.util.HashSet;

@Entity
@Table(name = "newspaper")
@DiscriminatorValue("NEWSPAPER")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Newspaper extends PrintableProduct {

    @Column(name = "editorInChief")
    String editorInChief;

    @Column(name = "issueNumber")
    String issueNumber;

    @Column(name = "publicationFrequency")
    String publicationFrequency;

    @Column(name = "issn")
    String issn;

    // One Newspaper can have many Sections
    @OneToMany(mappedBy = "newspaper", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @lombok.ToString.Exclude
    @lombok.EqualsAndHashCode.Exclude
    Set<Section> sections = new HashSet<>();
}
