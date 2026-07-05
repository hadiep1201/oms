package com.example.aims.entity;

import com.example.aims.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.sql.Timestamp;
import java.util.Set;
import java.util.HashSet;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "orderId")
    Integer orderId;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    OrderStatus status;

    @Column(name = "shippingFee", precision = 10, scale = 2)
    java.math.BigDecimal shippingFee;

    @Column(name = "createdDate")
    Timestamp createdDate;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    // Loại bỏ các trường mối quan hệ vòng khỏi toString/equals/hashCode do Lombok @Data tạo tự động.
    // Nếu không, các liên kết hai chiều như Order ↔ OrderDetail hoặc Order ↔ Invoice có thể đệ quy vô hạn và gây StackOverflowError.
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    Set<OrderDetail> orderDetails = new HashSet<>();

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    DeliveryInfo deliveryInfo;

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    Invoice invoice;
}
