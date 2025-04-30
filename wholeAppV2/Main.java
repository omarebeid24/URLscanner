package backend;

import javafx.stage.Stage;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Scanner;

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

    public static void main(String[] args) {
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
                //printMainBanner();

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
                       // batchCheckUrls(scanner, safetyChecker);
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

//
    /**
     * Handles the URL checking flow with professional output
     */
    private static void checkUrl(String inputURL, URLSafetyChecker safetyChecker)
    {
        if (inputURL != null && !inputURL.trim().isEmpty()) {
            URLValidator.ValidationResult validator = URLValidator.validate(inputURL);

            if (validator.isValid)
            {
                String normalizedInputURL = validator.normalizedUrl;

                try {
                    URLSafetyChecker.SafetyResult result = safetyChecker.checkSafety(normalizedInputURL);

                    displaySummaryReport(result);
                    displayGeneralReport(result, inputURL);
                    displayTechnicalReport(result);

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

    private static void displaySummaryReport(URLSafetyChecker.SafetyResult result) {
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
    }

    /**
     * Displays a professional, simplified report with a clear verdict
     */
    private static void displayGeneralReport(URLSafetyChecker.SafetyResult result, String inputURL)
    {

        demo.getMainScreenController().updateVerdict(result.verdict.equalsIgnoreCase("SAFE"));

        if(URLSafetyChecker.TRUSTED_DOMAINS.contains(inputURL))
        {
            demo.getMainScreenController().updateConfidenceLevel(1);

            demo.getMainScreenController().updateRiskLevel(1);

            demo.getMainScreenController().updateMLLevel(1);
        }
        else
        {
            demo.getMainScreenController().updateConfidenceLevel((result.confidence));

            demo.getMainScreenController().updateRiskLevel((double) (result.structureResult.suspicionScore) / 100);

            demo.getMainScreenController().updateMLLevel(result.mlProbabilities[0]);
        }
    }


    private static void displayTechnicalReport(URLSafetyChecker.SafetyResult result)
    {
        long startTime = System.currentTimeMillis();
        long endTime = System.currentTimeMillis();
        double analysisTime = (endTime - startTime) / 1000.0;

        demo.getMainScreenController().updateTimeStamp("Analysis Time: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + String.format(" (%.2f seconds)", analysisTime));

        if (result.sslResult != null)
        {
            displaySSLResults(result.sslResult);
        }

        if (result.structureResult != null)
        {
            displayStructureAnalysis(result.structureResult);
        }

        if (result.normalizedUrl != null)
        {
            displayMLFeatures(URLFeatureExtractor.extractFeatures(result.normalizedUrl));
        }
    }


    /**
     * Displays SSL certificate verification results
     */
    private static void displaySSLResults(SSLVerificationService.SSLVerificationResult result)
    {
        StringBuilder reportBuilder = new StringBuilder();

        if (!result.isHttps)
        {
            reportBuilder.append("⚠ URL does not use HTTPS (secure connection)\n");
            reportBuilder.append("This means data is transmitted in plain text and could be intercepted.");
            demo.getMainScreenController().updateSSLReport(reportBuilder.toString());
            return;
        }

        if (result.errorMessage != null)
        {
            reportBuilder.append("✗ SSL Error: ").append(result.errorMessage).append("\n");

            if (result.errorMessage.contains("Unsupported or unrecognized SSL message"))
            {
                reportBuilder.append("This error indicates the server cannot establish a proper secure connection.\n");
                reportBuilder.append("Security Risk: HIGH - The site is attempting to use HTTPS but is misconfigured.");
            }
            else if (result.errorMessage.contains("SSL Handshake failed"))
            {
                reportBuilder.append("This error occurs when the SSL/TLS handshake process fails to complete.\n");
                reportBuilder.append("Security Risk: HIGH - This could indicate outdated or insecure protocols.");
            }
            else
            {
                reportBuilder.append("This error prevents proper verification of the site's security.\n");
                reportBuilder.append("Security Risk: HIGH - The site's encryption cannot be validated.");
            }

            demo.getMainScreenController().updateSSLReport(reportBuilder.toString());
            return;
        }

        if (!result.hasCertificate)
        {
            reportBuilder.append("✗ No SSL certificate found despite HTTPS connection.");
            demo.getMainScreenController().updateSSLReport(reportBuilder.toString());
            return;
        }

        // Certificate validity
        if (result.isValid)
        {
            reportBuilder.append("✓ Certificate is valid\n");
        }
        else
        {
            reportBuilder.append("✗ Certificate is invalid\n");
        }

        // Domain matching
        if (result.matchesDomain)
        {
            reportBuilder.append("✓ Certificate matches domain\n\n");
        }
        else
        {
            reportBuilder.append("✗ Certificate does not match domain\n\n");
        }

        // Self-signed
        if (result.isSelfSigned)
        {
            reportBuilder.append("⚠ Certificate is self-signed (not trusted)\n");
        }

        // Basic certificate info
        reportBuilder.append("Common Name: ").append(result.commonName).append("\n");

        // Certificate expiration
        reportBuilder.append("Expires: ").append(result.validTo).append("\n");
        reportBuilder.append("Days until expiration: ").append(result.daysToExpiration).append("\n");

        // Certificate issuer (shortened)
        String issuer = result.issuer;

        if (issuer.length() > 140)
        {
            issuer = issuer.substring(0, 137) + "...";
        }

        reportBuilder.append("Issuer: ").append(issuer);
        demo.getMainScreenController().updateSSLReport(reportBuilder.toString());
    }

    /**
     * Displays URL structure analysis results
     */
    private static void displayStructureAnalysis(URLStructureAnalyzer.URLStructureResult result)
    {
        StringBuilder reportBuilder = new StringBuilder();

        reportBuilder.append("Risk Level: ").append(result.riskLevel).append( String.format(" (Score: %d/100)", result.suspicionScore)).append("\n");

        reportBuilder.append("Domain: ").append(result.domainName).append(".").append(result.domainTLD).append("\n");

        if (result.subdomainCount > 0)
        {
            reportBuilder.append("Subdomains: ").append(result.subdomainCount).append("\n");
        }

        reportBuilder.append("Path Depth: ").append(result.pathDepth).append("\n\n");


        if (!result.suspiciousElements.isEmpty())
        {
            reportBuilder.append("Suspicious Elements:\n");

            int count = 0;
            for (String element : result.suspiciousElements)
            {
                if (count < 5)
                {
                    reportBuilder.append("  - ").append(element).append("\n");
                    count++;
                }
                else
                {
                    reportBuilder.append("  - ... plus ").append(result.suspiciousElements.size() - 5).append(" more");
                    break;
                }
            }
        }

        demo.getMainScreenController().updateURLAnalysis(reportBuilder.toString());
    }

    /**
     * Displays the extracted features in a readable format
     */
    private static void displayMLFeatures(double[] features)
    {
        StringBuilder reportBuilder = new StringBuilder();

        String[] featureNames =
        {
                "URL length", "Has IP address", "Has HTTPS", "Number of dots",
                "Has @ symbol", "URL depth", "Is shortened URL", "Has suspicious words",
                "Suspicious word count", "Has encoded chars", "Number of subdomains",
                "Domain entropy", "Has port number"
        };

        for (int i = 0; i < features.length; i++)
        {
            reportBuilder.append(String.format("%-25s = %.2f\n", featureNames[i], features[i]));
        }

        demo.getMainScreenController().updateMLFeature(reportBuilder.toString());
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



//    /**
//     * Displays a professional, simplified report with a clear verdict
//     */
//    private static void displayProfessionalReport(URLSafetyChecker.SafetyResult result, double analysisTime) {
//        // URL and timestamp info
//        System.out.println(ANSI_BLUE + "URL: " + ANSI_RESET + result.normalizedUrl);
//        System.out.println(ANSI_BLUE + "Analysis Time: " + ANSI_RESET +
//                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) +
//                String.format(" (%.2f seconds)", analysisTime));
//
//
//        // Display ML probabilities if available
//        if (result.mlProbabilities != null && result.mlProbabilities.length >= 2) {
//            System.out.println("\n" + ANSI_BLUE + "ML CLASSIFICATION:" + ANSI_RESET);
//            System.out.printf(ANSI_GREEN + "%.1f%% Safe" + ANSI_RESET + " | " +
//                            ANSI_RED + "%.1f%% Malicious" + ANSI_RESET + "\n",
//                    result.mlProbabilities[0] * 100,
//                    result.mlProbabilities[1] * 100);
//        }
//
//
//
//        // Option for detailed analysis
//        System.out.println("\nWould you like to see detailed technical analysis? (y/n): ");
//        Scanner scanner = new Scanner(System.in);
//        String showDetails = scanner.nextLine().trim().toLowerCase();
//
//        if (showDetails.equals("y") || showDetails.equals("yes")) {
//            displayDetailedAnalysis(result);
//        }
//    }


/**
 * Handles batch checking of multiple URLs with simplified output

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
 */


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
//    /**
//     * Displays detailed technical analysis
//     */
//    private static void displayDetailedAnalysis(URLSafetyChecker.SafetyResult result)
//    {
//
//        // Display ML feature vector
//        try {
//            if (result.normalizedUrl != null) {
//                double[] features = URLFeatureExtractor.extractFeatures(result.normalizedUrl);
//                displayFeatures(features);
//            }
//        } catch (Exception e) {
//            System.out.println(ANSI_RED + "Error displaying features: " + e.getMessage() + ANSI_RESET);
//        }
//    }
/**
 //     * Prints the main application banner and menu
 //     */
//    private static void printMainBanner() {
//        // Clear screen (works on most terminals)
//        System.out.print("\033[H\033[2J");
//        System.out.flush();
//
//        System.out.println(ANSI_CYAN +
//                "╔═══════════════════════════════════════════════════════╗\n" +
//                "║       URL SAFETY ANALYZER PROFESSIONAL v4.0            ║\n" +
//                "║       ML · SSL · Structure Analysis · Whitelist        ║\n" +
//                "╚═══════════════════════════════════════════════════════╝" + ANSI_RESET);
//
//        System.out.println("\nMENU:");
//        System.out.println("  1. Check URL safety");
//        System.out.println("  2. Batch check multiple URLs");
//        System.out.println("  3. Help / About");
//        System.out.println("  4. Exit");
//    }
