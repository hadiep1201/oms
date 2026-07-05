package com.example.aims.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Set;
import java.util.HashSet;

@Entity
@Table(name = "product")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "dtype", discriminatorType = DiscriminatorType.STRING)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    Integer id;

    @Column(name = "title", nullable = false)
    String title;

    @Column(name = "categlory")
    String category;

    @Column(name = "generalDescription")
    String generalDescription;
    @Column(name = "imageUrl")
    String imageUrl;

    @Column(name = "originalValue", precision = 10, scale = 2)
    BigDecimal originalValue;

    @Column(name = "currentPrice", precision = 10, scale = 2)
    BigDecimal currentPrice;

    @Column(name = "stockQuantity")
    Integer stockQuantity;

    @Column(name = "status")
    String status;

    // One Product can have many History records
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    Set<History> histories = new HashSet<>();

    // One Product can have many OrderDetails
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    Set<OrderDetail> orderDetails = new HashSet<>();
}
