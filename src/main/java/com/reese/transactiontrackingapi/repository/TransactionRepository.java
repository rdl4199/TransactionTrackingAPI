package com.reese.transactiontrackingapi.repository;

import com.reese.transactiontrackingapi.models.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}