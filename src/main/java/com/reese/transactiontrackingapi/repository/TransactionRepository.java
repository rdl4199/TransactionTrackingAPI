package com.reese.transactiontrackingapi.repository;

import com.reese.transactiontrackingapi.models.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findAllByOrderByDateDescIdDesc();

    @Query("select coalesce(sum(t.amount), 0) from Transaction t")
    BigDecimal calculateTotalAmount();
}
