package view;


import controller.MainController;
import controller.URLCheckController;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel for URL input and validation
 */
public class URLInputPanel extends JPanel {

    private JTextField urlTextField;
    private JButton checkButton;
    private JComboBox<String> historyComboBox;
    private JLabel statusLabel;
    private JPanel suggestionPanel;
    private JLabel suggestionLabel;
    private JButton useSuggestionButton;

    private List<String> urlHistory;
    private URLCheckController urlCheckController;
    private MainController mainController;

    /**
     * Constructor
     */
    public URLInputPanel(MainController mainController) {
        this.mainController = mainController;
        this.urlHistory = new ArrayList<>();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("URL Input"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        initializeComponents();
        layoutComponents();
    }

    /**
     * Initialize UI components
     */
    private void initializeComponents() {
        // URL input field
        urlTextField = new JTextField();
        urlTextField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        urlTextField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                // Enter key triggers URL check
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    checkURL();
                }

                // Hide suggestion panel when user starts typing
                if (suggestionPanel.isVisible()) {
                    suggestionPanel.setVisible(false);
                }
            }
        });

        // Check button
        checkButton = new JButton("Check URL Safety");
        checkButton.setIcon(new ImageIcon(getClass().getResource("/resources/search.png")));
        checkButton.addActionListener(this::onCheckButtonClicked);

        // URL history dropdown
        historyComboBox = new JComboBox<>();
        historyComboBox.setToolTipText("Recently checked URLs");
        historyComboBox.setEditable(false);
        historyComboBox.addActionListener(this::onHistorySelected);

        // Status label
        statusLabel = new JLabel("Enter a URL to check");
        statusLabel.setForeground(Color.GRAY);

        // Suggestion panel (hidden by default)
        suggestionPanel = new JPanel(new BorderLayout(5, 0));
        suggestionPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        suggestionPanel.setVisible(false);

        suggestionLabel = new JLabel("Did you mean: ");
        suggestionLabel.setForeground(new Color(0, 100, 0));

        useSuggestionButton = new JButton("Use This");
        useSuggestionButton.setForeground(new Color(0, 100, 0));
        useSuggestionButton.addActionListener(this::onUseSuggestionClicked);

        JPanel suggestionContent = new JPanel(new BorderLayout(5, 0));
        suggestionContent.add(suggestionLabel, BorderLayout.CENTER);
        suggestionContent.add(useSuggestionButton, BorderLayout.EAST);

        suggestionPanel.add(new JLabel(new ImageIcon(getClass().getResource("/resources/suggestion.png"))), BorderLayout.WEST);
        suggestionPanel.add(suggestionContent, BorderLayout.CENTER);
    }

    /**
     * Layout the UI components
     */
    private void layoutComponents() {
        // URL input and buttons panel
        JPanel inputPanel = new JPanel(new BorderLayout(10, 0));

        // History dropdown on the left
        inputPanel.add(historyComboBox, BorderLayout.WEST);

        // URL text field in the center
        inputPanel.add(urlTextField, BorderLayout.CENTER);

        // Check button on the right
        inputPanel.add(checkButton, BorderLayout.EAST);

        // Add input panel at the top
        add(inputPanel, BorderLayout.NORTH);

        // Status label below input
        add(statusLabel, BorderLayout.CENTER);

        // Suggestion panel at the bottom (hidden initially)
        add(suggestionPanel, BorderLayout.SOUTH);
    }

    /**
     * Set the URL Check Controller
     */
    public void setURLCheckController(URLCheckController urlCheckController) {
        this.urlCheckController = urlCheckController;
    }

    /**
     * Handle check button click
     */
    private void onCheckButtonClicked(ActionEvent e) {
        checkURL();
    }

    /**
     * Handle history selection
     */
    private void onHistorySelected(ActionEvent e) {
        if (historyComboBox.getSelectedIndex() > 0) {
            urlTextField.setText((String) historyComboBox.getSelectedItem());
        }
    }

    /**
     * Handle use suggestion button click
     */
    private void onUseSuggestionClicked(ActionEvent e) {
        String suggestion = suggestionLabel.getText().replace("Did you mean: ", "");
        urlTextField.setText(suggestion);
        suggestionPanel.setVisible(false);
        checkURL();
    }

    /**
     * Show URL suggestion
     */
    public void showSuggestion(String suggestion) {
        if (suggestion != null && !suggestion.isEmpty()) {
            suggestionLabel.setText("Did you mean: " + suggestion);
            suggestionPanel.setVisible(true);
        } else {
            suggestionPanel.setVisible(false);
        }
    }

    /**
     * Check the URL by passing it to the controller
     */
    private void checkURL() {
        String url = urlTextField.getText().trim();

        if (url.isEmpty()) {
            statusLabel.setText("Please enter a URL");
            statusLabel.setForeground(Color.RED);
            return;
        }

        // Set status to "checking..."
        statusLabel.setText("Analyzing URL... Please wait.");
        statusLabel.setForeground(Color.BLUE);

        // Hide suggestion panel
        suggestionPanel.setVisible(false);

        // Disable input while checking
        setInputEnabled(false);

        // Start URL check in background thread
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                urlCheckController.checkURL(url);
                return null;
            }

            @Override
            protected void done() {
                // Re-enable input
                setInputEnabled(true);

                // Add to history if not already present
                addToHistory(url);

                // Reset status
                statusLabel.setText("Enter a URL to check");
                statusLabel.setForeground(Color.GRAY);
            }
        };

        worker.execute();
    }

    /**
     * Add URL to history
     */
    private void addToHistory(String url) {
        // Prevent duplicates
        if (!urlHistory.contains(url)) {
            urlHistory.add(0, url);

            // Limit history size
            if (urlHistory.size() > 10) {
                urlHistory.remove(urlHistory.size() - 1);
            }

            // Update combo box
            updateHistoryComboBox();
        }
    }

    /**
     * Update the history combo box with current history items
     */
    private void updateHistoryComboBox() {
        historyComboBox.removeAllItems();
        historyComboBox.addItem("URL History");

        for (String url : urlHistory) {
            historyComboBox.addItem(url);
        }

        historyComboBox.setSelectedIndex(0);
    }

    /**
     * Enable or disable input components
     */
    private void setInputEnabled(boolean enabled) {
        urlTextField.setEnabled(enabled);
        checkButton.setEnabled(enabled);
        historyComboBox.setEnabled(enabled);
        useSuggestionButton.setEnabled(enabled);
    }

    /**
     * Clear the URL input field
     */
    public void clearInput() {
        urlTextField.setText("");
        suggestionPanel.setVisible(false);
    }
}