package com.example.aims.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.sql.Timestamp;
import java.util.Set;
import java.util.HashSet;

/**
 * Cohesion: Functional - all fields represent the single domain concept of a
 *           payment transaction record (method, status, timestamps, PayPal IDs).
 *
 * Coupling:
 * - Stamp coupling with Invoice (@OneToOne):
 *     PaymentTransaction is associated with one Invoice; created and saved by
 *     PayThroughPaymentGatewayService after a successful PayPal capture.
 */
@Entity
@Table(name = "payment_transaction")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transactionId")
    Integer transactionId;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToOne
    @JoinColumn(name = "invoice_id", unique = true, nullable = false, referencedColumnName = "invoiceId")
    Invoice invoice;

    @Column(name = "method")
    String method;

    @Column(name = "transactionDatetime")
    Timestamp transactionDatetime;

    @Column(name = "transactionStatus")
    String transactionStatus;

    @Column(name = "transactionContent")
    String transactionContent;

    /*
     * SOLID analysis:
     *
     * - SRP: PaymentTransaction is a provider-neutral transaction entity, but these columns store
     *   PayPal-specific identifiers. The entity now changes when PayPal-specific persistence needs
     *   change, even if the common transaction model is stable.
     * - OCP: Adding another gateway with provider-specific identifiers would require adding more
     *   columns to this shared entity, so the model is not closed for extension.
     *
     * Refactoring direction:
     * - Keep common fields here and move provider-specific data to a PaymentProviderMetadata table,
     *   a one-to-one provider detail entity, or a provider-neutral externalOrderId/externalCaptureId
     *   naming scheme if all gateways share the same concept.
     */
    @Column(name = "paypalOrderId")
    String externalOrderId;

    @Column(name = "paypalCaptureId")
    String externalCaptureId;

    // One PaymentTransaction can have many Refunds
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "paymentTransaction", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    Set<Refund> refunds = new HashSet<>();
}
