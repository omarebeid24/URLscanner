package controller;

import backend.URLSafetyChecker;
import backend.URLValidator;
import view.BatchPanel;

import javax.swing.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Controller for handling batch URL safety checks
 */
public class BatchController {

    private MainController mainController;
    private BatchPanel batchPanel;

    /**
     * Constructor
     */
    public BatchController(MainController mainController) {
        this.mainController = mainController;
    }

    /**
     * Set the batch panel
     */
    public void setBatchPanel(BatchPanel batchPanel) {
        this.batchPanel = batchPanel;
    }

    /**
     * Check multiple URLs in batch mode
     */
    public void checkURLs(List<String> urls) {
        if (urls.isEmpty()) {
            return;
        }

        // Create thread pool with fixed number of threads
        int numThreads = Math.min(Runtime.getRuntime().availableProcessors(), 4);
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        // Counters for progress tracking
        final AtomicInteger completed = new AtomicInteger(0);
        final AtomicInteger successful = new AtomicInteger(0);
        final int total = urls.size();

        // Process each URL
        for (String url : urls) {
            executor.submit(() -> {
                try {
                    processURL(url, completed, successful, total);
                } catch (Exception e) {
                    // Handle individual URL processing errors
                    SwingUtilities.invokeLater(() -> {
                        batchPanel.addResult(url, "ERROR", 0.0, e.getMessage());
                    });
                } finally {
                    int current = completed.incrementAndGet();

                    // Update progress on EDT
                    SwingUtilities.invokeLater(() -> {
                        batchPanel.updateProgress(current, total);

                        // If all URLs have been processed, finalize
                        if (current >= total) {
                            finalizeBatch(successful.get(), total);
                        }
                    });
                }
            });
        }

        // Shutdown executor when done (won't accept new tasks)
        executor.shutdown();
    }

    /**
     * Process a single URL in the batch
     */
    private void processURL(String url, AtomicInteger completed, AtomicInteger successful, int total) {
        try {
            // Validate URL
            URLValidator.ValidationResult validationResult = URLValidator.validate(url);

            if (!validationResult.isValid) {
                // Add invalid URL result
                SwingUtilities.invokeLater(() -> {
                    batchPanel.addResult(url, "INVALID", 0.0, validationResult.errorMessage);
                });
                return;
            }

            // Use normalized URL
            String normalizedUrl = validationResult.normalizedUrl;

            // Perform safety check
            URLSafetyChecker.SafetyResult result = mainController.getURLSafetyChecker().checkSafety(normalizedUrl);

            // Increment successful counter
            successful.incrementAndGet();

            // Add result to table on EDT
            SwingUtilities.invokeLater(() -> {
                batchPanel.addResult(
                        normalizedUrl,
                        result.verdict,
                        result.confidence,
                        result.reason
                );
            });

        } catch (Exception e) {
            // Handle errors
            SwingUtilities.invokeLater(() -> {
                batchPanel.addResult(url, "ERROR", 0.0, "Error: " + e.getMessage());
            });
        }
    }

    /**
     * Finalize batch processing
     */
    private void finalizeBatch(int successful, int total) {
        if (batchPanel != null) {
            batchPanel.finishBatch(successful, total);
        }
    }

    /**
     * Import URLs from a file
     */
    public void importURLs(File file) {
        try {
            List<String> urls = new ArrayList<>();

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty()) {
                        urls.add(line);
                    }
                }
            }

            if (urls.isEmpty()) {
                mainController.showError("Import Error", "No URLs found in the file.");
                return;
            }

            // Join URLs with newlines and set in text area
            StringBuilder sb = new StringBuilder();
            for (String url : urls) {
                sb.append(url).append("\n");
            }

            if (batchPanel != null) {
                batchPanel.setURLsText(sb.toString());
            }

            mainController.showMessage("Import Successful",
                    urls.size() + " URLs imported successfully.");

        } catch (IOException e) {
            mainController.showError("Import Error",
                    "Error reading file: " + e.getMessage());
        }
    }

    /**
     * Export batch results to a file
     */
    public void exportResults(File file) {
        try {
            boolean isCsv = file.getName().toLowerCase().endsWith(".csv");

            try (BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {

                // Get results from the table model
                JTable table = batchPanel.getResultsTable();

                // Write header
                if (isCsv) {
                    writer.write("URL,Verdict,Confidence,Reason\n");
                } else {
                    writer.write("URL Safety Analysis Results\n");
                    writer.write("============================\n\n");
                    writer.write("Timestamp: " + new java.util.Date() + "\n\n");
                }

                // Write each row
                for (int row = 0; row < table.getRowCount(); row++) {
                    String url = (String) table.getValueAt(row, 0);
                    String verdict = (String) table.getValueAt(row, 1);
                    String confidence = (String) table.getValueAt(row, 2);
                    String reason = (String) table.getValueAt(row, 3);

                    if (isCsv) {
                        // Escape fields for CSV
                        url = escapeForCsv(url);
                        verdict = escapeForCsv(verdict);
                        confidence = escapeForCsv(confidence);
                        reason = escapeForCsv(reason);

                        writer.write(url + "," + verdict + "," + confidence + "," + reason + "\n");
                    } else {
                        // Write formatted text
                        writer.write("URL: " + url + "\n");
                        writer.write("Verdict: " + verdict + "\n");
                        writer.write("Confidence: " + confidence + "\n");
                        writer.write("Reason: " + reason + "\n");
                        writer.write("----------------------------\n\n");
                    }
                }
            }

            mainController.showMessage("Export Successful",
                    "Results exported successfully to:\n" + file.getAbsolutePath());

        } catch (IOException e) {
            mainController.showError("Export Error",
                    "Error writing to file: " + e.getMessage());
        }
    }

    /**
     * Escape a string for CSV format
     */
    private String escapeForCsv(String s) {
        if (s == null) {
            return "";
        }

        // If string contains comma, quote, or newline, wrap in quotes and escape quotes
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }

        return s;
    }
}
