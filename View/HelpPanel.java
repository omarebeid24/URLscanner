package view;



import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Panel for help and information about the application
 */
public class HelpPanel extends JPanel {

    private JTabbedPane tabbedPane;

    /**
     * Constructor
     */
    public HelpPanel() {
        setLayout(new BorderLayout());

        tabbedPane = new JTabbedPane();

        initializeComponents();
    }

    /**
     * Initialize the components
     */
    private void initializeComponents() {
        // About panel
        tabbedPane.addTab("About", createAboutPanel());

        // User guide panel
        tabbedPane.addTab("User Guide", createUserGuidePanel());

        // Security tips panel
        tabbedPane.addTab("Security Tips", createSecurityTipsPanel());

        // Add tabbed pane to main panel
        add(tabbedPane, BorderLayout.CENTER);
    }

    /**
     * Create the About panel
     */
    private JScrollPane createAboutPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Add logo
        JLabel logoLabel = new JLabel(new ImageIcon(getClass().getResource("/resources/logo.png")));
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(logoLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Add application title
        JLabel titleLabel = new JLabel("URL Safety Analyzer Professional v4.0");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Add subtitle
        JLabel subtitleLabel = new JLabel("ML · SSL · Structure Analysis · Whitelist");
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitleLabel.setForeground(Color.GRAY);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(subtitleLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 30)));

        // Add description
        String description = "<html><div style='text-align: center; width: 400px;'>"
                + "URL Safety Analyzer combines multiple technologies to detect phishing and malicious URLs "
                + "with high accuracy. The application uses machine learning, SSL certificate verification, "
                + "URL structure analysis, and a trusted domain whitelist to provide comprehensive protection.</div></html>";

        JLabel descLabel = new JLabel(description);
        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(descLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 30)));

        // Add website link button
        JButton websiteButton = new JButton("Visit Website");
        websiteButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        websiteButton.addActionListener(e -> openWebsite("https://www.urlsafetyanalyzer.com"));
        panel.add(websiteButton);

        return new JScrollPane(panel);
    }

    /**
     * Create the User Guide panel
     */
    private JPanel createUserGuidePanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Create panel for main content using vertical BoxLayout
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        // Title
        JLabel titleLabel = new JLabel("User Guide");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Section 1: Basic Usage
        contentPanel.add(createSectionPanel("Basic Usage", new String[] {
                "1. Enter a URL in the input field",
                "2. Click 'Check URL Safety' or press Enter",
                "3. View the safety verdict and recommendations",
                "4. Click 'View Technical Details' for in-depth analysis"
        }));
        contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Section 2: Batch Processing
        contentPanel.add(createSectionPanel("Batch URL Processing", new String[] {
                "1. Go to the 'Batch Check' tab",
                "2. Enter multiple URLs (one per line) or import from a file",
                "3. Click 'Check URLs' to analyze all URLs",
                "4. Review results in the table",
                "5. Export results to CSV or TXT file if needed"
        }));
        contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Section 3: Understanding Results
        contentPanel.add(createSectionPanel("Understanding Results", new String[] {
                "SAFE - URL passes all security checks and is likely safe to visit",
                "PROBABLY SAFE - URL has good security indicators but isn't in the whitelist",
                "UNCERTAIN - Mixed results from different analysis methods",
                "SUSPICIOUS - URL has concerning characteristics, use caution",
                "DANGEROUS - URL is likely malicious and should be avoided"
        }));
        contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Section 4: Technical Details
        contentPanel.add(createSectionPanel("Technical Analysis Tabs", new String[] {
                "SSL Certificate - Shows HTTPS and certificate information",
                "URL Structure - Analyzes patterns associated with phishing URLs",
                "ML Classification - Shows machine learning model results",
                "Feature Analysis - Displays the 13 features used for detection"
        }));

        // Add content to scrollable panel
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Create the Security Tips panel
     */
    private JPanel createSecurityTipsPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Create panel for main content using vertical BoxLayout
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        // Title
        JLabel titleLabel = new JLabel("Online Safety Best Practices");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Add security tips sections
        contentPanel.add(createSectionPanel("URL Safety", new String[] {
                "Always check the domain name carefully before entering credentials",
                "Look for HTTPS with a valid certificate (padlock icon in browser)",
                "Be wary of URLs containing random characters or numbers",
                "Verify the spelling of domain names (e.g., amaz0n.com vs amazon.com)",
                "Check for excessive subdomains or unusual URL structures"
        }));
        contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        contentPanel.add(createSectionPanel("Email Safety", new String[] {
                "Avoid clicking links in unsolicited emails",
                "Hover over links to preview the URL before clicking",
                "Be suspicious of urgent requests or threats",
                "Never open attachments from unknown senders",
                "When in doubt, navigate to websites directly rather than using email links"
        }));
        contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        contentPanel.add(createSectionPanel("Password Security", new String[] {
                "Use unique passwords for each important account",
                "Create strong passwords with a mix of characters, numbers, and symbols",
                "Consider using a password manager",
                "Enable two-factor authentication when available",
                "Change passwords periodically, especially after data breaches"
        }));
        contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        contentPanel.add(createSectionPanel("General Tips", new String[] {
                "Keep your operating system and browsers updated",
                "Install security software and keep it updated",
                "Be cautious about what information you share online",
                "Regularly check your financial statements for unauthorized transactions",
                "Educate yourself about common phishing techniques"
        }));

        // Add a footer note
        JLabel noteLabel = new JLabel("<html><em>This tool is designed for informational purposes only. "
                + "Always exercise caution online.</em></html>");
        noteLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        noteLabel.setForeground(Color.GRAY);
        noteLabel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        contentPanel.add(noteLabel);

        // Add content to scrollable panel
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Create a section panel with a title and bullet points
     */
    private JPanel createSectionPanel(String title, String[] bullets) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        // Section title
        JLabel sectionLabel = new JLabel(title);
        sectionLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        sectionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(sectionLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));

        // Create bullet points
        for (String bullet : bullets) {
            JLabel bulletLabel = new JLabel("• " + bullet);
            bulletLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.add(bulletLabel);
            panel.add(Box.createRigidArea(new Dimension(0, 5)));
        }

        return panel;
    }

    /**
     * Open a website URL
     */
    private void openWebsite(String url) {
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (IOException | URISyntaxException e) {
            JOptionPane.showMessageDialog(this,
                    "Could not open the website: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}