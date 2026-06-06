package com.reese.expensetracker;

import com.reese.transactiontrackingapi.TransactionTrackingApiApplication;
import com.reese.transactiontrackingapi.dto.ImportResponse;
import com.reese.transactiontrackingapi.dto.SummaryResponse;
import com.reese.transactiontrackingapi.dto.TransactionRequest;
import com.reese.transactiontrackingapi.exception.InvalidCsvException;
import com.reese.transactiontrackingapi.exception.ResourceNotFoundException;
import com.reese.transactiontrackingapi.models.Transaction;
import com.reese.transactiontrackingapi.repository.TransactionRepository;
import com.reese.transactiontrackingapi.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(classes = TransactionTrackingApiApplication.class)
class TransactionServiceIntegrationTests {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TransactionRepository transactionRepository;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
    }

    @Test
    void shouldCreateTransaction() {
        Transaction createdTransaction = transactionService.createTransaction(new TransactionRequest(
            LocalDate.of(2026, 5, 9),
            "Laptop stand",
            new BigDecimal("49.99"),
            "Office"
        ));

        assertEquals("Laptop stand", createdTransaction.getDescription());
        assertEquals(1, transactionRepository.count());
    }

    @Test
    void shouldReturnTransactionsSortedByDateDescending() {
        transactionRepository.save(new Transaction(LocalDate.of(2026, 5, 1), "Rent", new BigDecimal("-1250.00"), "Housing"));
        transactionRepository.save(new Transaction(LocalDate.of(2026, 5, 8), "Paycheck", new BigDecimal("2400.00"), "Income"));

        List<Transaction> transactions = transactionService.getAllTransactions();

        assertEquals(2, transactions.size());
        assertEquals("Paycheck", transactions.get(0).getDescription());
        assertEquals("Rent", transactions.get(1).getDescription());
    }

    @Test
    void shouldReturnSummaryWithTotalAndCount() {
        transactionRepository.save(new Transaction(LocalDate.of(2026, 5, 1), "Groceries", new BigDecimal("-54.21"), "Food"));
        transactionRepository.save(new Transaction(LocalDate.of(2026, 5, 3), "Freelance", new BigDecimal("300.00"), "Income"));

        SummaryResponse summary = transactionService.getSummary();

        assertEquals(new BigDecimal("245.79"), summary.total());
        assertEquals(2, summary.transactionCount());
        assertEquals(new BigDecimal("300.00"), summary.incomeTotal());
        assertEquals(new BigDecimal("54.21"), summary.expenseTotal());
        assertEquals("Income", summary.topCategory());
    }

    @Test
    void shouldReturnHelpfulSummaryWhenNoTransactionsExist() {
        SummaryResponse summary = transactionService.getSummary();

        assertEquals(BigDecimal.ZERO, summary.total());
        assertEquals(0, summary.transactionCount());
        assertEquals(BigDecimal.ZERO, summary.incomeTotal());
        assertEquals(BigDecimal.ZERO, summary.expenseTotal());
        assertEquals("No category data", summary.topCategory());
        assertTrue(summary.summaryText().contains("No transactions are available yet"));
    }

    @Test
    void shouldDeleteExistingTransaction() {
        Transaction transaction = transactionRepository.save(
            new Transaction(LocalDate.of(2026, 5, 5), "Desk lamp", new BigDecimal("32.00"), "Office")
        );

        transactionService.deleteTransaction(transaction.getId());

        assertEquals(0, transactionRepository.count());
    }

    @Test
    void shouldThrowWhenDeletingMissingTransaction() {
        assertThrows(ResourceNotFoundException.class, () -> transactionService.deleteTransaction(99999L));
    }

    @Test
    void shouldImportCsvTransactions() {
        String csv = """
            date,description,amount,category
            2026-05-01,Coffee,-4.50,Food
            2026-05-02,Invoice,1200.00,Income
            """;

        MockMultipartFile file = new MockMultipartFile(
            "file",
            "transactions.csv",
            "text/csv",
            csv.getBytes(StandardCharsets.UTF_8)
        );

        ImportResponse response = transactionService.importTransactions(file);

        assertEquals("CSV imported successfully", response.message());
        assertEquals(2, response.rowsImported());
        assertEquals(2, transactionRepository.count());
    }

    @Test
    void shouldRejectEmptyCsvImport() {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "transactions.csv",
            "text/csv",
            new byte[0]
        );

        assertThrows(InvalidCsvException.class, () -> transactionService.importTransactions(file));
    }

    @Test
    void shouldRejectNonCsvFileImport() {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "transactions.txt",
            "text/plain",
            "not,a,csv".getBytes(StandardCharsets.UTF_8)
        );

        InvalidCsvException exception = assertThrows(
            InvalidCsvException.class,
            () -> transactionService.importTransactions(file)
        );

        assertEquals("Only .csv files are supported for import.", exception.getMessage());
    }

    @Test
    void shouldRejectCsvMissingRequiredColumn() {
        String csv = """
            date,description,amount
            2026-05-01,Coffee,-4.50
            """;

        MockMultipartFile file = new MockMultipartFile(
            "file",
            "transactions.csv",
            "text/csv",
            csv.getBytes(StandardCharsets.UTF_8)
        );

        InvalidCsvException exception = assertThrows(
            InvalidCsvException.class,
            () -> transactionService.importTransactions(file)
        );

        assertEquals(
            "CSV import failed. Ensure the file uses date, description, amount, and category columns.",
            exception.getMessage()
        );
    }
}
