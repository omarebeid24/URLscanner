package model;

import launcher.AppMain;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * URL - SINGLETON
 */
public class URLProcessor
{
    public static URLProcessor instance;

    private URLProcessor() {}

    private boolean isModelReady = false;
    private URLSafetyChecker safetyChecker;

    public void loadModel() {
        Thread loadingThread = new Thread(() ->
        {
            try {
                System.out.println("Loading the data model...");
                safetyChecker = new URLSafetyChecker("MLModels/newmodel4.model");
                System.out.println("Data model loaded!");
                isModelReady = true;
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        loadingThread.start();
    }

    public void checkUrl(String inputURL) {
        if (inputURL != null && !inputURL.trim().isEmpty()) {
            URLValidator.ValidationResult validator = URLValidator.validate(inputURL);

            if (validator.isValid) {
                String normalizedInputURL = validator.normalizedUrl;

                try {
                    URLSafetyChecker.SafetyResult result = safetyChecker.checkSafety(normalizedInputURL);

                    displaySummaryReport(result);
                    displayGeneralReport(result, inputURL);
                    displayTechnicalReport(result);

                } catch (Exception e) {
                    AppMain.controller.sendError("Error analyzing URL " + e.getMessage());
                }
            } else {
                AppMain.controller.sendError(validator.errorMessage);
            }
        } else {
            AppMain.controller.sendError("No URL entered. Returning to main menu.");
        }
    }

    private void displaySummaryReport(URLSafetyChecker.SafetyResult result) {
        String recommendation = "proceed with caution!";

        switch (result.verdict.toUpperCase()) {
            case "SAFE" -> recommendation = "This URL appears safe to visit.";

            case "PROBABLY SAFE" -> recommendation = "This URL is likely safe, but maintain normal online caution.";

            case "UNCERTAIN" -> recommendation = "Exercise caution with this URL. Proceed only if necessary.";

            case "SUSPICIOUS" -> {
                recommendation = "This URL has suspicious characteristics. Not recommended.";
                if (result.sslResult != null && result.sslResult.errorMessage != null) {
                    recommendation = "SSL certificate verification failed, creating a security risk.";
                }
            }

            case "DANGEROUS" -> {
                recommendation = "DO NOT VISIT this URL. High confidence it is unsafe.";

                if (result.sslResult != null && result.sslResult.errorMessage != null) {
                    recommendation = "Critical SSL security issue detected";
                }
            }
        }

        AppMain.controller.updateSummarySection(result.reason, recommendation);
    }
    private void displayGeneralReport(URLSafetyChecker.SafetyResult result, String inputURL) {

        AppMain.controller.updateVerdict(result.verdict.equalsIgnoreCase("SAFE"));

        if (URLSafetyChecker.TRUSTED_DOMAINS.contains(inputURL)) {
            AppMain.controller.updateConfidenceLevel(1);

            AppMain.controller.updateRiskLevel(1);

            AppMain.controller.updateMLLevel(1);
        } else {
            AppMain.controller.updateConfidenceLevel((result.confidence));

            AppMain.controller.updateRiskLevel((double) (result.structureResult.suspicionScore) / 100);

            AppMain.controller.updateMLLevel(result.mlProbabilities[0]);
        }
    }
    private void displayTechnicalReport(URLSafetyChecker.SafetyResult result) {
        long startTime = System.currentTimeMillis();
        long endTime = System.currentTimeMillis();
        double analysisTime = (endTime - startTime) / 1000.0;

        AppMain.controller.updateTimeStamp("Analysis Time: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + String.format(" (%.2f seconds)", analysisTime));

        if (result.sslResult != null) {
            displaySSLResults(result.sslResult);
        }

        if (result.structureResult != null) {
            displayStructureAnalysis(result.structureResult);
        }

        if (result.normalizedUrl != null) {
            displayMLFeatures(URLFeatureExtractor.extractFeatures(result.normalizedUrl));
        }
    }

    private void displaySSLResults(SSLVerificationService.SSLVerificationResult result) {
        StringBuilder reportBuilder = new StringBuilder();

        if (!result.isHttps) {
            reportBuilder.append("⚠ URL does not use HTTPS (secure connection)\n");
            reportBuilder.append("This means data is transmitted in plain text and could be intercepted.");
            AppMain.controller.updateSSLReport(reportBuilder.toString());
            return;
        }

        if (result.errorMessage != null) {
            reportBuilder.append("✗ SSL Error: ").append(result.errorMessage).append("\n");

            if (result.errorMessage.contains("Unsupported or unrecognized SSL message")) {
                reportBuilder.append("This error indicates the server cannot establish a proper secure connection.\n");
                reportBuilder.append("Security Risk: HIGH - The site is attempting to use HTTPS but is misconfigured.");
            } else if (result.errorMessage.contains("SSL Handshake failed")) {
                reportBuilder.append("This error occurs when the SSL/TLS handshake process fails to complete.\n");
                reportBuilder.append("Security Risk: HIGH - This could indicate outdated or insecure protocols.");
            } else {
                reportBuilder.append("This error prevents proper verification of the site's security.\n");
                reportBuilder.append("Security Risk: HIGH - The site's encryption cannot be validated.");
            }

            AppMain.controller.updateSSLReport(reportBuilder.toString());
            return;
        }

        if (!result.hasCertificate) {
            reportBuilder.append("✗ No SSL certificate found despite HTTPS connection.");
            AppMain.controller.updateSSLReport(reportBuilder.toString());
            return;
        }

        // Certificate validity
        if (result.isValid) {
            reportBuilder.append("✓ Certificate is valid\n");
        } else {
            reportBuilder.append("✗ Certificate is invalid\n");
        }

        // Domain matching
        if (result.matchesDomain) {
            reportBuilder.append("✓ Certificate matches domain\n\n");
        } else {
            reportBuilder.append("✗ Certificate does not match domain\n\n");
        }

        // Self-signed
        if (result.isSelfSigned) {
            reportBuilder.append("⚠ Certificate is self-signed (not trusted)\n");
        }

        // Basic certificate info
        reportBuilder.append("Common Name: ").append(result.commonName).append("\n");

        // Certificate expiration
        reportBuilder.append("Expires: ").append(result.validTo).append("\n");
        reportBuilder.append("Days until expiration: ").append(result.daysToExpiration).append("\n");

        // Certificate issuer (shortened)
        String issuer = result.issuer;

        if (issuer.length() > 140) {
            issuer = issuer.substring(0, 137) + "...";
        }

        reportBuilder.append("Issuer: ").append(issuer);
        AppMain.controller.updateSSLReport(reportBuilder.toString());
    }
    private void displayStructureAnalysis(URLStructureAnalyzer.URLStructureResult result) {
        StringBuilder reportBuilder = new StringBuilder();

        reportBuilder.append("Risk Level: ").append(result.riskLevel).append(String.format(" (Score: %d/100)", result.suspicionScore)).append("\n");

        reportBuilder.append("Domain: ").append(result.domainName).append(".").append(result.domainTLD).append("\n");

        if (result.subdomainCount > 0) {
            reportBuilder.append("Subdomains: ").append(result.subdomainCount).append("\n");
        }

        reportBuilder.append("Path Depth: ").append(result.pathDepth).append("\n\n");


        if (!result.suspiciousElements.isEmpty()) {
            reportBuilder.append("Suspicious Elements:\n");

            int count = 0;
            for (String element : result.suspiciousElements) {
                if (count < 5) {
                    reportBuilder.append("  - ").append(element).append("\n");
                    count++;
                } else {
                    reportBuilder.append("  - ... plus ").append(result.suspiciousElements.size() - 5).append(" more");
                    break;
                }
            }
        }

        AppMain.controller.updateURLAnalysis(reportBuilder.toString());
    }
    private void displayMLFeatures(double[] features) {
        StringBuilder reportBuilder = new StringBuilder();

        String[] featureNames =
                {
                        "URL length", "Has IP address", "Has HTTPS", "Number of dots",
                        "Has @ symbol", "URL depth", "Is shortened URL", "Has suspicious words",
                        "Suspicious word count", "Has encoded chars", "Number of subdomains",
                        "Domain entropy", "Has port number"
                };

        for (int i = 0; i < features.length; i++) {
            reportBuilder.append(String.format("%-25s = %.2f\n", featureNames[i], features[i]));
        }

        AppMain.controller.updateMLFeature(reportBuilder.toString());
    }

    public static URLProcessor getInstance() {
        if(instance == null)
        {
            instance = new URLProcessor();
        }

        return instance;
    }
    public boolean isModelReady() {return isModelReady;}
}