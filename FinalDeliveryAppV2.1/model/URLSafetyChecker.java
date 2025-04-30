package model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An enhanced URL safety checker that combines machine learning with rule-based heuristics,
 * SSL certificate verification, and URL structure analysis for comprehensive phishing detection.
 */
public class URLSafetyChecker {
    private URLClassifier classifier;
    private SSLVerificationService sslVerifier;
    private URLStructureAnalyzer structureAnalyzer;

    // Enhanced trusted domains whitelist with 50+ popular websites
    public static final Set<String> TRUSTED_DOMAINS = new HashSet<>(Arrays.asList(
            // Social Media
            "facebook.com", "twitter.com", "instagram.com", "linkedin.com", "pinterest.com",
            "reddit.com", "tumblr.com", "tiktok.com", "snapchat.com", "whatsapp.com",

            // Technology Companies
            "google.com", "youtube.com", "microsoft.com", "apple.com", "amazon.com",
            "netflix.com", "spotify.com", "zoom.us", "adobe.com", "slack.com",

            // E-commerce
            "ebay.com", "walmart.com", "target.com", "bestbuy.com", "etsy.com",
            "shopify.com", "aliexpress.com", "costco.com",

            // News & Information
            "cnn.com", "bbc.com", "nytimes.com", "washingtonpost.com", "wsj.com",
            "bloomberg.com", "reuters.com", "wikipedia.org", "weather.com",

            // Education & Reference
            "github.com", "stackoverflow.com", "medium.com", "quora.com", "edu",

            // Banking & Finance
            "paypal.com", "chase.com", "bankofamerica.com", "wellsfargo.com", "visa.com",
            "mastercard.com", "discover.com", "americanexpress.com",

            // Email Providers
            "gmail.com", "outlook.com", "yahoo.com", "protonmail.com", "mail.com",

            // Government
            "gov", "mil", "nasa.gov", "irs.gov", "whitehouse.gov"
    ));

    // Keywords commonly found in phishing URLs
    private static final String[] PHISHING_KEYWORDS = {
            "secure", "account", "signin", "login", "webscr", "verify", "update", "access",
            "authenticate", "authorize", "password", "confirm", "wallet", "billing",
            "recover", "unlock", "validate", "identity", "verification", "authenticate"
    };

    // Weight factors for different analysis components
    private static final double URL_STRUCTURE_WEIGHT = 0.40;
    private static final double SSL_VERIFICATION_WEIGHT = 0.40;
    private static final double ML_MODEL_WEIGHT = 0.20;

    /**
     * Creates a new enhanced URL safety checker
     * @param modelPath Path to the machine learning model file
     */
    public URLSafetyChecker(String modelPath) throws Exception {
        this.classifier = new URLClassifier(modelPath);
        this.sslVerifier = new SSLVerificationService();
        this.structureAnalyzer = new URLStructureAnalyzer();
    }

    /**
     * Performs a comprehensive safety check on a URL
     * @param url The URL to check
     * @return A SafetyResult object containing detailed analysis results
     */
    public SafetyResult checkSafety(String url) {
        try {
            // Step 1: Normalize the URL (always try HTTPS first)
            String normalizedUrl = normalizeURL(url);

            // Step 2: Extract base domain for whitelist checking
            String baseDomain = extractBaseDomain(normalizedUrl);

            // Step 3: Check against trusted domains whitelist
            if (TRUSTED_DOMAINS.contains(baseDomain)) {
                return new SafetyResult("SAFE", 1.0,
                        "Trusted domain in whitelist", normalizedUrl, null, null, new double[]{1.0, 0.0});
            }

            // Special handling for domains ending with trusted TLDs
            for (String trustedDomain : TRUSTED_DOMAINS) {
                if (trustedDomain.startsWith(".") && baseDomain.endsWith(trustedDomain)) {
                    return new SafetyResult("SAFE", 0.95,
                            "Trusted domain extension", normalizedUrl, null, null, new double[]{0.95, 0.05});
                }
            }

            // Step 4: Perform SSL certificate verification
            SSLVerificationService.SSLVerificationResult sslResult =
                    sslVerifier.verifyCertificate(normalizedUrl);

            // If the URL has valid HTTPS but was entered as HTTP, update the normalized URL
            if (sslResult.isHttps && sslResult.hasCertificate && sslResult.isValid &&
                    normalizedUrl.startsWith("http://")) {
                normalizedUrl = normalizedUrl.replace("http://", "https://");
            }

            // Step 5: Perform URL structure analysis
            URLStructureAnalyzer.URLStructureResult structureResult =
                    structureAnalyzer.analyzeStructure(normalizedUrl);

            // Step 6: Extract feature vector for ML model
            double[] features = URLFeatureExtractor.extractFeatures(normalizedUrl);

            // Step 7: Override 'Has HTTPS' feature if SSL certificate is valid
            if (sslResult.isHttps && sslResult.hasCertificate && sslResult.isValid) {
                features[2] = 1.0;  // Set 'Has HTTPS' feature to 1.0 (true)
            }

            // Step 8: Apply heuristic rules and check for obvious phishing signals
            HeuristicResult heuristicResult = applyHeuristics(normalizedUrl);

            // Step 9: For high-risk URL structures, prioritize structure analysis
            if (structureResult.suspicionScore >= 70) {
                double finalScore = 0.9 + (structureResult.suspicionScore - 70) / 300.0;
                return new SafetyResult("DANGEROUS", finalScore,
                        "High-risk URL structure detected", normalizedUrl,
                        structureResult, sslResult, new double[]{0.0, 1.0});
            }

            // Step 10: Special handling for SSL errors
            if (sslResult.errorMessage != null) {
                // Determine risk level based on domain and error type
                double riskScore = calculateSSLErrorRiskScore(sslResult, baseDomain);

                if (riskScore >= 0.85) {
                    return new SafetyResult("DANGEROUS", riskScore,
                            "SSL certificate verification failed: " + sslResult.errorMessage,
                            normalizedUrl, structureResult, sslResult, features.length >= 2 ?
                            new double[]{0.1, 0.9} : null);
                } else if (riskScore >= 0.70) {
                    return new SafetyResult("SUSPICIOUS", riskScore,
                            "SSL certificate error detected: " + sslResult.errorMessage,
                            normalizedUrl, structureResult, sslResult, features.length >= 2 ?
                            new double[]{0.3, 0.7} : null);
                }
                // If lower risk, continue with normal analysis but with SSL error noted
            }

            // Step 11: Determine if we have strong safety signals
            boolean strongSafetySignals = (structureResult.suspicionScore <= 10 &&
                    sslResult.isHttps && sslResult.hasCertificate &&
                    sslResult.isValid && sslResult.matchesDomain);

            // Step 12: Use machine learning model with updated features
            String mlResult = classifier.classify(normalizedUrl, features);
            double[] probs = classifier.getDistribution(normalizedUrl, features);

            // Step 13: Calculate comprehensive verdict
            VerificationResult finalVerdict = calculateFinalVerdict(
                    structureResult, sslResult, mlResult, probs, strongSafetySignals);

            // Create final result with all analysis components
            return new SafetyResult(
                    finalVerdict.verdict,
                    finalVerdict.confidence,
                    finalVerdict.reason,
                    normalizedUrl,
                    structureResult,
                    sslResult,
                    probs
            );

        } catch (Exception e) {
            // If something fails, mark as suspicious to be safe
            return new SafetyResult(
                    "SUSPICIOUS",
                    0.7,
                    "Error during analysis: " + e.getMessage(),
                    url,
                    null,
                    null,
                    new double[]{0.3, 0.7}
            );
        }
    }

    /**
     * Calculates risk score for SSL errors based on error type and domain
     */
    private double calculateSSLErrorRiskScore(SSLVerificationService.SSLVerificationResult sslResult, String domain) {
        double baseScore = 0.7; // Default risk score for SSL errors

        // Adjust based on error type
        if (sslResult.errorMessage.contains("Unsupported or unrecognized SSL message") ||
                sslResult.errorMessage.contains("SSL Handshake failed") ||
                sslResult.errorMessage.contains("certificate_unknown")) {

            baseScore += 0.15; // More severe SSL errors
        }

        // Apply TLD risk factors - some TLDs have historically higher association with malicious sites
        String tld = extractTLD(domain);
        if (tld != null) {
            if (tld.equals("ru") || tld.equals("cn") || tld.equals("su") ||
                    tld.equals("top") || tld.equals("tk") || tld.equals("ml") ||
                    tld.equals("ga") || tld.equals("cf") || tld.equals("gq")) {

                baseScore += 0.15; // Higher risk TLDs with SSL errors are particularly suspicious
            }
        }

        return Math.min(0.95, baseScore); // Cap at 0.95 (95%)
    }

    /**
     * Extracts the TLD from a domain
     */
    private String extractTLD(String domain) {
        if (domain == null || domain.isEmpty()) {
            return null;
        }

        String[] parts = domain.split("\\.");
        if (parts.length >= 2) {
            return parts[parts.length - 1].toLowerCase();
        }

        return null;
    }

    /**
     * Calculates a final comprehensive verdict considering all analysis factors
     */
    private VerificationResult calculateFinalVerdict(
            URLStructureAnalyzer.URLStructureResult structureResult,
            SSLVerificationService.SSLVerificationResult sslResult,
            String mlVerdict,
            double[] mlProbs,
            boolean strongSafetySignals) {

        // Convert structure analysis score to 0-1 scale (higher means more suspicious)
        double structureSuspicionScore = structureResult.suspicionScore / 100.0;

        // Calculate SSL trust score (0-1 scale, higher means more trusted)
        double sslTrustScore = calculateSSLTrustScore(sslResult);

        // Calculate combined score using weighted formula
        double safeFactor = 0;
        double dangerousFactor = 0;

        // Adjust weights based on strong safety signals or SSL errors
        double urlStructureWeight = URL_STRUCTURE_WEIGHT;
        double sslVerificationWeight = SSL_VERIFICATION_WEIGHT;
        double mlModelWeight = ML_MODEL_WEIGHT;

        // If SSL has errors, increase its weight in decision
        if (sslResult != null && sslResult.errorMessage != null) {
            sslVerificationWeight = 0.50;  // 50% weight to SSL
            urlStructureWeight = 0.30;     // 30% to URL structure
            mlModelWeight = 0.20;          // 20% to ML model
        } else if (strongSafetySignals) {
            // If URL structure and SSL both indicate safety, reduce ML impact further
            urlStructureWeight = 0.45;
            sslVerificationWeight = 0.45;
            mlModelWeight = 0.10;
        }

        // Calculate positive and negative factors
        safeFactor += (1 - structureSuspicionScore) * urlStructureWeight;  // Low suspicion = higher safety
        safeFactor += sslTrustScore * sslVerificationWeight;               // Higher SSL trust = higher safety
        safeFactor += mlProbs[0] * mlModelWeight;                          // GOOD probability

        dangerousFactor += structureSuspicionScore * urlStructureWeight;   // High suspicion = more dangerous
        dangerousFactor += (1 - sslTrustScore) * sslVerificationWeight;    // Lower SSL trust = more dangerous
        dangerousFactor += mlProbs[1] * mlModelWeight;                     // BAD probability

        // Normalize to ensure they sum to 1
        double total = safeFactor + dangerousFactor;
        safeFactor /= total;
        dangerousFactor /= total;

        // Determine verdict and confidence
        String verdict;
        double confidence;
        String reason;

        if (safeFactor >= 0.8) {
            verdict = "SAFE";
            confidence = safeFactor;
            reason = buildDetailedReason(structureResult, sslResult, mlVerdict, mlProbs, true, safeFactor);
        } else if (safeFactor >= 0.65) {
            verdict = "PROBABLY SAFE";
            confidence = safeFactor;
            reason = buildDetailedReason(structureResult, sslResult, mlVerdict, mlProbs, true, safeFactor);
        } else if (dangerousFactor >= 0.8) {
            verdict = "DANGEROUS";
            confidence = dangerousFactor;
            reason = buildDetailedReason(structureResult, sslResult, mlVerdict, mlProbs, false, dangerousFactor);
        } else if (dangerousFactor >= 0.65) {
            verdict = "SUSPICIOUS";
            confidence = dangerousFactor;
            reason = buildDetailedReason(structureResult, sslResult, mlVerdict, mlProbs, false, dangerousFactor);
        } else {
            verdict = "UNCERTAIN";
            confidence = Math.max(safeFactor, dangerousFactor);
            reason = "Mixed signals from different analysis methods. Proceed with caution.";
        }

        return new VerificationResult(verdict, confidence, reason);
    }

    /**
     * Calculates a trust score from SSL certificate verification
     * Returns 0-1 value where higher means more trusted
     */
    private double calculateSSLTrustScore(SSLVerificationService.SSLVerificationResult sslResult) {
        if (sslResult == null) {
            return 0.3; // Unknown SSL status
        }

        // Check for error message first
        if (sslResult.errorMessage != null) {
            // Different types of SSL errors have different security implications
            if (sslResult.errorMessage.contains("Unsupported or unrecognized SSL message") ||
                    sslResult.errorMessage.contains("SSL Handshake failed")) {
                // Critical SSL errors suggest potential MITM attack or serious misconfiguration
                return 0.05; // Very low trust score
            } else {
                // Default handling for other SSL errors
                return 0.1;
            }
        }

        if (!sslResult.isHttps) {
            return 0.2; // HTTP is not secure
        }

        if (!sslResult.hasCertificate) {
            return 0.1; // HTTPS but no certificate is very suspicious
        }

        double score = 0.5; // Base score for having a certificate

        // Valid certificate is important
        if (sslResult.isValid) {
            score += 0.2;
        } else {
            return 0.2; // Invalid certificate is a major red flag
        }

        // Domain matching is critical
        if (sslResult.matchesDomain) {
            score += 0.2;
        } else {
            return 0.3; // Certificate not matching domain is a major issue
        }

        // Self-signed certificates are less trustworthy
        if (sslResult.isSelfSigned) {
            score -= 0.2;
        }

        // Nearing expiration is a minor concern
        if (sslResult.isNearingExpiration) {
            score -= 0.05;
        }

        return Math.min(1.0, Math.max(0.0, score)); // Ensure between 0 and 1
    }

    /**
     * Builds a detailed reason for the verdict
     */
    private String buildDetailedReason(
            URLStructureAnalyzer.URLStructureResult structureResult,
            SSLVerificationService.SSLVerificationResult sslResult,
            String mlVerdict,
            double[] mlProbs,
            boolean isSafe,
            double confidence) {

        StringBuilder reason = new StringBuilder();

        // Add verdict explanation
        if (isSafe) {
            if (confidence > 0.9) {
                reason.append("High confidence this URL is safe. ");
            } else if (confidence > 0.7) {
                reason.append("Moderate confidence this URL is safe. ");
            } else {
                reason.append("This URL appears generally safe but proceed with awareness. ");
            }
        } else {
            if (confidence > 0.9) {
                reason.append("High confidence this URL is dangerous. ");
            } else if (confidence > 0.7) {
                reason.append("Moderate confidence this URL is suspicious. ");
            } else {
                reason.append("This URL has some concerning characteristics. ");
            }
        }

        // Add key information about each analysis component

        // SSL certificate information (most critical)
        if (sslResult != null) {
            if (sslResult.errorMessage != null) {
                reason.append("SSL certificate verification failed: ").append(sslResult.errorMessage).append(". ");
            } else if (!sslResult.isHttps) {
                reason.append("No HTTPS connection. ");
            } else if (!sslResult.hasCertificate) {
                reason.append("No SSL certificate found. ");
            } else if (!sslResult.isValid) {
                reason.append("Invalid SSL certificate. ");
            } else if (!sslResult.matchesDomain) {
                reason.append("SSL certificate doesn't match the domain. ");
            } else if (sslResult.isSelfSigned) {
                reason.append("Self-signed certificate. ");
            } else {
                reason.append("Valid SSL certificate. ");
            }
        }

        // Structure analysis
        if (structureResult != null) {
            if (structureResult.suspicionScore < 10) {
                reason.append("URL structure appears benign. ");
            } else if (structureResult.suspicionScore < 30) {
                reason.append("URL structure has minor unusual characteristics. ");
            } else if (structureResult.suspicionScore < 60) {
                reason.append("URL structure contains suspicious elements. ");
            } else {
                reason.append("URL structure matches known phishing patterns. ");
            }
        }

        // ML model
        reason.append("Machine learning classification: ").append(mlVerdict);

        return reason.toString();
    }

    /**
     * Applies various heuristic rules to detect phishing URLs
     */
    private HeuristicResult applyHeuristics(String url) {
        int score = 0;
        StringBuilder reasons = new StringBuilder();

        // 1. Check URL length (phishing URLs are often very long)
        if (url.length() > 100) {
            score += 1;
            reasons.append("URL is unusually long. ");
        }

        // 2. Check for suspicious keywords
        int keywordCount = 0;
        for (String keyword : PHISHING_KEYWORDS) {
            if (url.toLowerCase().contains(keyword.toLowerCase())) {
                keywordCount++;
            }
        }

        if (keywordCount >= 3) {
            score += 3;
            reasons.append("Contains multiple suspicious keywords. ");
        } else if (keywordCount > 0) {
            score += keywordCount;
            reasons.append("Contains suspicious keywords. ");
        }

        // 3. Check for target brand names that don't match the domain
        String domain = extractDomain(url);
        for (String brand : new String[]{"paypal", "apple", "microsoft", "amazon", "facebook"}) {
            if (url.toLowerCase().contains(brand.toLowerCase()) &&
                    !domain.toLowerCase().contains(brand.toLowerCase())) {
                score += 3;
                reasons.append("Contains brand name ('").append(brand)
                        .append("') not matching domain. ");
                break;
            }
        }

        // 4. Check for suspicious patterns
        Pattern[] suspiciousPatterns = {
                // Multiple subdomains
                Pattern.compile("([a-zA-Z0-9][-a-zA-Z0-9]*\\.){4,}[a-zA-Z][-a-zA-Z0-9]*\\.[a-zA-Z]{2,}"),
                // Long hexadecimal or random strings in path
                Pattern.compile("/[a-fA-F0-9]{16,}/"),
                // Repetitive path elements
                Pattern.compile("(?i)(/[a-z]{4,}){4,}")
        };

        for (Pattern pattern : suspiciousPatterns) {
            Matcher matcher = pattern.matcher(url);
            if (matcher.find()) {
                score += 2;
                reasons.append("Contains suspicious URL pattern. ");
                break;
            }
        }

        // 5. Check for domain name issues
        String[] domainParts = domain.split("\\.");
        if (domainParts.length > 0) {
            String mainDomain = domainParts[0];

            // Check for random-looking domain name
            if (mainDomain.length() >= 10 && containsRandomLookingString(mainDomain)) {
                score += 2;
                reasons.append("Domain name appears random or has excessive numbers. ");
            }

            // Check for excessive hyphens
            int hyphenCount = mainDomain.length() - mainDomain.replace("-", "").length();
            if (hyphenCount > 2) {
                score += 1;
                reasons.append("Domain name contains excessive hyphens. ");
            }
        }

        // Calculate final score (max 1.0)
        double normalizedScore = Math.min(score / 10.0, 1.0);
        return new HeuristicResult(normalizedScore, reasons.toString().trim());
    }

    // Helper methods

    /**
     * Normalizes a URL by adding proper protocol if missing
     * Now tries HTTPS first rather than defaulting to HTTP
     */
    private String normalizeURL(String url) {
        url = url.trim();
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            // Try HTTPS first by default for better security
            url = "https://" + url;
        }
        return url;
    }

    /**
     * Extracts the base domain (e.g., example.com) from a URL
     */
    private String extractBaseDomain(String url) {
        try {
            java.net.URL parsedUrl = new java.net.URL(url);
            String host = parsedUrl.getHost();

            // Remove www prefix if present
            if (host.startsWith("www.")) {
                host = host.substring(4);
            }

            // Handle IP addresses
            if (host.matches("\\d+\\.\\d+\\.\\d+\\.\\d+")) {
                return host;
            }

            // Get base domain (last two parts)
            String[] parts = host.split("\\.");
            if (parts.length >= 2) {
                // Handle special TLDs like co.uk
                if (parts.length > 2 &&
                        (parts[parts.length-1].equals("uk") ||
                                parts[parts.length-1].equals("au") ||
                                parts[parts.length-1].equals("jp"))) {
                    if (parts[parts.length-2].equals("co") ||
                            parts[parts.length-2].equals("com") ||
                            parts[parts.length-2].equals("org") ||
                            parts[parts.length-2].equals("net") ||
                            parts[parts.length-2].equals("ac")) {
                        // Return domain + special TLD (e.g., example.co.uk)
                        return parts[parts.length-3] + "." + parts[parts.length-2] + "." + parts[parts.length-1];
                    }
                }

                return parts[parts.length-2] + "." + parts[parts.length-1];
            }

            return host;
        } catch (Exception e) {
            return url;
        }
    }

    /**
     * Extracts just the domain part from a URL
     */
    private String extractDomain(String url) {
        try {
            java.net.URL parsedUrl = new java.net.URL(url);
            return parsedUrl.getHost();
        } catch (Exception e) {
            return url;
        }
    }

    /**
     * Checks if a string appears to be randomly generated
     */
    private boolean containsRandomLookingString(String str) {
        // Check if string has a good mix of numbers and letters
        int digits = 0;
        int letters = 0;

        for (char c : str.toCharArray()) {
            if (Character.isDigit(c)) digits++;
            else if (Character.isLetter(c)) letters++;
        }

        // Random-looking if at least 30% digits and 30% letters
        double digitRatio = (double) digits / str.length();
        double letterRatio = (double) letters / str.length();

        return str.length() > 8 && digitRatio >= 0.3 && letterRatio >= 0.3;
    }

    // Inner classes for results

    /**
     * Final verification result with verdict and confidence
     */
    private static class VerificationResult {
        public final String verdict;
        public final double confidence;
        public final String reason;

        public VerificationResult(String verdict, double confidence, String reason) {
            this.verdict = verdict;
            this.confidence = confidence;
            this.reason = reason;
        }
    }

    /**
     * Represents the result of heuristic analysis
     */
    private static class HeuristicResult {
        public final double score;
        public final String reason;

        public HeuristicResult(double score, String reason) {
            this.score = score;
            this.reason = reason;
        }
    }

    /**
     * Comprehensive safety analysis result
     */
    public static class SafetyResult {
        public final String verdict;
        public final double confidence;
        public final String reason;
        public final String normalizedUrl;
        public final URLStructureAnalyzer.URLStructureResult structureResult;
        public final SSLVerificationService.SSLVerificationResult sslResult;
        public final double[] mlProbabilities;

        public SafetyResult(String verdict, double confidence, String reason, String normalizedUrl) {
            this(verdict, confidence, reason, normalizedUrl, null, null, null);
        }

        public SafetyResult(
                String verdict,
                double confidence,
                String reason,
                String normalizedUrl,
                URLStructureAnalyzer.URLStructureResult structureResult,
                SSLVerificationService.SSLVerificationResult sslResult,
                double[] mlProbabilities) {

            this.verdict = verdict;
            this.confidence = confidence;
            this.reason = reason;
            this.normalizedUrl = normalizedUrl;
            this.structureResult = structureResult;
            this.sslResult = sslResult;
            this.mlProbabilities = mlProbabilities;
        }

        @Override
        public String toString() {
            return String.format("Verdict: %s (Confidence: %.2f%%)\nReason: %s",
                    verdict, confidence * 100, reason);
        }
    }
}