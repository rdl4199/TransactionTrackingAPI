package com.reese.transactiontrackingapi.service;

import com.reese.transactiontrackingapi.exception.InvalidCsvException;
import com.reese.transactiontrackingapi.models.Transaction;
import com.reese.transactiontrackingapi.repository.TransactionRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class CsvImportService {

    private final TransactionRepository transactionRepository;

    public CsvImportService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public int importCsv(MultipartFile file) {
        validateFile(file);
        List<Transaction> transactions = new ArrayList<>();

        try (
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8)
            );
            CSVParser csvParser = CSVFormat.DEFAULT
                .builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .build()
                .parse(reader)
        ) {
            for (CSVRecord record : csvParser) {
                LocalDate date = LocalDate.parse(getRequiredValue(record, "date"));
                String description = getRequiredValue(record, "description");
                BigDecimal amount = new BigDecimal(getRequiredValue(record, "amount"));
                String category = getRequiredValue(record, "category");

                transactions.add(new Transaction(date, description, amount, category));
            }
        } catch (IOException exception) {
            throw new InvalidCsvException("Unable to read the uploaded CSV file.", exception);
        } catch (RuntimeException exception) {
            throw new InvalidCsvException("CSV import failed. Ensure the file uses date, description, amount, and category columns.", exception);
        }

        transactionRepository.saveAll(transactions);
        return transactions.size();
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidCsvException("A non-empty CSV file is required.");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".csv")) {
            throw new InvalidCsvException("Only .csv files are supported for import.");
        }
    }

    private String getRequiredValue(CSVRecord record, String columnName) {
        if (!record.isMapped(columnName)) {
            throw new InvalidCsvException("Missing required column: " + columnName);
        }

        String value = record.get(columnName).trim();
        if (value.isEmpty()) {
            throw new InvalidCsvException("Column '" + columnName + "' cannot be empty.");
        }

        return value;
    }
}
