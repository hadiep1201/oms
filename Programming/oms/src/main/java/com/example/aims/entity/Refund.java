package com.example.aims.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;

@Entity
@Table(name = "refund")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Refund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "refundId")
    Integer refundId;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne
    @JoinColumn(name = "transaction_id", nullable = false, referencedColumnName = "transactionId")
    PaymentTransaction paymentTransaction;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false, referencedColumnName = "userId")
    User user;

    @Column(name = "status")
    String status;

    @Column(name = "note")
    String note;

    @Column(name = "amount", precision = 10, scale = 2)
    BigDecimal amount;

    @Column(name = "refundType")
    String refundType;
}
