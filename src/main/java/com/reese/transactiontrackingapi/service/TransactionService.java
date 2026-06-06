package com.reese.transactiontrackingapi.service;

import com.reese.transactiontrackingapi.dto.ImportResponse;
import com.reese.transactiontrackingapi.dto.SummaryResponse;
import com.reese.transactiontrackingapi.dto.TransactionRequest;
import com.reese.transactiontrackingapi.exception.ResourceNotFoundException;
import com.reese.transactiontrackingapi.models.Transaction;
import com.reese.transactiontrackingapi.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CsvImportService csvImportService;

    public TransactionService(TransactionRepository transactionRepository, CsvImportService csvImportService) {
        this.transactionRepository = transactionRepository;
        this.csvImportService = csvImportService;
    }

    @Transactional(readOnly = true)
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAllByOrderByDateDescIdDesc();
    }

    @Transactional
    public Transaction createTransaction(TransactionRequest transactionRequest) {
        Transaction transaction = new Transaction(
            transactionRequest.date(),
            transactionRequest.description().trim(),
            transactionRequest.amount(),
            transactionRequest.category().trim()
        );

        return transactionRepository.save(transaction);
    }

    @Transactional
    public void deleteTransaction(Long id) {
        if (!transactionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Transaction with id " + id + " was not found.");
        }

        transactionRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public SummaryResponse getSummary() {
        List<Transaction> transactions = transactionRepository.findAll();
        BigDecimal incomeTotal = transactions.stream()
            .map(Transaction::getAmount)
            .filter(amount -> amount.compareTo(BigDecimal.ZERO) > 0)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal expenseTotal = transactions.stream()
            .map(Transaction::getAmount)
            .filter(amount -> amount.compareTo(BigDecimal.ZERO) < 0)
            .map(BigDecimal::abs)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        String topCategory = determineTopCategory(transactions);

        return new SummaryResponse(
            transactionRepository.calculateTotalAmount(),
            transactionRepository.count(),
            incomeTotal,
            expenseTotal,
            topCategory,
            buildSummaryText(transactionRepository.calculateTotalAmount(), transactionRepository.count(), incomeTotal, expenseTotal, topCategory)
        );
    }

    @Transactional
    public ImportResponse importTransactions(MultipartFile file) {
        int rowsImported = csvImportService.importCsv(file);
        return new ImportResponse("CSV imported successfully", rowsImported);
    }

    private String determineTopCategory(List<Transaction> transactions) {
        return transactions.stream()
            .filter(transaction -> transaction.getCategory() != null && !transaction.getCategory().isBlank())
            .collect(Collectors.groupingBy(
                Transaction::getCategory,
                Collectors.mapping(Transaction::getAmount, Collectors.reducing(BigDecimal.ZERO, BigDecimal::abs, BigDecimal::add))
            ))
            .entrySet()
            .stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("No category data");
    }

    private String buildSummaryText(
        BigDecimal total,
        long transactionCount,
        BigDecimal incomeTotal,
        BigDecimal expenseTotal,
        String topCategory
    ) {
        if (transactionCount == 0) {
            return "No transactions are available yet. Add a manual transaction or import a CSV to generate a summary.";
        }

        String balanceDirection = total.compareTo(BigDecimal.ZERO) >= 0 ? "positive" : "negative";
        return "You currently have " + transactionCount + " recorded transactions with a " + balanceDirection
            + " net balance of " + total + ". Income totals " + incomeTotal + ", expenses total " + expenseTotal
            + ", and the most active category is " + topCategory + ".";
    }
}
