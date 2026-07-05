package com.example.aims.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Entity
@Table(name = "physical_product")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public abstract class PhysicalProduct extends Product {

    @Column(name = "barcode")
    String barcode;

    @Column(name = "weight", precision = 10, scale = 2)
    BigDecimal weight;

    @Column(name = "length", precision = 10, scale = 2)
    BigDecimal length;

    @Column(name = "height", precision = 10, scale = 2)
    BigDecimal height;

    @Column(name = "width", precision = 10, scale = 2)
    BigDecimal width;
}
