package com.example.aims.repository;

import com.example.aims.entity.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Integer> {

    Optional<PaymentTransaction> findByInvoice_InvoiceId(Integer invoiceId);
}
