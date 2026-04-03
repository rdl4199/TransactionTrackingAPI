package com.rhys.transactiontracker.repository;

import com.rhys.transactiontracker.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}