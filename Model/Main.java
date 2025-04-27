package backend;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.Scanner;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Main {

    // ANSI color codes for console output
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_PURPLE = "\u001B[35m";
    private static final String ANSI_CYAN = "\u001B[36m";

    public static Stage stage;

    public static boolean isModelReady = false;
    public static URLSafetyChecker safetyChecker;

    public static void loadModel()
    {
        Thread loadingThread = new Thread(() ->
        {
            try
            {
                System.out.println("Loading the model");
                safetyChecker = new URLSafetyChecker("models/newmodel4.model");
                isModelReady = true;
                Thread.currentThread().interrupt();
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        });

        loadingThread.start();
    }


    // THIS IS A DEMO TO CONNECT TO THE UI
    // CAN DELETED LATER ON
    public static void runDemo(String text)
    {
        checkUrl(text, safetyChecker);
    }

    public static void main(String[] args)
    {
        try
        {
            // Initialize our enhanced URL safety checker ONCE at startup
            safetyChecker = new URLSafetyChecker("models/newmodel4.model");
            isModelReady = true;

            // Create scanner for user input
            Scanner scanner = new Scanner(System.in);

            // Main program loop
            boolean keepRunning = true;

            while (keepRunning) {
                // Print main menu banner
                printMainBanner();

                // Get user choice
                System.out.print("Enter choice [1-4]: ");
                String choice = scanner.nextLine().trim();

                switch (choice) {
                    case "1":
                        // Check a URL
                        // @TODO REMOVE THIS LINE
                        // checkUrl(scanner., safetyChecker);
                        break;
                    case "2":
                        // Batch check URLs
                        batchCheckUrls(scanner, safetyChecker);
                        break;
                    case "3":
                        // Show help/about info
                        showHelp();
                        break;
                    case "4":
                        // Exit
                        keepRunning = false;
                        System.out.println(ANSI_BLUE + "Thank you for using URL Safety Analyzer!" + ANSI_RESET);
                        break;
                    default:
                        System.out.println(ANSI_RED + "Invalid choice. Please enter 1, 2, 3, or 4." + ANSI_RESET);
                        break;
                }

                // If not exiting, prompt to continue
                if (keepRunning) {
                    System.out.println("\nPress ENTER to continue...");
                    scanner.nextLine();
                }
            }

            // Close scanner before exit
            scanner.close();

        } catch (Exception e) {
            System.err.println(ANSI_RED + "❌ Error during initialization: " + e.getMessage() + ANSI_RESET);
            e.printStackTrace();
        }
    }

    /**
     * Prints the main application banner and menu
     */
    private static void printMainBanner() {
        // Clear screen (works on most terminals)
        System.out.print("\033[H\033[2J");
        System.out.flush();

        System.out.println(ANSI_CYAN +
                "╔═══════════════════════════════════════════════════════╗\n" +
                "║       URL SAFETY ANALYZER PROFESSIONAL v4.0            ║\n" +
                "║       ML · SSL · Structure Analysis · Whitelist        ║\n" +
                "╚═══════════════════════════════════════════════════════╝" + ANSI_RESET);

        System.out.println("\nMENU:");
        System.out.println("  1. Check URL safety");
        System.out.println("  2. Batch check multiple URLs");
        System.out.println("  3. Help / About");
        System.out.println("  4. Exit");
    }

    /**
     * Handles the URL checking flow with professional output
     */
    private static void checkUrl(String inputURL, URLSafetyChecker safetyChecker) {
        if (inputURL != null && !inputURL.trim().isEmpty()) {
            URLValidator.ValidationResult validator = URLValidator.validate(inputURL);

            if (validator.isValid) {
                inputURL = validator.normalizedUrl;

                try {
                    long startTime = System.currentTimeMillis();

                    URLSafetyChecker.SafetyResult result = safetyChecker.checkSafety(inputURL);

                    long endTime = System.currentTimeMillis();
                    double analysisTime = (endTime - startTime) / 1000.0;

                    displayAnalysisReport(result);
                    displayRiskBar(result.confidence, result);

                } catch (Exception e) {
                    demo.getMainScreenController().sendError("Error analyzing URL " + e.getMessage());
                }
            } else {
                demo.getMainScreenController().sendError(validator.errorMessage);
            }
        } else {
            demo.getMainScreenController().sendError("No URL entered. Returning to main menu.");
        }
    }

    /**
     * Displays a professional, simplified report with a clear verdict
     */
    private static void displayAnalysisReport(URLSafetyChecker.SafetyResult result)
    {
        String recommendation = "proceed with caution!";

        switch (result.verdict.toUpperCase())
        {
            case "SAFE" -> recommendation = "This URL appears safe to visit.";

            case "PROBABLY SAFE" -> recommendation = "This URL is likely safe, but maintain normal online caution.";

            case "UNCERTAIN" -> recommendation ="Exercise caution with this URL. Proceed only if necessary.";

            case "SUSPICIOUS" ->
            {
                recommendation ="This URL has suspicious characteristics. Not recommended.";
                if (result.sslResult != null && result.sslResult.errorMessage != null)
                {
                    recommendation ="SSL certificate verification failed, creating a security risk.";
                }
            }

            case "DANGEROUS" ->
            {
                recommendation = "DO NOT VISIT this URL. High confidence it is unsafe.";

                if (result.sslResult != null && result.sslResult.errorMessage != null)
                {
                    recommendation = "Critical SSL security issue detected";
                }
            }
        }

        demo.getMainScreenController().updateSummarySection(result.reason ,recommendation);
        demo.getMainScreenController().updateConfidenceProgressLevel((result.confidence));
    }

    /**
     * Displays a professional, simplified report with a clear verdict
     */
    private static void displayProfessionalReport(URLSafetyChecker.SafetyResult result, double analysisTime) {
        // Color coding based on verdict
        String headerColor;
        String verdictColor;
        String icon;

        switch (result.verdict.toUpperCase())
        {
            case "SAFE":
                headerColor = ANSI_GREEN;
                verdictColor = ANSI_GREEN;
                icon = "✓";
                break;
            case "PROBABLY SAFE":
                headerColor = ANSI_GREEN;
                verdictColor = ANSI_GREEN;
                icon = "✓";
                break;
            case "SUSPICIOUS":
                headerColor = ANSI_YELLOW;
                verdictColor = ANSI_YELLOW;
                icon = "⚠";
                break;
            case "DANGEROUS":
                headerColor = ANSI_RED;
                verdictColor = ANSI_RED;
                icon = "✗";
                break;
            case "UNCERTAIN":
                headerColor = ANSI_YELLOW;
                verdictColor = ANSI_YELLOW;
                icon = "?";
                break;
            default:
                headerColor = ANSI_YELLOW;
                verdictColor = ANSI_YELLOW;
                icon = "⚠";
        }


        // URL and timestamp info
        System.out.println(ANSI_BLUE + "URL: " + ANSI_RESET + result.normalizedUrl);
        System.out.println(ANSI_BLUE + "Analysis Time: " + ANSI_RESET +
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) +
                String.format(" (%.2f seconds)", analysisTime));


        // Clean, professionally formatted security assessment
        System.out.println("\n" + ANSI_BLUE + "SECURITY ASSESSMENT:" + ANSI_RESET);


        // Display ML probabilities if available
        if (result.mlProbabilities != null && result.mlProbabilities.length >= 2) {
            System.out.println("\n" + ANSI_BLUE + "ML CLASSIFICATION:" + ANSI_RESET);
            System.out.printf(ANSI_GREEN + "%.1f%% Safe" + ANSI_RESET + " | " +
                            ANSI_RED + "%.1f%% Malicious" + ANSI_RESET + "\n",
                    result.mlProbabilities[0] * 100,
                    result.mlProbabilities[1] * 100);
        }



        // Option for detailed analysis
        System.out.println("\nWould you like to see detailed technical analysis? (y/n): ");
        Scanner scanner = new Scanner(System.in);
        String showDetails = scanner.nextLine().trim().toLowerCase();

        if (showDetails.equals("y") || showDetails.equals("yes")) {
            displayDetailedAnalysis(result);
        }
    }

    /**
     * Displays a visual risk bar
     */
    private static void displayRiskBar(double confidence,  URLSafetyChecker.SafetyResult result) {
        if (result.verdict.equalsIgnoreCase("SAFE"))
        {
            demo.getMainScreenController().updateRiskProgressLevel(confidence , true);
        }
        else
        {
            demo.getMainScreenController().updateRiskProgressLevel(confidence, false);
        }
    }

    /**
     * Handles batch checking of multiple URLs with simplified output
     */
    private static void batchCheckUrls(Scanner scanner, URLSafetyChecker safetyChecker) {
        System.out.println("\n" + ANSI_CYAN + "BATCH URL CHECKER" + ANSI_RESET);
        System.out.println("Enter URLs to check (one per line). Enter a blank line when done.");

        int count = 0;
        int validUrlCount = 0;
        System.out.print("\nURL #1: ");
        String url = scanner.nextLine();

        // Clear screen and prepare for results
        clearScreen();
        System.out.println(ANSI_CYAN + "╔═══════════════════════════════════════════════════════╗");
        System.out.println("║              BATCH URL ANALYSIS RESULTS                  ║");
        System.out.println("╚═══════════════════════════════════════════════════════╝" + ANSI_RESET);

        System.out.println("\n" + ANSI_BLUE + "ANALYSIS TIMESTAMP: " + ANSI_RESET +
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));

        while (url != null && !url.trim().isEmpty()) {
            count++;

            System.out.println(ANSI_BLUE + "\nURL #" + count + ": " + url + ANSI_RESET);

            // Validate the URL first
            URLValidator.ValidationResult validationResult = URLValidator.validate(url);

            if (!validationResult.isValid) {
                // Show validation error but continue batch processing
                System.out.println(ANSI_RED + "✗ INVALID URL: " + validationResult.errorMessage + ANSI_RESET);

                if (validationResult.suggestion != null) {
                    System.out.println(ANSI_YELLOW + "  Did you mean: " + validationResult.suggestion + "?" + ANSI_RESET);
                }

                System.out.println("───────────────────────────────────────────────────────");
            } else {
                try {
                    // Use the normalized URL
                    String normalizedUrl = validationResult.normalizedUrl;

                    // Perform comprehensive URL safety check
                    URLSafetyChecker.SafetyResult result = safetyChecker.checkSafety(normalizedUrl);
                    validUrlCount++;

                    // Determine color based on verdict
                    String verdictColor;
                    String emoji;

                    switch (result.verdict.toUpperCase()) {
                        case "SAFE":
                        case "PROBABLY SAFE":
                            verdictColor = ANSI_GREEN;
                            emoji = "✓";
                            break;
                        case "SUSPICIOUS":
                        case "UNCERTAIN":
                            verdictColor = ANSI_YELLOW;
                            emoji = "⚠";
                            break;
                        case "DANGEROUS":
                            verdictColor = ANSI_RED;
                            emoji = "✗";
                            break;
                        default:
                            verdictColor = ANSI_YELLOW;
                            emoji = "⚠";
                    }

                    // Display simplified results for batch mode
                    System.out.println(verdictColor + emoji + " VERDICT: " + result.verdict.toUpperCase() +
                            String.format(" (Confidence: %.1f%%)", result.confidence * 100) + ANSI_RESET);

                    // Show SSL errors prominently
                    if (result.sslResult != null && result.sslResult.errorMessage != null) {
                        System.out.println(ANSI_RED + "  SSL ERROR: " + result.sslResult.errorMessage + ANSI_RESET);
                    }

                    // Show ML assessment if available
                    if (result.mlProbabilities != null && result.mlProbabilities.length >= 2) {
                        System.out.printf("  ML Assessment: " +
                                        ANSI_GREEN + "%.1f%% Safe / " +
                                        ANSI_RED + "%.1f%% Malicious\n" + ANSI_RESET,
                                result.mlProbabilities[0] * 100,
                                result.mlProbabilities[1] * 100);
                    }

                    System.out.println("  " + truncateReason(result.reason, 100));
                    System.out.println("───────────────────────────────────────────────────────");

                } catch (Exception e) {
                    System.err.println(ANSI_RED + "❌ Error analyzing URL #" + count + ": " + e.getMessage() + ANSI_RESET);
                    System.out.println("───────────────────────────────────────────────────────");
                }
            }

            System.out.print("\nURL #" + (count + 1) + " (or press Enter to finish): ");
            url = scanner.nextLine();
        }

        System.out.println("\n" + ANSI_GREEN + "Batch analysis complete. " + validUrlCount + " of " + count + " URLs analyzed successfully." + ANSI_RESET);
    }

    /**
     * Displays detailed technical analysis
     */
    private static void displayDetailedAnalysis(URLSafetyChecker.SafetyResult result) {
        System.out.println("\n" + ANSI_PURPLE + "╔═══════════════════════════════════════════════════════╗");
        System.out.println("║                TECHNICAL ANALYSIS DETAILS                ║");
        System.out.println("╚═══════════════════════════════════════════════════════╝" + ANSI_RESET);

        // Display SSL verification results
        if (result.sslResult != null) {
            displaySSLResults(result.sslResult);
        }

        // Display URL structure analysis
        if (result.structureResult != null) {
            displayStructureAnalysis(result.structureResult);
        }

        // Display ML feature vector
        try {
            if (result.normalizedUrl != null) {
                double[] features = URLFeatureExtractor.extractFeatures(result.normalizedUrl);
                displayFeatures(features);
            }
        } catch (Exception e) {
            System.out.println(ANSI_RED + "Error displaying features: " + e.getMessage() + ANSI_RESET);
        }
    }

    /**
     * Displays SSL certificate verification results
     */
    private static void displaySSLResults(SSLVerificationService.SSLVerificationResult result) {
        System.out.println("\n" + ANSI_PURPLE + "=== SSL Certificate Verification ===" + ANSI_RESET);

        if (!result.isHttps) {
            System.out.println(ANSI_YELLOW + "⚠ URL does not use HTTPS (secure connection)" + ANSI_RESET);
            System.out.println(ANSI_YELLOW + "  This means data is transmitted in plain text and could be intercepted." + ANSI_RESET);
            return;
        }

        if (result.errorMessage != null) {
            System.out.println(ANSI_RED + "✗ SSL Error: " + result.errorMessage + ANSI_RESET);

            // Add more detailed explanation based on the type of error
            if (result.errorMessage.contains("Unsupported or unrecognized SSL message")) {
                System.out.println(ANSI_RED + "  This error indicates the server cannot establish a proper secure connection." + ANSI_RESET);
                System.out.println(ANSI_RED + "  Security Risk: HIGH - The site is attempting to use HTTPS but is misconfigured." + ANSI_RESET);
            } else if (result.errorMessage.contains("SSL Handshake failed")) {
                System.out.println(ANSI_RED + "  This error occurs when the SSL/TLS handshake process fails to complete." + ANSI_RESET);
                System.out.println(ANSI_RED + "  Security Risk: HIGH - This could indicate outdated or insecure protocols." + ANSI_RESET);
            } else {
                System.out.println(ANSI_RED + "  This error prevents proper verification of the site's security." + ANSI_RESET);
                System.out.println(ANSI_RED + "  Security Risk: HIGH - The site's encryption cannot be validated." + ANSI_RESET);
            }
            return;
        }

        if (!result.hasCertificate) {
            System.out.println(ANSI_RED + "✗ No SSL certificate found despite HTTPS connection" + ANSI_RESET);
            return;
        }

        // Certificate validity
        if (result.isValid) {
            System.out.println(ANSI_GREEN + "✓ Certificate is valid" + ANSI_RESET);
        } else {
            System.out.println(ANSI_RED + "✗ Certificate is invalid" + ANSI_RESET);
        }

        // Domain matching
        if (result.matchesDomain) {
            System.out.println(ANSI_GREEN + "✓ Certificate matches domain" + ANSI_RESET);
        } else {
            System.out.println(ANSI_RED + "✗ Certificate does not match domain" + ANSI_RESET);
        }

        // Self-signed
        if (result.isSelfSigned) {
            System.out.println(ANSI_YELLOW + "⚠ Certificate is self-signed (not trusted)" + ANSI_RESET);
        }

        // Basic certificate info
        System.out.println(ANSI_BLUE + "Common Name: " + ANSI_RESET + result.commonName);

        // Certificate expiration
        String expiryColor = result.isNearingExpiration ? ANSI_YELLOW : ANSI_GREEN;
        System.out.println(ANSI_BLUE + "Expires: " + expiryColor + result.validTo + ANSI_RESET);
        System.out.println(ANSI_BLUE + "Days until expiration: " + expiryColor +
                result.daysToExpiration + ANSI_RESET);

        // Certificate issuer (shortened)
        String issuer = result.issuer;
        if (issuer.length() > 60) {
            issuer = issuer.substring(0, 57) + "...";
        }
        System.out.println(ANSI_BLUE + "Issuer: " + ANSI_RESET + issuer);
    }

    /**
     * Displays URL structure analysis results
     */
    private static void displayStructureAnalysis(URLStructureAnalyzer.URLStructureResult result) {
        System.out.println("\n" + ANSI_PURPLE + "=== URL Structure Analysis ===" + ANSI_RESET);

        // Display risk level with appropriate color
        String riskColor;
        switch (result.riskLevel) {
            case "High":
                riskColor = ANSI_RED;
                break;
            case "Medium":
            case "Low":
                riskColor = ANSI_YELLOW;
                break;
            default:
                riskColor = ANSI_GREEN;
        }

        System.out.println(ANSI_BLUE + "Risk Level: " + riskColor + result.riskLevel + ANSI_RESET +
                String.format(" (Score: %d/100)", result.suspicionScore));

        // Domain information
        System.out.println(ANSI_BLUE + "Domain: " + ANSI_RESET +
                (result.isIPAddress ? ANSI_YELLOW + "[IP Address] " : "") +
                result.domainName + "." + result.domainTLD);

        if (result.subdomainCount > 0) {
            System.out.println(ANSI_BLUE + "Subdomains: " + ANSI_RESET +
                    (result.hasExcessiveSubdomains ? ANSI_YELLOW : ANSI_RESET) +
                    result.subdomainCount);
        }

        // Path information
        System.out.println(ANSI_BLUE + "Path Depth: " + ANSI_RESET +
                (result.hasDeepPathStructure ? ANSI_YELLOW : ANSI_RESET) +
                result.pathDepth);

        // Show suspicious elements if any
        if (!result.suspiciousElements.isEmpty()) {
            System.out.println(ANSI_BLUE + "Suspicious Elements:" + ANSI_RESET);
            int count = 0;
            for (String element : result.suspiciousElements) {
                if (count < 5) { // Limit to 5 elements for readability
                    System.out.println(ANSI_YELLOW + "  - " + element + ANSI_RESET);
                    count++;
                } else {
                    System.out.println(ANSI_YELLOW + "  - ... plus " +
                            (result.suspiciousElements.size() - 5) + " more" + ANSI_RESET);
                    break;
                }
            }
        }
    }

    /**
     * Displays the extracted features in a readable format
     */
    private static void displayFeatures(double[] features) {
        System.out.println("\n" + ANSI_PURPLE + "=== ML Feature Vector ===" + ANSI_RESET);
        String[] featureNames = {
                "URL length", "Has IP address", "Has HTTPS", "Number of dots",
                "Has @ symbol", "URL depth", "Is shortened URL", "Has suspicious words",
                "Suspicious word count", "Has encoded chars", "Number of subdomains",
                "Domain entropy", "Has port number"
        };

        for (int i = 0; i < features.length; i++) {
            // Highlight suspicious features in yellow
            String color = isFeatureSuspicious(i, features[i]) ? ANSI_YELLOW : ANSI_RESET;
            System.out.printf("%s%-25s = %.2f%s\n", color, featureNames[i], features[i], ANSI_RESET);
        }
    }

    /**
     * Determines if a feature value is suspicious
     */
    private static boolean isFeatureSuspicious(int featureIndex, double value) {
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
     * Truncates a long reason string for batch display
     */
    private static String truncateReason(String reason, int maxLength) {
        if (reason.length() <= maxLength) {
            return reason;
        }
        return reason.substring(0, maxLength - 3) + "...";
    }

    /**
     * Clears the console screen
     */
    private static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    /**
     * Shows help and information about the application
     */
    private static void showHelp() {
        clearScreen();
        System.out.println(ANSI_CYAN +
                "╔═══════════════════════════════════════════════════════╗\n" +
                "║        URL SAFETY ANALYZER PROFESSIONAL v4.0           ║\n" +
                "║                     USER GUIDE                         ║\n" +
                "╚═══════════════════════════════════════════════════════╝" + ANSI_RESET);

        System.out.println("\n" + ANSI_PURPLE + "ABOUT THIS TOOL" + ANSI_RESET);
        System.out.println("URL Safety Analyzer combines four detection methods to accurately assess URL safety:");

        System.out.println("\n1. " + ANSI_CYAN + "Machine Learning" + ANSI_RESET +
                " - Neural model trained on 500,000+ URLs to detect malicious patterns");
        System.out.println("2. " + ANSI_CYAN + "SSL Verification" + ANSI_RESET +
                " - Checks certificate validity, authenticity, and encryption protocols");
        System.out.println("3. " + ANSI_CYAN + "URL Structure Analysis" + ANSI_RESET +
                " - Examines 13 structural features associated with phishing sites");
        System.out.println("4. " + ANSI_CYAN + "Domain Whitelist" + ANSI_RESET +
                " - Pre-verified database of 50+ trusted domains");

        System.out.println("\n" + ANSI_PURPLE + "SECURITY VERDICTS" + ANSI_RESET);
        System.out.println(ANSI_GREEN + "✓ SAFE" + ANSI_RESET + " - URL is on our whitelist or passes all security checks");
        System.out.println(ANSI_GREEN + "✓ PROBABLY SAFE" + ANSI_RESET + " - URL has good security indicators");
        System.out.println(ANSI_YELLOW + "? UNCERTAIN" + ANSI_RESET + " - Mixed results from different analysis methods");
        System.out.println(ANSI_YELLOW + "⚠ SUSPICIOUS" + ANSI_RESET + " - URL has concerning characteristics");
        System.out.println(ANSI_RED + "✗ DANGEROUS" + ANSI_RESET + " - URL is likely malicious and should be avoided");

        System.out.println("\n" + ANSI_PURPLE + "SSL CERTIFICATE VERIFICATION" + ANSI_RESET);
        System.out.println("SSL errors are critical security indicators. When our tool reports an SSL error,");
        System.out.println("it means the website has failed to properly implement secure communications.");
        System.out.println("This could indicate:");
        System.out.println("- Expired or invalid certificates");
        System.out.println("- Self-signed certificates not verified by trusted authorities");
        System.out.println("- Misconfigured servers attempting to use HTTPS");
        System.out.println("- Potential man-in-the-middle attack vulnerability");

        System.out.println("\n" + ANSI_PURPLE + "URL VALIDATION" + ANSI_RESET);
        System.out.println("The system validates URLs before analysis to ensure they:");
        System.out.println("- Have proper format and structure");
        System.out.println("- Contain valid domain extensions (TLDs)");
        System.out.println("- Exist in the DNS system (have a real domain)");
        System.out.println("For invalid URLs, the system may suggest corrections or alternatives.");

        System.out.println("\n" + ANSI_PURPLE + "ONLINE SAFETY BEST PRACTICES" + ANSI_RESET);
        System.out.println("- Verify the domain name carefully before entering credentials");
        System.out.println("- Look for HTTPS with a valid certificate (padlock icon in browser)");
        System.out.println("- Be wary of URLs containing random characters or numbers");
        System.out.println("- Avoid clicking links in unsolicited emails");
        System.out.println("- When in doubt, navigate to websites directly rather than using links");

        System.out.println("\n" + ANSI_BLUE + "This tool is designed for informational purposes only. " +
                "Always exercise caution online." + ANSI_RESET);
    }
}



//        //@TODO ERROR HANDLING WTF!?
//        if (!validationResult.isValid)
//        {
//            // Display error with appropriate formatting
//            clearScreen();
//            System.out.println(ANSI_YELLOW +
//                    "╔═══════════════════════════════════════════════════════╗\n" +
//                    "║               INVALID URL DETECTED                     ║\n" +
//                    "╚═══════════════════════════════════════════════════════╝" + ANSI_RESET);
//
//            System.out.println("\n" + ANSI_RED + "✗ ERROR: " + validationResult.errorMessage + ANSI_RESET);
//
//        }
//        else
//        {
//            //@TODO Progress continuing
//            System.out.println(ANSI_BLUE + "\nAnalyzing URL... Please wait." + ANSI_RESET);
//            testURL = validationResult.normalizedUrl;
//        }


// For unsafe verdicts, confidence maps to danger level
//            filledWidth = (int)(confidence * barWidth);
//            System.out.print(ANSI_BLUE + "RISK LEVEL: " + ANSI_RESET + "[");
//            for (int i = 0; i < barWidth; i++)
//            {
//                if (i < filledWidth)
//                {
//                    System.out.print(ANSI_RED + "█" + ANSI_RESET);
//                }
//                else
//                {
//                    System.out.print("░");
//                }
//            }
//
//            System.out.println("] " + ANSI_RED + "HIGH" + ANSI_RESET);

//@TODO Time stances
//        try
//        {
//            long startTime = System.currentTimeMillis();
//
//            // Perform comprehensive URL safety check
//            URLSafetyChecker.SafetyResult result = safetyChecker.checkSafety(testURL);
//
//            long endTime = System.currentTimeMillis();
//            double analysisTime = (endTime - startTime) / 1000.0;
//
//            // Display professional report
//            displayProfessionalReport(result, analysisTime);
//
//        }
//        catch (Exception e)
//        {
//            System.err.println(ANSI_RED + "❌ Error analyzing URL: " + e.getMessage() + ANSI_RESET);
//            e.printStackTrace();
//        }
//    }

// For safe verdicts, confidence directly maps to filled portion
//            filledWidth = (int)(confidence * barWidth);
//            System.out.print(ANSI_BLUE + "RISK LEVEL: " + ANSI_RESET + "[");
//            for (int i = 0; i < barWidth; i++)
//            {
//                if (i < filledWidth)
//                {
//                    System.out.print(ANSI_GREEN + "█" + ANSI_RESET);
//                }
//                else
//                {
//                    System.out.print("░");
//                }
//            }
//
//            System.out.println("] " + ANSI_GREEN + "LOW" + ANSI_RESET);