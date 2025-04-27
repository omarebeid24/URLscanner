package controller;

import backend.URLSafetyChecker;
import backend.SSLVerificationService;
import backend.URLStructureAnalyzer;
import backend.URLValidator;
import view.DetailPanel;
import view.ResultPanel;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Controller for handling URL safety checks.
 * Acts as intermediary between view components and URL safety models.
 */
public class URLCheckController {

    private MainController mainController;
    private ResultPanel resultPanel;
    private DetailPanel detailPanel;

    // Feature names for displaying in the Features tab
    private static final String[] FEATURE_NAMES = {
            "URL length", "Has IP address", "Has HTTPS", "Number of dots",
            "Has @ symbol", "URL depth", "Is shortened URL", "Has suspicious words",
            "Suspicious word count", "Has encoded chars", "Number of subdomains",
            "Domain entropy", "Has port number"
    };

    /**
     * Constructor
     */
    public URLCheckController(MainController mainController) {
        this.mainController = mainController;
    }

    /**
     * Set the result panel
     */
    public void setResultPanel(ResultPanel resultPanel) {
        this.resultPanel = resultPanel;
    }

    /**
     * Set the detail panel
     */
    public void setDetailPanel(DetailPanel detailPanel) {
        this.detailPanel = detailPanel;
    }

    /**
     * Check a URL's safety
     *
     * @param url The URL to check
     */
    public void checkURL(String url) {
        // First validate the URL
        URLValidator.ValidationResult validationResult = URLValidator.validate(url);

        if (!validationResult.isValid) {
            // Show error message
            mainController.showError("Invalid URL", validationResult.errorMessage);

            // Show suggestion if available
            if (validationResult.suggestion != null) {
                int option = JOptionPane.showConfirmDialog(
                        mainController.getMainFrame(),
                        "Did you mean " + validationResult.suggestion + "?",
                        "URL Suggestion",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );

                if (option == JOptionPane.YES_OPTION) {
                    // Use the suggested URL
                    url = validationResult.suggestion;
                    // Restart check with suggested URL
                    checkURL(url);
                }
            }

            return;
        }

        try {
            // Clear previous results
            if (resultPanel != null) {
                resultPanel.clearResults();
            }
            if (detailPanel != null) {
                detailPanel.clearDetails();
            }

            // Use normalized URL from validator
            String normalizedUrl = validationResult.normalizedUrl;

            // Perform safety check
            URLSafetyChecker.SafetyResult result = mainController.getURLSafetyChecker().checkSafety(normalizedUrl);

            // Update result panel
            if (resultPanel != null) {
                boolean isSafe = result.verdict.equals("SAFE") || result.verdict.equals("PROBABLY SAFE");
                resultPanel.updateResults(
                        result.normalizedUrl,
                        result.verdict,
                        result.confidence,
                        result.reason,
                        isSafe
                );
            }

            // Update detail panel with different analysis components
            if (detailPanel != null) {
                // SSL verification details
                if (result.sslResult != null) {
                    Map<String, Object> sslInfo = extractSSLInfo(result.sslResult);
                    detailPanel.updateSSLDetails(sslInfo);
                }

                // URL structure analysis details
                if (result.structureResult != null) {
                    Map<String, Object> structureInfo = extractStructureInfo(result.structureResult);
                    detailPanel.updateStructureDetails(structureInfo);
                }

                // ML classification details
                Map<String, Object> mlInfo = new HashMap<>();
                mlInfo.put("verdict", result.verdict);
                mlInfo.put("confidence", result.confidence);
                mlInfo.put("reason", result.reason);

                if (result.mlProbabilities != null && result.mlProbabilities.length >= 2) {
                    mlInfo.put("probabilities", result.mlProbabilities);
                }

                detailPanel.updateMLDetails(mlInfo);

                // Feature analysis
                try {
                    // Extract features from the URL
                    double[] features = backend.URLFeatureExtractor.extractFeatures(result.normalizedUrl);
                    detailPanel.updateFeaturesDetails(features, FEATURE_NAMES);
                } catch (Exception e) {
                    System.err.println("Error extracting features: " + e.getMessage());
                }
            }

        } catch (Exception e) {
            mainController.showError("Error", "An error occurred while analyzing the URL: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Extract SSL verification information into a map
     */
    private Map<String, Object> extractSSLInfo(SSLVerificationService.SSLVerificationResult sslResult) {
        Map<String, Object> sslInfo = new HashMap<>();

        sslInfo.put("isHttps", sslResult.isHttps);
        sslInfo.put("hasCertificate", sslResult.hasCertificate);
        sslInfo.put("isValid", sslResult.isValid);
        sslInfo.put("matchesDomain", sslResult.matchesDomain);
        sslInfo.put("isSelfSigned", sslResult.isSelfSigned);
        sslInfo.put("isNearingExpiration", sslResult.isNearingExpiration);
        sslInfo.put("daysToExpiration", sslResult.daysToExpiration);

        sslInfo.put("commonName", sslResult.commonName != null ? sslResult.commonName : "N/A");
        sslInfo.put("issuer", sslResult.issuer != null ? sslResult.issuer : "N/A");

        // Format dates if available
        if (sslResult.validFrom != null) {
            sslInfo.put("validFrom", sslResult.validFrom.toString());
        }
        if (sslResult.validTo != null) {
            sslInfo.put("validTo", sslResult.validTo.toString());
        }

        // Include error message if any
        if (sslResult.errorMessage != null) {
            sslInfo.put("errorMessage", sslResult.errorMessage);
        }

        return sslInfo;
    }

    /**
     * Extract URL structure analysis information into a map
     */
    private Map<String, Object> extractStructureInfo(URLStructureAnalyzer.URLStructureResult structureResult) {
        Map<String, Object> structureInfo = new HashMap<>();

        structureInfo.put("domainName", structureResult.domainName);
        structureInfo.put("domainTLD", structureResult.domainTLD);
        structureInfo.put("isIPAddress", structureResult.isIPAddress);
        structureInfo.put("subdomainCount", structureResult.subdomainCount);
        structureInfo.put("hasExcessiveSubdomains", structureResult.hasExcessiveSubdomains);
        structureInfo.put("pathDepth", structureResult.pathDepth);
        structureInfo.put("hasDeepPathStructure", structureResult.hasDeepPathStructure);
        structureInfo.put("suspicionScore", structureResult.suspicionScore);
        structureInfo.put("riskLevel", structureResult.riskLevel);

        // Convert set of suspicious elements to array
        Set<String> elements = structureResult.suspiciousElements;
        structureInfo.put("suspiciousElements", elements.toArray());

        return structureInfo;
    }
}
