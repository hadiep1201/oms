package com.example.aims.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.sql.Date;

@Entity
@Table(name = "book_author")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookAuthor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "authorId")
    Integer authorId;

    @ManyToOne
    @JoinColumn(name = "book_id", nullable = false, referencedColumnName = "id")
    @lombok.ToString.Exclude
    @lombok.EqualsAndHashCode.Exclude
    Book book;

    @Column(name = "name")
    String name;

    @Column(name = "dob")
    Date dob;
}
