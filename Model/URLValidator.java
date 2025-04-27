package backend;

import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Validates URLs for proper format and existence before analysis
 */
public class URLValidator {

    // Regular expression for basic URL format validation
    private static final String URL_REGEX =
            "^(https?://)?([a-zA-Z0-9]([a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])?\\.)+[a-zA-Z]{2,}(/.*)?$";
    private static final Pattern URL_PATTERN = Pattern.compile(URL_REGEX);

    // Common TLDs for suggesting corrections
    private static final String[] COMMON_TLDS = {
            ".com", ".org", ".net", ".gov", ".edu", ".io", ".co", ".info", ".biz", ".me"
    };

    // Connection timeout for checking domain existence
    private static final int CONNECTION_TIMEOUT = 3000;

    /**
     * Result of URL validation
     */
    public static class ValidationResult {
        public final boolean isValid;
        public final String errorMessage;
        public final String normalizedUrl;
        public final String suggestion;

        public ValidationResult(boolean isValid, String errorMessage, String normalizedUrl, String suggestion) {
            this.isValid = isValid;
            this.errorMessage = errorMessage;
            this.normalizedUrl = normalizedUrl;
            this.suggestion = suggestion;
        }
    }

    /**
     * Validates a URL for format and existence
     * @param urlInput The URL to validate
     * @return ValidationResult containing validity and error details
     */
    public static ValidationResult validate(String urlInput) {
        if (urlInput == null || urlInput.trim().isEmpty()) {
            return new ValidationResult(false, "URL cannot be empty", null, null);
        }

        // Normalize URL input
        String normalized = urlInput.trim();

        // Add protocol if missing
        if (!normalized.startsWith("http://") && !normalized.startsWith("https://")) {
            normalized = "https://" + normalized;
        }

        // Step 1: Check URL format using regex
        Matcher matcher = URL_PATTERN.matcher(normalized);
        if (!matcher.matches()) {
            // If no TLD is present, suggest adding one
            if (!hasTLD(normalized)) {
                String suggestion = suggestWithTLD(normalized);
                return new ValidationResult(false,
                        "Invalid URL format: Missing or invalid domain extension (TLD)",
                        null, suggestion);
            }

            return new ValidationResult(false,
                    "Invalid URL format: The URL doesn't match the expected pattern",
                    null, null);
        }

        // Step 2: Parse URL (this validates protocol, host, port, etc.)
        URL url;
        try {
            url = new URL(normalized);
        } catch (MalformedURLException e) {
            return new ValidationResult(false,
                    "Malformed URL: " + e.getMessage(),
                    null, null);
        }

        // Step 3: Verify domain exists via DNS lookup
        String host = url.getHost();
        try {
            InetAddress address = InetAddress.getByName(host);
            if (address.getHostAddress() == null) {
                return new ValidationResult(false,
                        "Domain does not exist: No DNS record found",
                        null, null);
            }
        } catch (UnknownHostException e) {
            // Domain doesn't exist - try to suggest alternatives
            String suggestion = suggestCorrection(host);
            return new ValidationResult(false,
                    "Domain does not exist: " + host,
                    null, suggestion);
        }

        // Optional Step 4: Check if the server responds (commented out by default)
        // This can slow down validation but provides more certainty
        /*
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(CONNECTION_TIMEOUT);
            connection.setRequestMethod("HEAD");
            connection.connect();
            int responseCode = connection.getResponseCode();
            if (responseCode >= 400) {
                return new ValidationResult(false, 
                    "Server responded with error: " + responseCode, 
                    normalized, null);
            }
        } catch (Exception e) {
            // We'll still consider the URL valid if the domain exists
            // but connectivity might be an issue
        }
        */

        // URL is valid
        return new ValidationResult(true, null, normalized, null);
    }

    /**
     * Checks if the URL has a valid TLD
     */
    private static boolean hasTLD(String url) {
        try {
            URL parsedUrl = new URL(url);
            String host = parsedUrl.getHost();
            int lastDot = host.lastIndexOf('.');

            if (lastDot == -1 || lastDot == host.length() - 1) {
                return false;
            }

            String tld = host.substring(lastDot + 1);
            return tld.length() >= 2;
        } catch (MalformedURLException e) {
            return false;
        }
    }

    /**
     * Suggests a URL with common TLDs if missing
     */
    private static String suggestWithTLD(String url) {
        try {
            URL parsedUrl = new URL(url);
            String host = parsedUrl.getHost();

            if (host.indexOf('.') == -1) {
                // No dots at all - suggest adding .com
                return url.replace(host, host + ".com");
            }

            return url;
        } catch (MalformedURLException e) {
            // If we can't parse, try to add .com to the end
            if (url.startsWith("https://")) {
                String host = url.substring(8);
                return "https://" + host + ".com";
            } else if (url.startsWith("http://")) {
                String host = url.substring(7);
                return "http://" + host + ".com";
            } else {
                return "https://" + url + ".com";
            }
        }
    }

    /**
     * Suggests possible corrections for mistyped domains
     */
    private static String suggestCorrection(String host) {
        // Remove www. if present
        if (host.startsWith("www.")) {
            host = host.substring(4);
        }

        // Try common TLDs for the base domain
        int lastDot = host.lastIndexOf('.');
        if (lastDot > 0) {
            String baseDomain = host.substring(0, lastDot);

            // Check for typos in common domains
            for (String commonDomain : new String[]{"google", "facebook", "amazon", "twitter", "github"}) {
                // Simple Levenshtein-inspired check (not full implementation)
                if (isSimilar(baseDomain, commonDomain)) {
                    return commonDomain + ".com";
                }
            }

            // Try different TLD
            for (String tld : COMMON_TLDS) {
                String suggestion = baseDomain + tld;
                try {
                    InetAddress address = InetAddress.getByName(suggestion);
                    if (address.getHostAddress() != null) {
                        return suggestion;
                    }
                } catch (UnknownHostException e) {
                    // Continue trying
                }
            }
        }

        return null;
    }

    /**
     * Simple string similarity check
     */
    private static boolean isSimilar(String str1, String str2) {
        // Consider similar if:
        // 1. One is contained in the other
        if (str1.contains(str2) || str2.contains(str1)) {
            return true;
        }

        // 2. Length difference is at most 2
        if (Math.abs(str1.length() - str2.length()) > 2) {
            return false;
        }

        // 3. Characters are mostly the same (simple check)
        int matchingChars = 0;
        for (char c : str1.toCharArray()) {
            if (str2.indexOf(c) >= 0) {
                matchingChars++;
            }
        }

        return (double)matchingChars / str1.length() > 0.7;
    }
}