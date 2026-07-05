package com.example.aims.repository;

import com.example.aims.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Integer> {

    /**
     * Returns the invoice total for an order as a scalar projection.
     *
     * Rooted from Order (the relationship the code already relies on: order.getInvoice()),
     * so it does not depend on an Invoice -> Order back-reference.
     *
     * Using a projection (not the Invoice/Order entity) avoids putting the Order into the
     * persistence context, so a later entity read in the same request (e.g. with
     * open-in-view) is not served a stale, cached instance.
     */
    @Query("select o.invoice.totalAmount from Order o where o.orderId = :orderId")
    BigDecimal findTotalAmountByOrderId(@Param("orderId") Integer orderId);
}