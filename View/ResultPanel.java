package view;


import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Panel for displaying URL safety analysis results
 */
public class ResultPanel extends JPanel {

    // UI Components
    private JPanel contentPanel;
    private JLabel urlLabel;
    private JLabel timestampLabel;
    private JPanel verdictPanel;
    private JLabel verdictLabel;
    private JLabel verdictIconLabel;
    private JProgressBar confidenceBar;
    private JLabel confidenceLabel;
    private JTextArea recommendationArea;

    // Colors for different verdicts
    private static final Color SAFE_COLOR = new Color(0, 150, 0);
    private static final Color PROBABLY_SAFE_COLOR = new Color(0, 120, 0);
    private static final Color UNCERTAIN_COLOR = new Color(200, 150, 0);
    private static final Color SUSPICIOUS_COLOR = new Color(200, 100, 0);
    private static final Color DANGEROUS_COLOR = new Color(200, 0, 0);

    /**
     * Constructor
     */
    public ResultPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Analysis Results"),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        initializeComponents();
        layoutComponents();

        // Initially show as empty
        showEmptyState();
    }

    /**
     * Initialize UI components
     */
    private void initializeComponents() {
        // Main content panel with spacing
        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // URL and timestamp info
        urlLabel = new JLabel("URL: ");
        urlLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));

        timestampLabel = new JLabel("Analysis Time: ");
        timestampLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        timestampLabel.setForeground(Color.DARK_GRAY);

        // Verdict panel with icon and text
        verdictPanel = new JPanel(new BorderLayout(10, 0));
        verdictPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        verdictIconLabel = new JLabel();
        verdictIconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        verdictIconLabel.setPreferredSize(new Dimension(48, 48));

        verdictLabel = new JLabel("VERDICT");
        verdictLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        verdictLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Confidence score components
        confidenceBar = new JProgressBar(0, 100);
        confidenceBar.setStringPainted(true);
        confidenceBar.setPreferredSize(new Dimension(100, 20));

        confidenceLabel = new JLabel("Confidence: 0%");
        confidenceLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));

        // Recommendation area
        recommendationArea = new JTextArea(2, 20);
        recommendationArea.setLineWrap(true);
        recommendationArea.setWrapStyleWord(true);
        recommendationArea.setEditable(false);
        recommendationArea.setBackground(new Color(245, 245, 245));
        recommendationArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Recommendation"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
    }

    /**
     * Layout UI components
     */
    private void layoutComponents() {
        // Button panel at bottom
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton detailsButton = new JButton("View Technical Details");
        detailsButton.addActionListener(e -> requestShowDetails());
        buttonPanel.add(detailsButton);

        // Verdict panel layout
        JPanel verdictContent = new JPanel();
        verdictContent.setLayout(new BoxLayout(verdictContent, BoxLayout.Y_AXIS));

        verdictLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel confidencePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        confidencePanel.add(confidenceLabel);
        confidencePanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        verdictContent.add(verdictLabel);
        verdictContent.add(Box.createRigidArea(new Dimension(0, 5)));
        verdictContent.add(confidencePanel);

        verdictPanel.add(verdictIconLabel, BorderLayout.WEST);
        verdictPanel.add(verdictContent, BorderLayout.CENTER);

        // Info panel with URL and timestamp
        JPanel infoPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        infoPanel.add(urlLabel);
        infoPanel.add(timestampLabel);

        // Confidence bar panel
        JPanel confidenceBarPanel = new JPanel(new BorderLayout(5, 0));
        confidenceBarPanel.setBorder(BorderFactory.createTitledBorder("Risk Level"));
        confidenceBarPanel.add(confidenceBar, BorderLayout.CENTER);

        // Add components to content panel
        contentPanel.add(infoPanel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        contentPanel.add(verdictPanel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        contentPanel.add(confidenceBarPanel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        contentPanel.add(recommendationArea);

        // Add content and button panels to main panel
        add(contentPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * Show the empty initial state
     */
    private void showEmptyState() {
        urlLabel.setText("URL: ");
        timestampLabel.setText("Analysis Time: ");
        verdictLabel.setText("No Analysis Yet");
        verdictIconLabel.setIcon(null);
        verdictPanel.setBackground(new Color(240, 240, 240));
        verdictLabel.setForeground(Color.GRAY);
        confidenceBar.setValue(0);
        confidenceBar.setForeground(Color.GRAY);
        confidenceLabel.setText("Confidence: N/A");
        recommendationArea.setText("Enter a URL to analyze its safety.");
    }

    /**
     * Update the panel with safety analysis results
     *
     * @param url The analyzed URL
     * @param verdict The safety verdict (SAFE, SUSPICIOUS, etc.)
     * @param confidence The confidence score (0-100)
     * @param recommendation The recommendation text
     * @param isSafe Whether the URL is considered safe
     */
    public void updateResults(String url, String verdict, double confidence,
                              String recommendation, boolean isSafe) {

        // Update URL and timestamp
        urlLabel.setText("URL: " + url);
        timestampLabel.setText("Analysis Time: " +
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));

        // Update verdict with appropriate colors and icons
        verdictLabel.setText(verdict);

        // Set icon and colors based on verdict
        Color verdictColor;
        ImageIcon verdictIcon;

        switch (verdict.toUpperCase()) {
            case "SAFE":
                verdictColor = SAFE_COLOR;
                verdictIcon = new ImageIcon(getClass().getResource("/resources/safe.png"));
                break;
            case "PROBABLY SAFE":
                verdictColor = PROBABLY_SAFE_COLOR;
                verdictIcon = new ImageIcon(getClass().getResource("/resources/probablysafe.png"));
                break;
            case "UNCERTAIN":
                verdictColor = UNCERTAIN_COLOR;
                verdictIcon = new ImageIcon(getClass().getResource("/resources/uncertain.png"));
                break;
            case "SUSPICIOUS":
                verdictColor = SUSPICIOUS_COLOR;
                verdictIcon = new ImageIcon(getClass().getResource("/resources/suspicious.png"));
                break;
            case "DANGEROUS":
                verdictColor = DANGEROUS_COLOR;
                verdictIcon = new ImageIcon(getClass().getResource("/resources/alarm.png"));
                break;
            default:
                verdictColor = Color.GRAY;
                verdictIcon = new ImageIcon(getClass().getResource("/resources/unknown.png"));
        }

        verdictLabel.setForeground(verdictColor);
        verdictIconLabel.setIcon(verdictIcon);
        verdictPanel.setBackground(new Color(245, 245, 245));

        // Update confidence score
        int confidenceValue = (int)(confidence * 100);
        confidenceBar.setValue(confidenceValue);
        confidenceBar.setString(confidenceValue + "%");
        confidenceLabel.setText("Confidence: " + confidenceValue + "%");

        // Set risk level color based on whether URL is safe or not
        if (isSafe) {
            confidenceBar.setForeground(SAFE_COLOR);
        } else {
            confidenceBar.setForeground(DANGEROUS_COLOR);
        }

        // Update recommendation
        recommendationArea.setText(recommendation);
    }

    /**
     * Request to show detailed technical analysis
     */
    private void requestShowDetails() {
        firePropertyChange("showDetails", false, true);
    }

    /**
     * Clear all results
     */
    public void clearResults() {
        showEmptyState();
    }
}