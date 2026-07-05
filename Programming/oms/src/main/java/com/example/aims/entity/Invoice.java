package com.example.aims.entity;

import com.example.aims.enums.InvoiceStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;

@Entity
@Table(name = "invoice")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "invoiceId")
    Integer invoiceId;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToOne
    @JoinColumn(name = "order_id", unique = true, nullable = false, referencedColumnName = "orderId")
    Order order;

    @Column(name = "subTotal", precision = 10, scale = 2)
    BigDecimal subTotal;

    @Column(name = "shippingFee", precision = 10, scale = 2)
    BigDecimal shippingFee;

    @Column(name = "vatAmount", precision = 10, scale = 2)
    BigDecimal vatAmount;

    @Column(name = "totalAmount", precision = 10, scale = 2)
    BigDecimal totalAmount;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    InvoiceStatus status;

    // One Invoice has one PaymentTransaction (1-1)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToOne(mappedBy = "invoice", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    PaymentTransaction paymentTransaction;
}
