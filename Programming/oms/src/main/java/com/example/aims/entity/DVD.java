package com.example.aims.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.sql.Date;

@Entity
@Table(name = "dvd")
@DiscriminatorValue("DVD")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DVD extends DiscProduct {

    @Column(name = "discType")
    String discType;

    @Column(name = "director")
    String director;

    @Column(name = "runtime")
    Integer runtime;

    @Column(name = "studio")
    String studio;

    @Column(name = "language")
    String language;

    @Column(name = "subtitles")
    String subtitles;
}
