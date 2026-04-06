package com.reese.transactiontrackingapi.service;

import com.reese.transactiontrackingapi.models.Transaction;
import com.reese.transactiontrackingapi.repository.TransactionRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
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

    public int importCsv(MultipartFile file) throws Exception {
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
                LocalDate date = LocalDate.parse(record.get("date").trim());
                String description = record.get("description").trim();
                BigDecimal amount = new BigDecimal(record.get("amount").trim());

                String category = "";
                if (record.isMapped("category")) {
                    category = record.get("category").trim();
                }

                transactions.add(new Transaction(date, description, amount, category));
            }
        }

        transactionRepository.saveAll(transactions);
        return transactions.size();
    }
}