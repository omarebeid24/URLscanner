package view;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Panel for displaying detailed technical analysis of a URL
 */
public class DetailPanel extends JPanel {

    // Tab indices
    public static final int SSL_TAB = 0;
    public static final int STRUCTURE_TAB = 1;
    public static final int ML_TAB = 2;
    public static final int FEATURES_TAB = 3;

    private JTabbedPane tabbedPane;
    private SSLPanel sslPanel;
    private StructurePanel structurePanel;
    private MLPanel mlPanel;
    private FeaturesPanel featuresPanel;

    /**
     * Constructor
     */
    public DetailPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Technical Analysis"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        initializeComponents();
        layoutComponents();
    }

    /**
     * Initialize UI components
     */
    private void initializeComponents() {
        tabbedPane = new JTabbedPane();

        sslPanel = new SSLPanel();
        structurePanel = new StructurePanel();
        mlPanel = new MLPanel();
        featuresPanel = new FeaturesPanel();
    }

    /**
     * Layout UI components
     */
    private void layoutComponents() {
        tabbedPane.addTab("SSL Certificate", new ImageIcon(getClass().getResource("/resources/ssl.png")), sslPanel);
        tabbedPane.addTab("URL Structure", new ImageIcon(getClass().getResource("/resources/structure.png")), structurePanel);
        tabbedPane.addTab("ML Classification", new ImageIcon(getClass().getResource("/resources/ml.png")), mlPanel);
        tabbedPane.addTab("Feature Analysis", new ImageIcon(getClass().getResource("/resources/features.png")), featuresPanel);

        add(tabbedPane, BorderLayout.CENTER);
    }

    /**
     * Select a specific tab
     */
    public void setSelectedTab(int tabIndex) {
        if (tabIndex >= 0 && tabIndex < tabbedPane.getTabCount()) {
            tabbedPane.setSelectedIndex(tabIndex);
        }
    }

    /**
     * Clear all detail panels
     */
    public void clearDetails() {
        sslPanel.clearDetails();
        structurePanel.clearDetails();
        mlPanel.clearDetails();
        featuresPanel.clearDetails();
    }

    /**
     * Update SSL verification details
     */
    public void updateSSLDetails(Map<String, Object> sslInfo) {
        sslPanel.updateDetails(sslInfo);
    }

    /**
     * Update URL structure analysis details
     */
    public void updateStructureDetails(Map<String, Object> structureInfo) {
        structurePanel.updateDetails(structureInfo);
    }

    /**
     * Update machine learning classification details
     */
    public void updateMLDetails(Map<String, Object> mlInfo) {
        mlPanel.updateDetails(mlInfo);
    }

    /**
     * Update features analysis details
     */
    public void updateFeaturesDetails(double[] features, String[] featureNames) {
        featuresPanel.updateDetails(features, featureNames);
    }

    /**
     * Panel for SSL Certificate verification details
     */
    class SSLPanel extends JPanel {
        private JTextArea detailsArea;
        private JPanel certificateInfoPanel;
        private JLabel isHttpsLabel;
        private JLabel hasCertificateLabel;
        private JLabel isValidLabel;
        private JLabel matchesDomainLabel;
        private JLabel isSelfSignedLabel;
        private JLabel commonNameLabel;
        private JLabel validToLabel;
        private JLabel daysToExpirationLabel;
        private JLabel issuerLabel;

        /**
         * Constructor
         */
        public SSLPanel() {
            setLayout(new BorderLayout(10, 10));

            // Certificate info panel with grid layout
            certificateInfoPanel = new JPanel(new GridLayout(0, 2, 10, 5));
            certificateInfoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            // Initialize labels
            isHttpsLabel = createInfoLabel("HTTPS:");
            hasCertificateLabel = createInfoLabel("Certificate:");
            isValidLabel = createInfoLabel("Valid:");
            matchesDomainLabel = createInfoLabel("Matches Domain:");
            isSelfSignedLabel = createInfoLabel("Self-Signed:");
            commonNameLabel = createInfoLabel("Common Name:");
            validToLabel = createInfoLabel("Expires:");
            daysToExpirationLabel = createInfoLabel("Days to Expiration:");
            issuerLabel = createInfoLabel("Issuer:");

            // Add labels to panel
            certificateInfoPanel.add(new JLabel("HTTPS:"));
            certificateInfoPanel.add(isHttpsLabel);
            certificateInfoPanel.add(new JLabel("Certificate:"));
            certificateInfoPanel.add(hasCertificateLabel);
            certificateInfoPanel.add(new JLabel("Valid:"));
            certificateInfoPanel.add(isValidLabel);
            certificateInfoPanel.add(new JLabel("Matches Domain:"));
            certificateInfoPanel.add(matchesDomainLabel);
            certificateInfoPanel.add(new JLabel("Self-Signed:"));
            certificateInfoPanel.add(isSelfSignedLabel);
            certificateInfoPanel.add(new JLabel("Common Name:"));
            certificateInfoPanel.add(commonNameLabel);
            certificateInfoPanel.add(new JLabel("Expires:"));
            certificateInfoPanel.add(validToLabel);
            certificateInfoPanel.add(new JLabel("Days to Expiration:"));
            certificateInfoPanel.add(daysToExpirationLabel);
            certificateInfoPanel.add(new JLabel("Issuer:"));
            certificateInfoPanel.add(issuerLabel);

            // Details area for error messages or additional info
            detailsArea = new JTextArea(5, 20);
            detailsArea.setEditable(false);
            detailsArea.setLineWrap(true);
            detailsArea.setWrapStyleWord(true);
            detailsArea.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createTitledBorder("SSL Details"),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)
            ));

            // Add components to panel
            add(new JScrollPane(certificateInfoPanel), BorderLayout.CENTER);
            add(new JScrollPane(detailsArea), BorderLayout.SOUTH);

            // Initialize with empty state
            clearDetails();
        }

        /**
         * Create a styled info label
         */
        private JLabel createInfoLabel(String text) {
            JLabel label = new JLabel();
            label.setFont(new Font("SansSerif", Font.PLAIN, 12));
            return label;
        }

        /**
         * Update SSL details
         */
        public void updateDetails(Map<String, Object> sslInfo) {
            if (sslInfo == null) {
                clearDetails();
                return;
            }

            // Extract values with defaults
            boolean isHttps = (boolean) sslInfo.getOrDefault("isHttps", false);
            boolean hasCertificate = (boolean) sslInfo.getOrDefault("hasCertificate", false);
            boolean isValid = (boolean) sslInfo.getOrDefault("isValid", false);
            boolean matchesDomain = (boolean) sslInfo.getOrDefault("matchesDomain", false);
            boolean isSelfSigned = (boolean) sslInfo.getOrDefault("isSelfSigned", false);
            String commonName = (String) sslInfo.getOrDefault("commonName", "N/A");
            String validTo = (String) sslInfo.getOrDefault("validTo", "N/A");
            long daysToExpiration = Long.parseLong(sslInfo.getOrDefault("daysToExpiration", "0").toString());
            String issuer = (String) sslInfo.getOrDefault("issuer", "N/A");
            String errorMessage = (String) sslInfo.get("errorMessage");

            // Update labels with appropriate colors
            updateStatusLabel(isHttpsLabel, isHttps, isHttps ? "Yes" : "No");
            updateStatusLabel(hasCertificateLabel, hasCertificate, hasCertificate ? "Present" : "Not Found");
            updateStatusLabel(isValidLabel, isValid, isValid ? "Yes" : "No");
            updateStatusLabel(matchesDomainLabel, matchesDomain, matchesDomain ? "Yes" : "No");
            updateStatusLabel(isSelfSignedLabel, !isSelfSigned, isSelfSigned ? "Yes (Not Trusted)" : "No");

            // Update text labels
            commonNameLabel.setText(commonName);
            validToLabel.setText(validTo);

            // Days to expiration with color coding
            boolean isNearingExpiration = daysToExpiration <= 30;
            daysToExpirationLabel.setText(String.valueOf(daysToExpiration));
            daysToExpirationLabel.setForeground(isNearingExpiration ? Color.ORANGE :
                    (daysToExpiration <= 0 ? Color.RED : Color.BLACK));

            // Truncate issuer if too long
            if (issuer.length() > 60) {
                issuerLabel.setText(issuer.substring(0, 57) + "...");
                issuerLabel.setToolTipText(issuer);
            } else {
                issuerLabel.setText(issuer);
                issuerLabel.setToolTipText(null);
            }

            // Show error message if present
            if (errorMessage != null && !errorMessage.isEmpty()) {
                detailsArea.setText("SSL Error: " + errorMessage);
                detailsArea.setForeground(Color.RED);
            } else {
                detailsArea.setText("SSL certificate verification completed successfully.");
                detailsArea.setForeground(Color.BLACK);
            }
        }

        /**
         * Update a status label with appropriate color
         */
        private void updateStatusLabel(JLabel label, boolean isGood, String text) {
            label.setText(text);
            if (isGood) {
                label.setForeground(new Color(0, 150, 0));
                label.setIcon(new ImageIcon(getClass().getResource("/resources/checkgreen.png")));
            } else {
                label.setForeground(new Color(200, 0, 0));
                label.setIcon(new ImageIcon(getClass().getResource("/resources/remove.png")));
            }
        }

        /**
         * Clear all details
         */
        public void clearDetails() {
            isHttpsLabel.setText("N/A");
            isHttpsLabel.setForeground(Color.GRAY);
            isHttpsLabel.setIcon(null);

            hasCertificateLabel.setText("N/A");
            hasCertificateLabel.setForeground(Color.GRAY);
            hasCertificateLabel.setIcon(null);

            isValidLabel.setText("N/A");
            isValidLabel.setForeground(Color.GRAY);
            isValidLabel.setIcon(null);

            matchesDomainLabel.setText("N/A");
            matchesDomainLabel.setForeground(Color.GRAY);
            matchesDomainLabel.setIcon(null);

            isSelfSignedLabel.setText("N/A");
            isSelfSignedLabel.setForeground(Color.GRAY);
            isSelfSignedLabel.setIcon(null);

            commonNameLabel.setText("N/A");
            validToLabel.setText("N/A");
            daysToExpirationLabel.setText("N/A");
            issuerLabel.setText("N/A");

            detailsArea.setText("No SSL verification data available.");
            detailsArea.setForeground(Color.GRAY);
        }
    }

    /**
     * Panel for URL Structure analysis details
     */
    class StructurePanel extends JPanel {
        private JPanel infoPanel;
        private JLabel domainLabel;
        private JLabel subdomainsLabel;
        private JLabel pathDepthLabel;
        private JLabel suspicionScoreLabel;
        private JProgressBar suspicionBar;
        private JLabel riskLevelLabel;
        private JList<String> suspiciousElementsList;

        /**
         * Constructor
         */
        public StructurePanel() {
            setLayout(new BorderLayout(10, 10));

            // Initialize components
            infoPanel = new JPanel(new GridLayout(0, 2, 10, 5));
            infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            domainLabel = new JLabel("N/A");
            subdomainsLabel = new JLabel("N/A");
            pathDepthLabel = new JLabel("N/A");
            suspicionScoreLabel = new JLabel("N/A");
            riskLevelLabel = new JLabel("N/A");

            suspicionBar = new JProgressBar(0, 100);
            suspicionBar.setStringPainted(true);

            // Add info panel components
            infoPanel.add(new JLabel("Domain:"));
            infoPanel.add(domainLabel);
            infoPanel.add(new JLabel("Subdomains:"));
            infoPanel.add(subdomainsLabel);
            infoPanel.add(new JLabel("Path Depth:"));
            infoPanel.add(pathDepthLabel);
            infoPanel.add(new JLabel("Risk Level:"));
            infoPanel.add(riskLevelLabel);
            infoPanel.add(new JLabel("Suspicion Score:"));
            infoPanel.add(suspicionScoreLabel);

            // Suspicious elements list
            suspiciousElementsList = new JList<>(new DefaultListModel<>());
            JScrollPane listScrollPane = new JScrollPane(suspiciousElementsList);
            listScrollPane.setBorder(BorderFactory.createTitledBorder("Suspicious Elements"));

            // Suspicion score panel
            JPanel scorePanel = new JPanel(new BorderLayout(5, 0));
            scorePanel.setBorder(BorderFactory.createTitledBorder("Suspicion Score"));
            scorePanel.add(suspicionBar, BorderLayout.CENTER);

            // Add all components
            JPanel northPanel = new JPanel(new BorderLayout());
            northPanel.add(infoPanel, BorderLayout.CENTER);
            northPanel.add(scorePanel, BorderLayout.SOUTH);

            add(northPanel, BorderLayout.NORTH);
            add(listScrollPane, BorderLayout.CENTER);

            // Initialize with empty state
            clearDetails();
        }

        /**
         * Update structure analysis details
         */
        public void updateDetails(Map<String, Object> structureInfo) {
            if (structureInfo == null) {
                clearDetails();
                return;
            }

            // Extract values with defaults
            String domainName = (String) structureInfo.getOrDefault("domainName", "N/A");
            String domainTLD = (String) structureInfo.getOrDefault("domainTLD", "N/A");
            boolean isIPAddress = (boolean) structureInfo.getOrDefault("isIPAddress", false);
            int subdomainCount = (int) structureInfo.getOrDefault("subdomainCount", 0);
            boolean hasExcessiveSubdomains = (boolean) structureInfo.getOrDefault("hasExcessiveSubdomains", false);
            int pathDepth = (int) structureInfo.getOrDefault("pathDepth", 0);
            boolean hasDeepPathStructure = (boolean) structureInfo.getOrDefault("hasDeepPathStructure", false);
            int suspicionScore = (int) structureInfo.getOrDefault("suspicionScore", 0);
            String riskLevel = (String) structureInfo.getOrDefault("riskLevel", "N/A");
            Object[] suspiciousElements = (Object[]) structureInfo.getOrDefault("suspiciousElements", new Object[0]);

            // Update domain label with IP address indicator if needed
            String domainText = domainName + "." + domainTLD;
            if (isIPAddress) {
                domainText = "[IP Address] " + domainText;
                domainLabel.setForeground(Color.ORANGE);
            } else {
                domainLabel.setForeground(Color.BLACK);
            }
            domainLabel.setText(domainText);

            // Update subdomains with warning if excessive
            String subdomainsText = String.valueOf(subdomainCount);
            if (hasExcessiveSubdomains) {
                subdomainsLabel.setForeground(Color.ORANGE);
                subdomainsText += " (Excessive)";
            } else {
                subdomainsLabel.setForeground(Color.BLACK);
            }
            subdomainsLabel.setText(subdomainsText);

            // Update path depth with warning if deep
            String pathDepthText = String.valueOf(pathDepth);
            if (hasDeepPathStructure) {
                pathDepthLabel.setForeground(Color.ORANGE);
                pathDepthText += " (Deep)";
            } else {
                pathDepthLabel.setForeground(Color.BLACK);
            }
            pathDepthLabel.setText(pathDepthText);

            // Update suspicion score and risk level
            suspicionScoreLabel.setText(suspicionScore + "/100");
            riskLevelLabel.setText(riskLevel);

            // Set risk level label color
            switch (riskLevel) {
                case "High":
                    riskLevelLabel.setForeground(Color.RED);
                    break;
                case "Medium":
                    riskLevelLabel.setForeground(new Color(200, 150, 0));
                    break;
                case "Low":
                    riskLevelLabel.setForeground(new Color(150, 150, 0));
                    break;
                default:
                    riskLevelLabel.setForeground(new Color(0, 150, 0));
                    break;
            }

            // Update suspicion bar
            suspicionBar.setValue(suspicionScore);
            suspicionBar.setString(suspicionScore + "%");

            // Set bar color based on score
            if (suspicionScore >= 70) {
                suspicionBar.setForeground(Color.RED);
            } else if (suspicionScore >= 40) {
                suspicionBar.setForeground(Color.ORANGE);
            } else if (suspicionScore >= 20) {
                suspicionBar.setForeground(Color.YELLOW);
            } else {
                suspicionBar.setForeground(new Color(0, 150, 0));
            }

            // Update suspicious elements list
            DefaultListModel<String> listModel = new DefaultListModel<>();
            for (Object element : suspiciousElements) {
                listModel.addElement(element.toString());
            }
            suspiciousElementsList.setModel(listModel);
        }

        /**
         * Clear all details
         */
        public void clearDetails() {
            domainLabel.setText("N/A");
            domainLabel.setForeground(Color.GRAY);

            subdomainsLabel.setText("N/A");
            subdomainsLabel.setForeground(Color.GRAY);

            pathDepthLabel.setText("N/A");
            pathDepthLabel.setForeground(Color.GRAY);

            suspicionScoreLabel.setText("N/A");
            riskLevelLabel.setText("N/A");
            riskLevelLabel.setForeground(Color.GRAY);

            suspicionBar.setValue(0);
            suspicionBar.setString("N/A");
            suspicionBar.setForeground(Color.GRAY);

            DefaultListModel<String> emptyModel = new DefaultListModel<>();
            emptyModel.addElement("No data available");
            suspiciousElementsList.setModel(emptyModel);
            suspiciousElementsList.setEnabled(false);
        }
    }

    /**
     * Panel for Machine Learning classification details
     */
    class MLPanel extends JPanel {
        private JLabel verdictLabel;
        private JPanel probChart;
        private JProgressBar safeBar;
        private JProgressBar maliciousBar;
        private JTextArea detailsArea;

        /**
         * Constructor
         */
        public MLPanel() {
            setLayout(new BorderLayout(10, 10));

            // ML verdict label
            verdictLabel = new JLabel("No ML data available");
            verdictLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
            verdictLabel.setHorizontalAlignment(SwingConstants.CENTER);
            verdictLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            // Probability chart panel
            probChart = new JPanel(new GridLayout(2, 1, 0, 10));
            probChart.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createTitledBorder("Classification Probabilities"),
                    BorderFactory.createEmptyBorder(10, 10, 10, 10)
            ));

            // Safe probability bar
            JPanel safePanel = new JPanel(new BorderLayout(10, 0));
            safePanel.add(new JLabel("Safe:"), BorderLayout.WEST);
            safeBar = new JProgressBar(0, 100);
            safeBar.setStringPainted(true);
            safeBar.setForeground(new Color(0, 150, 0));
            safePanel.add(safeBar, BorderLayout.CENTER);

            // Malicious probability bar
            JPanel maliciousPanel = new JPanel(new BorderLayout(10, 0));
            maliciousPanel.add(new JLabel("Malicious:"), BorderLayout.WEST);
            maliciousBar = new JProgressBar(0, 100);
            maliciousBar.setStringPainted(true);
            maliciousBar.setForeground(new Color(200, 0, 0));
            maliciousPanel.add(maliciousBar, BorderLayout.CENTER);

            // Add bars to chart
            probChart.add(safePanel);
            probChart.add(maliciousPanel);

            // Details area
            detailsArea = new JTextArea(5, 20);
            detailsArea.setEditable(false);
            detailsArea.setLineWrap(true);
            detailsArea.setWrapStyleWord(true);
            detailsArea.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createTitledBorder("ML Classification Details"),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)
            ));

            // Add components to panel
            add(verdictLabel, BorderLayout.NORTH);
            add(probChart, BorderLayout.CENTER);
            add(new JScrollPane(detailsArea), BorderLayout.SOUTH);

            // Initialize with empty state
            clearDetails();
        }

        /**
         * Update ML classification details
         */
        public void updateDetails(Map<String, Object> mlInfo) {
            if (mlInfo == null) {
                clearDetails();
                return;
            }

            // Extract values with defaults
            String verdict = (String) mlInfo.getOrDefault("verdict", "N/A");
            double[] probabilities = (double[]) mlInfo.getOrDefault("probabilities", new double[]{0, 0});
            String reason = (String) mlInfo.getOrDefault("reason", "No details available");

            // Update verdict label with appropriate color
            verdictLabel.setText("ML Classification: " + verdict.toUpperCase());

            if (verdict.equalsIgnoreCase("good") || verdict.equalsIgnoreCase("safe")) {
                verdictLabel.setForeground(new Color(0, 150, 0));
            } else {
                verdictLabel.setForeground(new Color(200, 0, 0));
            }

            // Update probability bars if available
            if (probabilities.length >= 2) {
                int safePct = (int)(probabilities[0] * 100);
                int maliciousPct = (int)(probabilities[1] * 100);

                safeBar.setValue(safePct);
                safeBar.setString(safePct + "%");

                maliciousBar.setValue(maliciousPct);
                maliciousBar.setString(maliciousPct + "%");
            }

            // Update details text
            detailsArea.setText(reason);
            detailsArea.setForeground(Color.BLACK);
        }

        /**
         * Clear all details
         */
        public void clearDetails() {
            verdictLabel.setText("No ML data available");
            verdictLabel.setForeground(Color.GRAY);

            safeBar.setValue(0);
            safeBar.setString("N/A");

            maliciousBar.setValue(0);
            maliciousBar.setString("N/A");

            detailsArea.setText("No ML classification data available.");
            detailsArea.setForeground(Color.GRAY);
        }
    }

    /**
     * Panel for URL feature analysis details
     */
    class FeaturesPanel extends JPanel {
        private JPanel featuresGrid;
        private Map<String, FeatureDisplay> featureDisplays;

        /**
         * Constructor
         */
        public FeaturesPanel() {
            setLayout(new BorderLayout());

            // Create scrollable grid for features
            featuresGrid = new JPanel(new GridLayout(0, 1, 5, 5));
            featuresGrid.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            JScrollPane scrollPane = new JScrollPane(featuresGrid);
            scrollPane.setBorder(BorderFactory.createEmptyBorder());

            add(scrollPane, BorderLayout.CENTER);

            // Initialize feature displays map
            featureDisplays = new HashMap<>();

            // Initialize with empty state
            clearDetails();
        }

        /**
         * Update feature analysis details
         */
        public void updateDetails(double[] features, String[] featureNames) {
            featuresGrid.removeAll();
            featureDisplays.clear();

            if (features == null || featureNames == null || features.length == 0) {
                JLabel emptyLabel = new JLabel("No feature data available");
                emptyLabel.setHorizontalAlignment(SwingConstants.CENTER);
                featuresGrid.add(emptyLabel);
                return;
            }

            // Add title label
            JLabel titleLabel = new JLabel("URL Feature Vector Analysis");
            titleLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
            titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
            titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
            featuresGrid.add(titleLabel);

            // Add each feature with its value
            for (int i = 0; i < Math.min(features.length, featureNames.length); i++) {
                String name = featureNames[i];
                double value = features[i];
                boolean isSuspicious = isFeatureSuspicious(i, value);

                FeatureDisplay display = new FeatureDisplay(name, value, isSuspicious);
                featureDisplays.put(name, display);
                featuresGrid.add(display);
            }

            featuresGrid.revalidate();
            featuresGrid.repaint();
        }

        /**
         * Determines if a feature value is suspicious
         */
        private boolean isFeatureSuspicious(int featureIndex, double value) {
            switch (featureIndex) {
                case 0: return value > 100;  // Long URL
                case 1: return value > 0;    // Has IP
                case 2: return value == 0;   // No HTTPS
                case 3: return value > 5;    // Many dots
                case 4: return value > 0;    // Has @ symbol
                case 5: return value > 5;    // Deep URL path
                case 6: return value > 0;    // Is shortened
                case 7: return value > 0;    // Has suspicious words
                case 8: return value > 1;    // Multiple suspicious words
                case 9: return value > 0;    // Has encoded chars
                case 10: return value > 2;   // Many subdomains
                case 11: return value > 4.5; // High domain entropy
                case 12: return value > 0;   // Has port number
                default: return false;
            }
        }

        /**
         * Clear all details
         */
        public void clearDetails() {
            featuresGrid.removeAll();
            featureDisplays.clear();

            JLabel emptyLabel = new JLabel("No feature data available");
            emptyLabel.setHorizontalAlignment(SwingConstants.CENTER);
            featuresGrid.add(emptyLabel);

            featuresGrid.revalidate();
            featuresGrid.repaint();
        }

        /**
         * Custom component for displaying a feature with its value
         */
        class FeatureDisplay extends JPanel {
            private JLabel nameLabel;
            private JLabel valueLabel;
            private JProgressBar valueBar;

            /**
             * Constructor
             */
            public FeatureDisplay(String name, double value, boolean isSuspicious) {
                setLayout(new BorderLayout(10, 0));
                setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

                // Name label
                nameLabel = new JLabel(name + ":");
                nameLabel.setPreferredSize(new Dimension(150, 20));

                // Value label - format based on type
                String formattedValue = String.format("%.2f", value);
                // For boolean-like features (0 or 1), add textual representation
                if (value == 0 || value == 1) {
                    formattedValue += value == 1 ? " (Yes)" : " (No)";
                }

                valueLabel = new JLabel(formattedValue);
                valueLabel.setPreferredSize(new Dimension(80, 20));

                // Value bar - visualize value
                valueBar = new JProgressBar(0, 100);
                valueBar.setValue((int)(value * 100)); // Normalize to 0-100 for display

                // If suspicious, highlight in yellow/red
                if (isSuspicious) {
                    setBackground(new Color(255, 250, 205)); // Light yellow
                    setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(new Color(255, 200, 0), 1),
                            BorderFactory.createEmptyBorder(2, 2, 2, 2)
                    ));
                    nameLabel.setForeground(new Color(180, 0, 0));

                    // For boolean features, if true is suspicious, show in red
                    if (value > 0) {
                        valueBar.setForeground(new Color(200, 0, 0));
                    }

                    // Add warning icon
                    add(new JLabel(new ImageIcon(getClass().getResource("/resources/warning.png"))),
                            BorderLayout.WEST);
                } else {
                    valueBar.setForeground(new Color(0, 150, 0));
                }

                // Add components
                JPanel labelPanel = new JPanel(new BorderLayout());
                labelPanel.setOpaque(false);
                labelPanel.add(nameLabel, BorderLayout.WEST);
                labelPanel.add(valueLabel, BorderLayout.EAST);

                add(labelPanel, BorderLayout.NORTH);
                add(valueBar, BorderLayout.CENTER);
            }
        }
    }
}