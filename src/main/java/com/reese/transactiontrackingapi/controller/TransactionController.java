package com.reese.transactiontrackingapi.controller;

import com.reese.transactiontrackingapi.models.Transaction;
import com.reese.transactiontrackingapi.repository.TransactionRepository;
import com.reese.transactiontrackingapi.service.CsvImportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionRepository transactionRepository;
    private final CsvImportService csvImportService;

    public TransactionController(TransactionRepository transactionRepository, CsvImportService csvImportService) {
        this.transactionRepository = transactionRepository;
        this.csvImportService = csvImportService;
    }

    @GetMapping
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    @PostMapping
    public Transaction createTransaction(@RequestBody Transaction transaction) {
        return transactionRepository.save(transaction);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        if (!transactionRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        transactionRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/summary/total")
    public Map<String, BigDecimal> getTotal() {
        BigDecimal total = transactionRepository.findAll()
            .stream()
            .map(Transaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, BigDecimal> response = new LinkedHashMap<>();
        response.put("total", total);
        return response;
    }

    @PostMapping("/import")
    public ResponseEntity<Map<String, Object>> importTransactions(@RequestParam("file") MultipartFile file) {
        try {
            int importedCount = csvImportService.importCsv(file);

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("message", "CSV imported successfully");
            response.put("rowsImported", importedCount);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("message", "Import failed");
            response.put("error", e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }
}