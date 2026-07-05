package com.example.aims.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "printable_product")
@DiscriminatorValue("PRINTABLE_PRODUCT")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PrintableProduct extends PhysicalProduct {

    @Column(name = "publicationDate")
    String publicationDate;

    @Column(name = "language")
    String language;

    @Column(name = "publisher")
    String publisher;
}
