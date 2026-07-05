package com.example.aims.repository;

import com.example.aims.entity.Refund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefundRepository extends JpaRepository<Refund, Integer> {

    boolean existsByPaymentTransaction_TransactionId(Integer transactionId);
}
