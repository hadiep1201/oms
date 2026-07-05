package com.example.aims.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Table(name = "delivery_info")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DeliveryInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "deliveryInfoId")
    Integer deliveryInfoId;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToOne
    @JoinColumn(name = "order_id", unique = true, nullable = false, referencedColumnName = "orderId")
    Order order;

    @Column(name = "receiverName")
    String receiverName;

    @Column(name = "email")
    String email;

    @Column(name = "phoneNumber")
    String phoneNumber;

    @Column(name = "address")
    String address;

    @Column(name = "city")
    String city;

}
