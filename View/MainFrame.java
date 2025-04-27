package view;



import controller.MainController;
import controller.URLCheckController;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * The main application window for the URL Safety Analyzer view.
 * This class represents the top-level container for all UI components.
 */
public class MainFrame extends JFrame {

    // UI Components
    private JTabbedPane tabbedPane;
    private URLInputPanel urlInputPanel;
    private ResultPanel resultPanel;
    private DetailPanel detailPanel;
    private BatchPanel batchPanel;
    private HelpPanel helpPanel;

    // Controllers
    private MainController mainController;
    private URLCheckController urlCheckController;

    /**
     * Constructor sets up the main application window
     *
     * @param mainController The main application controller
     */
    public MainFrame(MainController mainController) {
        this.mainController = mainController;

        setupFrame();
        initializeComponents();
        layoutComponents();

        // Register window close event
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onExit();
            }
        });
    }

    /**
     * Sets up the frame properties
     */
    private void setupFrame() {
        setTitle("URL Safety Analyzer Professional v4.0");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setPreferredSize(new Dimension(1000, 700));
        setIconImage(new ImageIcon(getClass().getResource("/resources/icon.png")).getImage());
    }

    /**
     * Initializes all UI components
     */
    private void initializeComponents() {
        // Create tabbed pane
        tabbedPane = new JTabbedPane();

        // Create panels
        urlInputPanel = new URLInputPanel(mainController);
        resultPanel = new ResultPanel();
        detailPanel = new DetailPanel();
        batchPanel = new BatchPanel(mainController);
        helpPanel = new HelpPanel();

        // Set the URL check controller
        urlCheckController = mainController.getURLCheckController();
        urlCheckController.setResultPanel(resultPanel);
        urlCheckController.setDetailPanel(detailPanel);

        // Connect URLInputPanel to the URL check controller
        urlInputPanel.setURLCheckController(urlCheckController);
    }

    /**
     * Lays out components in the frame
     */
    private void layoutComponents() {
        // Create main input panel with URL input and results
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Add URL input at top
        mainPanel.add(urlInputPanel, BorderLayout.NORTH);

        // Create split pane for results and details
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, resultPanel, detailPanel);
        splitPane.setResizeWeight(0.3); // 30% to results, 70% to details
        splitPane.setDividerLocation(200);
        mainPanel.add(splitPane, BorderLayout.CENTER);

        // Add tabs
        tabbedPane.addTab("Check URL", new ImageIcon(getClass().getResource("/resources/check_url.png")), mainPanel);
        tabbedPane.addTab("Batch Check", new ImageIcon(getClass().getResource("/resources/batch.png")), batchPanel);
        tabbedPane.addTab("Help", new ImageIcon(getClass().getResource("/resources/help.png")), helpPanel);

        // Add tabbed pane to frame
        this.add(tabbedPane);

        // Add status bar
        JPanel statusBar = createStatusBar();
        this.add(statusBar, BorderLayout.SOUTH);

        // Set icons for tabs
        tabbedPane.setIconAt(0, new ImageIcon(getClass().getResource("/resources/check_url.png")));
        tabbedPane.setIconAt(1, new ImageIcon(getClass().getResource("/resources/batch.png")));
        tabbedPane.setIconAt(2, new ImageIcon(getClass().getResource("/resources/help.png")));

        // Pack and center the frame
        pack();
        setLocationRelativeTo(null);
    }

    /**
     * Creates the application status bar
     */
    private JPanel createStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBorder(BorderFactory.createEtchedBorder());

        JLabel statusLabel = new JLabel(" Model and analyzers loaded successfully");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));

        JLabel versionLabel = new JLabel("v4.0 ");
        versionLabel.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));

        statusBar.add(statusLabel, BorderLayout.WEST);
        statusBar.add(versionLabel, BorderLayout.EAST);

        return statusBar;
    }

    /**
     * Handle application exit
     */
    private void onExit() {
        int option = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to exit URL Safety Analyzer?",
                "Exit Application",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (option == JOptionPane.YES_OPTION) {
            dispose();
            System.exit(0);
        }
    }

    /**
     * Shows the detail panel with the specified tab selected
     */
    public void showDetailTab(int tabIndex) {
        detailPanel.setSelectedTab(tabIndex);
    }

    /**
     * Shows or hides URL validation suggestions
     */
    public void showURLSuggestion(String suggestion) {
        urlInputPanel.showSuggestion(suggestion);
    }

    /**
     * Clear all results
     */
    public void clearResults() {
        resultPanel.clearResults();
        detailPanel.clearDetails();
    }

    /**
     * Updates the analysis status
     */
    public void setAnalysisStatus(String status) {
        // Update status bar or other UI elements
    }
}