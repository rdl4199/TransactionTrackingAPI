package com.reese.transactiontrackingapi.controller;

import com.reese.transactiontrackingapi.dto.ImportResponse;
import com.reese.transactiontrackingapi.dto.SummaryResponse;
import com.reese.transactiontrackingapi.dto.TransactionRequest;
import com.reese.transactiontrackingapi.models.Transaction;
import com.reese.transactiontrackingapi.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    public List<Transaction> getAllTransactions() {
        return transactionService.getAllTransactions();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Transaction createTransaction(@Valid @RequestBody TransactionRequest transactionRequest) {
        return transactionService.createTransaction(transactionRequest);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        transactionService.deleteTransaction(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping({"/summary", "/summary/total"})
    public SummaryResponse getTotal() {
        return transactionService.getSummary();
    }

    @PostMapping("/import")
    public ImportResponse importTransactions(@RequestParam("file") MultipartFile file) {
        return transactionService.importTransactions(file);
    }
}
