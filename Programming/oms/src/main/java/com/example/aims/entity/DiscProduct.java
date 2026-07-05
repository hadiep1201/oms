package com.example.aims.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.sql.Date;

@Entity
@Table(name = "disc_product")
@DiscriminatorValue("DISC_PRODUCT")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DiscProduct extends PhysicalProduct {

    @Column(name = "genre")
    String genre;

    @Column(name = "releaseDate")
    Date releaseDate;
}
