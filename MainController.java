package controller;

import backend.URLSafetyChecker;
import view.MainFrame;

/**
 * Main controller for the URL Safety Analyzer application.
 * Serves as the central coordinator connecting models and views.
 */
public class MainController {

    // Model components
    private URLSafetyChecker urlSafetyChecker;

    // View components
    private MainFrame mainFrame;

    // Sub-controllers
    private URLCheckController urlCheckController;
    private BatchController batchController;

    /**
     * Constructor
     */
    public MainController() {
        initializeModel();
        initializeControllers();
    }

    /**
     * Initialize the model components
     */
    private void initializeModel() {
        try {
            // Initialize the URL safety checker with the ML model
            urlSafetyChecker = new URLSafetyChecker("models/newmodel4.model");
        } catch (Exception e) {
            System.err.println("Error initializing model: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Initialize the sub-controllers
     */
    private void initializeControllers() {
        urlCheckController = new URLCheckController(this);
        batchController = new BatchController(this);
    }

    /**
     * Initialize the main view
     */
    public void initializeView() {
        // Create and show the main frame
        mainFrame = new MainFrame(this);
        mainFrame.setVisible(true);
    }

    /**
     * Get the URL safety checker model
     */
    public URLSafetyChecker getURLSafetyChecker() {
        return urlSafetyChecker;
    }

    /**
     * Get the URL check controller
     */
    public URLCheckController getURLCheckController() {
        return urlCheckController;
    }

    /**
     * Get the batch controller
     */
    public BatchController getBatchController() {
        return batchController;
    }

    /**
     * Get the main frame
     */
    public MainFrame getMainFrame() {
        return mainFrame;
    }

    /**
     * Show an error dialog
     */
    public void showError(String title, String message) {
        javax.swing.JOptionPane.showMessageDialog(
                mainFrame,
                message,
                title,
                javax.swing.JOptionPane.ERROR_MESSAGE
        );
    }

    /**
     * Show a message dialog
     */
    public void showMessage(String title, String message) {
        javax.swing.JOptionPane.showMessageDialog(
                mainFrame,
                message,
                title,
                javax.swing.JOptionPane.INFORMATION_MESSAGE
        );
    }

    /**
     * Main method to start the application
     */
    public static void main(String[] args) {
        // Set look and feel to system
        try {
            javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Could not set look and feel: " + e.getMessage());
        }

        // Create controller and initialize application
        javax.swing.SwingUtilities.invokeLater(() -> {
            MainController controller = new MainController();
            controller.initializeView();
        });
    }
}