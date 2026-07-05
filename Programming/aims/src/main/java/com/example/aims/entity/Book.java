package com.example.aims.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.util.Set;
import java.util.HashSet;

@Entity
@Table(name = "book")
@DiscriminatorValue("BOOK")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Book extends PrintableProduct {

    @Column(name = "coverType")
    String coverType;

    @Column(name = "nbPages")
    Integer nbPages;

    @Column(name = "genre")
    String genre;

    // One Book can have many BookAuthors
    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @lombok.ToString.Exclude
    @lombok.EqualsAndHashCode.Exclude
    Set<BookAuthor> authors = new HashSet<>();
}
