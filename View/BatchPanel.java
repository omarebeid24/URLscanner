package view;

import controller.BatchController;
import controller.MainController;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;

/**
 * Panel for batch URL checking
 */
public class BatchPanel extends JPanel {

    private JTextArea urlTextArea;
    private JButton checkButton;
    private JButton clearButton;
    private JButton importButton;
    private JButton exportButton;
    private JTable resultsTable;
    private DefaultTableModel tableModel;
    private JLabel statusLabel;
    private JProgressBar progressBar;

    private BatchController batchController;

    /**
     * Constructor
     */
    public BatchPanel(MainController mainController) {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        this.batchController = mainController.getBatchController();
        this.batchController.setBatchPanel(this);

        initializeComponents();
        layoutComponents();
    }

    /**
     * Initialize UI components
     */
    private void initializeComponents() {
        // URL input area
        urlTextArea = new JTextArea(10, 40);
        urlTextArea.setLineWrap(true);
        urlTextArea.setWrapStyleWord(true);
        urlTextArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        urlTextArea.setFont(new Font("SansSerif", Font.PLAIN, 12));

        // Placeholder text
        urlTextArea.setText("Enter URLs to check (one per line)");
        urlTextArea.setForeground(Color.GRAY);

        urlTextArea.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (urlTextArea.getText().equals("Enter URLs to check (one per line)")) {
                    urlTextArea.setText("");
                    urlTextArea.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (urlTextArea.getText().isEmpty()) {
                    urlTextArea.setText("Enter URLs to check (one per line)");
                    urlTextArea.setForeground(Color.GRAY);
                }
            }
        });

        // Buttons
        checkButton = new JButton("Check URLs");
        checkButton.setIcon(new ImageIcon(getClass().getResource("/resources/check_url.png")));
        checkButton.addActionListener(this::onCheckButtonClicked);

        clearButton = new JButton("Clear All");
        clearButton.setIcon(new ImageIcon(getClass().getResource("/resources/clearall.png")));
        clearButton.addActionListener(e -> clearAll());

        importButton = new JButton("Import List");
        importButton.setIcon(new ImageIcon(getClass().getResource("/resources/import.png")));
        importButton.addActionListener(this::onImportButtonClicked);

        exportButton = new JButton("Export Results");
        exportButton.setIcon(new ImageIcon(getClass().getResource("/resources/export.png")));
        exportButton.addActionListener(this::onExportButtonClicked);
        exportButton.setEnabled(false);

        // Results table
        tableModel = new DefaultTableModel(
                new Object[][] {},
                new String[] {"URL", "Verdict", "Confidence", "Reason"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
            }
        };

        resultsTable = new JTable(tableModel);
        resultsTable.setRowHeight(25);
        resultsTable.getTableHeader().setReorderingAllowed(false);

        // Set column widths
        resultsTable.getColumnModel().getColumn(0).setPreferredWidth(300); // URL
        resultsTable.getColumnModel().getColumn(1).setPreferredWidth(100); // Verdict
        resultsTable.getColumnModel().getColumn(2).setPreferredWidth(80);  // Confidence
        resultsTable.getColumnModel().getColumn(3).setPreferredWidth(400); // Reason

        // Custom cell renderer for verdict column
        resultsTable.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                String verdict = value.toString();
                switch (verdict.toUpperCase()) {
                    case "SAFE":
                    case "PROBABLY SAFE":
                        c.setForeground(new Color(0, 150, 0));
                        break;
                    case "UNCERTAIN":
                        c.setForeground(new Color(150, 150, 0));
                        break;
                    case "SUSPICIOUS":
                        c.setForeground(new Color(200, 120, 0));
                        break;
                    case "DANGEROUS":
                        c.setForeground(new Color(200, 0, 0));
                        break;
                    default:
                        c.setForeground(Color.BLACK);
                }

                return c;
            }
        });

        // Status label and progress bar
        statusLabel = new JLabel("Ready for batch analysis");
        statusLabel.setForeground(Color.GRAY);

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setString("0%");
        progressBar.setValue(0);
        progressBar.setVisible(false);
    }

    /**
     * Layout UI components
     */
    private void layoutComponents() {
        // Input panel (top)
        JPanel inputPanel = new JPanel(new BorderLayout(10, 10));
        inputPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Enter URLs"),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // URL area with scroll pane
        JScrollPane urlScrollPane = new JScrollPane(urlTextArea);
        inputPanel.add(urlScrollPane, BorderLayout.CENTER);

        // Button panel on the right
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

        checkButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        clearButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        importButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        exportButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        buttonPanel.add(checkButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        buttonPanel.add(clearButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        buttonPanel.add(importButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        buttonPanel.add(exportButton);

        inputPanel.add(buttonPanel, BorderLayout.EAST);

        // Results panel (bottom)
        JPanel resultsPanel = new JPanel(new BorderLayout(10, 10));
        resultsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Batch Results"),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JScrollPane tableScrollPane = new JScrollPane(resultsTable);
        resultsPanel.add(tableScrollPane, BorderLayout.CENTER);

        // Status panel at the bottom
        JPanel statusPanel = new JPanel(new BorderLayout(10, 0));
        statusPanel.add(statusLabel, BorderLayout.WEST);
        statusPanel.add(progressBar, BorderLayout.EAST);

        resultsPanel.add(statusPanel, BorderLayout.SOUTH);

        // Add both panels to main panel with a split pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, inputPanel, resultsPanel);
        splitPane.setDividerLocation(200);
        splitPane.setResizeWeight(0.3);

        add(splitPane, BorderLayout.CENTER);
    }

    /**
     * Handle check button click
     */
    private void onCheckButtonClicked(ActionEvent e) {
        String text = urlTextArea.getText();
        if (text.isEmpty() || text.equals("Enter URLs to check (one per line)")) {
            JOptionPane.showMessageDialog(this,
                    "Please enter at least one URL to check.",
                    "No URLs Entered",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Split by newlines to get individual URLs
        String[] urls = text.split("\\r?\\n");

        // Filter out empty lines
        java.util.List<String> urlList = new java.util.ArrayList<>();
        for (String url : urls) {
            url = url.trim();
            if (!url.isEmpty()) {
                urlList.add(url);
            }
        }

        if (urlList.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter at least one valid URL to check.",
                    "No Valid URLs",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Clear previous results
        clearResults();

        // Show progress indicators
        statusLabel.setText("Analyzing URLs... Please wait.");
        statusLabel.setForeground(Color.BLUE);
        progressBar.setValue(0);
        progressBar.setVisible(true);

        // Disable input while processing
        setInputEnabled(false);

        // Start batch check
        batchController.checkURLs(urlList);
    }

    /**
     * Handle import button click
     */
    private void onImportButtonClicked(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Import URL List");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setAcceptAllFileFilterUsed(false);

        // Add file filters
        fileChooser.addChoosableFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Text Files (*.txt)", "txt"));
        fileChooser.addChoosableFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "CSV Files (*.csv)", "csv"));

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            batchController.importURLs(selectedFile);
        }
    }

    /**
     * Handle export button click
     */
    private void onExportButtonClicked(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Results");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setAcceptAllFileFilterUsed(false);

        // Add file filters
        fileChooser.addChoosableFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "CSV Files (*.csv)", "csv"));
        fileChooser.addChoosableFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Text Files (*.txt)", "txt"));

        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();

            // Add extension if missing
            String filename = selectedFile.getAbsolutePath();
            if (!filename.toLowerCase().endsWith(".csv") && !filename.toLowerCase().endsWith(".txt")) {
                // Default to CSV if no extension specified
                selectedFile = new File(filename + ".csv");
            }

            batchController.exportResults(selectedFile);
        }
    }

    /**
     * Update the progress bar
     */
    public void updateProgress(int current, int total) {
        int percentage = (int)((current / (double)total) * 100);
        progressBar.setValue(percentage);
        progressBar.setString(current + "/" + total + " (" + percentage + "%)");

        statusLabel.setText("Processing URL " + current + " of " + total + "...");
    }

    /**
     * Add a result to the table
     */
    public void addResult(String url, String verdict, double confidence, String reason) {
        tableModel.addRow(new Object[]{
                url,
                verdict,
                String.format("%.1f%%", confidence * 100),
                reason
        });

        // Enable export button once we have results
        if (!exportButton.isEnabled() && tableModel.getRowCount() > 0) {
            exportButton.setEnabled(true);
        }
    }

    /**
     * Finalize batch processing
     */
    public void finishBatch(int successful, int total) {
        progressBar.setValue(100);
        progressBar.setString("Complete");

        statusLabel.setText("Batch analysis complete. " + successful + " of " + total + " URLs analyzed successfully.");
        statusLabel.setForeground(new Color(0, 150, 0));

        // Re-enable input
        setInputEnabled(true);
    }

    /**
     * Set error status
     */
    public void setErrorStatus(String error) {
        statusLabel.setText("Error: " + error);
        statusLabel.setForeground(Color.RED);
        progressBar.setVisible(false);

        // Re-enable input
        setInputEnabled(true);
    }

    /**
     * Set the URLs text
     */
    public void setURLsText(String text) {
        urlTextArea.setText(text);
        urlTextArea.setForeground(Color.BLACK);
    }

    /**
     * Clear all input and results
     */
    public void clearAll() {
        // Clear URL text area
        urlTextArea.setText("Enter URLs to check (one per line)");
        urlTextArea.setForeground(Color.GRAY);

        // Clear results
        clearResults();

        // Reset status
        statusLabel.setText("Ready for batch analysis");
        statusLabel.setForeground(Color.GRAY);
        progressBar.setValue(0);
        progressBar.setVisible(false);
    }

    /**
     * Clear just the results
     */
    public void clearResults() {
        // Clear table
        while (tableModel.getRowCount() > 0) {
            tableModel.removeRow(0);
        }

        // Disable export button
        exportButton.setEnabled(false);
    }

    /**
     * Get the results table
     */
    public JTable getResultsTable() {
        return resultsTable;
    }

    /**
     * Enable or disable input components
     */
    private void setInputEnabled(boolean enabled) {
        urlTextArea.setEnabled(enabled);
        checkButton.setEnabled(enabled);
        clearButton.setEnabled(enabled);
        importButton.setEnabled(enabled);

        // Keep export button enabled if we have results
        exportButton.setEnabled(enabled && tableModel.getRowCount() > 0);
    }
}