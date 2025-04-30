package model;

import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Analyzes URL structure to identify patterns common in phishing URLs
 */
public class URLStructureAnalyzer {

    // Common file extensions in phishing URLs
    private static final Set<String> SUSPICIOUS_EXTENSIONS = new HashSet<String>() {{
        add("php"); add("html"); add("htm"); add("asp"); add("aspx");
        add("exe"); add("scr"); add("cgi"); add("action");
    }};

    // Common URL parameter names used in phishing sites
    private static final Set<String> SUSPICIOUS_PARAMS = new HashSet<String>() {{
        add("login"); add("password"); add("passwd"); add("account");
        add("submit"); add("email"); add("token"); add("id"); add("session");
        add("auth"); add("authorize"); add("validate"); add("verification");
    }};

    // Brands commonly targeted in phishing attacks
    private static final Set<String> TARGET_BRANDS = new HashSet<String>() {{
        add("paypal"); add("apple"); add("microsoft"); add("amazon");
        add("facebook"); add("google"); add("netflix"); add("instagram");
        add("bank"); add("wellsfargo"); add("chase"); add("citibank");
        add("bankofamerica"); add("amex"); add("americanexpress");
        add("outlook"); add("office365"); add("gmail"); add("icloud");
    }};

    // Patterns for detecting suspicious URL structures
    private static final Pattern[] SUSPICIOUS_PATTERNS = new Pattern[] {
            // Multiple subdomains (more than 3)
            Pattern.compile("([a-zA-Z0-9][-a-zA-Z0-9]*\\.){4,}[a-zA-Z][-a-zA-Z0-9]*\\.[a-zA-Z]{2,}"),

            // Long hexadecimal or random strings in path
            Pattern.compile("/[a-fA-F0-9]{16,}/"),
            Pattern.compile("/[a-zA-Z0-9]{20,}/"),

            // Brand name in subdomain with unrelated domain
            Pattern.compile("(?i)(paypal|apple|microsoft|amazon|facebook)\\.(?!com|net|org|co\\.uk|ca|au).*"),

            // Multiple occurrences of "secure", "login", etc.
            Pattern.compile("(?i)(secure.*secure|login.*login|account.*account)"),

            // Excessive path depth (more than 5 segments)
            Pattern.compile("(?i)/[^/]+/[^/]+/[^/]+/[^/]+/[^/]+/[^/]+/"),

            // Path impersonating file system
            Pattern.compile("(?i)/home/|/users/|/admin/|/root/|/sys/|/system/"),

            // Multiple redirects in URL
            Pattern.compile("(?i)(redirect|redir|link|click|goto).*?(redirect|redir|link|click|goto)")
    };

    /**
     * Analyzes the structure of a URL to identify potential phishing indicators
     *
     * @param urlStr The URL to analyze
     * @return A URLStructureResult object containing the analysis results
     */
    public URLStructureResult analyzeStructure(String urlStr) {
        URLStructureResult result = new URLStructureResult();
        result.url = urlStr;

        try {
            // Parse the URL
            URL url = new URL(urlStr);
            URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(),
                    url.getPort(), url.getPath(), url.getQuery(), url.getRef());

            // Analyze domain structure
            analyzeDomain(url.getHost(), result);

            // Analyze path structure
            analyzePath(uri.getPath(), result);

            // Analyze query parameters
            analyzeQueryParams(uri.getQuery(), result);

            // Check for presence of user info (username:password format)
            if (url.getUserInfo() != null) {
                result.hasCredentialsInUrl = true;
                result.suspiciousElements.add("Contains credentials in URL (username:password format)");
            }

            // Check for port specification (except common ports)
            int port = url.getPort();
            if (port != -1 && port != 80 && port != 443) {
                result.hasUncommonPort = true;
                result.suspiciousElements.add("Uses uncommon port number: " + port);
            }

            // Check for suspicious patterns
            for (Pattern pattern : SUSPICIOUS_PATTERNS) {
                Matcher matcher = pattern.matcher(urlStr);
                if (matcher.find()) {
                    result.matchesSuspiciousPattern = true;
                    result.suspiciousElements.add("Matches suspicious URL pattern: " + matcher.group());
                    break;
                }
            }

            // Calculate overall suspicion score (0-100)
            calculateSuspicionScore(result);

        } catch (Exception e) {
            result.error = "Error analyzing URL structure: " + e.getMessage();
        }

        return result;
    }

    /**
     * Analyzes the domain portion of the URL
     */
    private void analyzeDomain(String domain, URLStructureResult result) {
        if (domain == null) return;

        // Count subdomains
        String[] parts = domain.split("\\.");
        if (parts.length > 0) {
            result.domainTLD = parts[parts.length - 1];
        }
        if (parts.length > 1) {
            result.domainName = parts[parts.length - 2];

            // Check for numeric-only domain name
            if (result.domainName.matches("\\d+")) {
                result.hasNumericOnlyDomain = true;
                result.suspiciousElements.add("Domain name consists of only numbers");
            }

            // Check for excessive hyphens in domain name
            int hyphenCount = result.domainName.length() - result.domainName.replace("-", "").length();
            if (hyphenCount > 2) {
                result.hasExcessiveHyphens = true;
                result.suspiciousElements.add("Domain name contains excessive hyphens");
            }

            // Check for very long domain name
            if (result.domainName.length() > 20) {
                result.suspiciousElements.add("Unusually long domain name: " + result.domainName);
            }

            // Check for random-looking domain name (entropy)
            if (containsRandomLookingString(result.domainName)) {
                result.suspiciousElements.add("Domain name appears randomly generated");
            }
        }

        // Count and analyze subdomains
        if (parts.length > 2) {
            result.subdomainCount = parts.length - 2;

            // Check for excessive subdomains
            if (result.subdomainCount > 3) {
                result.hasExcessiveSubdomains = true;
                result.suspiciousElements.add("Excessive number of subdomains: " + result.subdomainCount);
            }

            // Check for brand names in subdomains
            for (int i = 0; i < parts.length - 2; i++) {
                String subdomain = parts[i].toLowerCase();
                for (String brand : TARGET_BRANDS) {
                    if (subdomain.contains(brand)) {
                        // Brand in subdomain but not in main domain
                        if (!result.domainName.toLowerCase().contains(brand)) {
                            result.hasBrandInSubdomain = true;
                            result.suspiciousElements.add("Brand name in subdomain doesn't match domain: " + brand);
                            break;
                        }
                    }
                }
            }
        }

        // Check for IP address instead of domain name
        if (domain.matches("\\d+\\.\\d+\\.\\d+\\.\\d+")) {
            result.isIPAddress = true;
            result.suspiciousElements.add("Uses IP address instead of domain name");
        }

        // Check for IDN homograph attack potential (Unicode characters that look like ASCII)
        if (domain.contains("xn--")) {
            result.isPotentialIDNHomograph = true;
            result.suspiciousElements.add("Potential IDN homograph attack (Unicode domain that may mimic known sites)");
        }
    }

    /**
     * Analyzes the path portion of the URL
     */
    private void analyzePath(String path, URLStructureResult result) {
        if (path == null || path.isEmpty() || path.equals("/")) {
            return;
        }

        // Count path segments
        String[] segments = path.split("/");
        result.pathDepth = segments.length - 1; // -1 because splitting "/" gives an empty first element

        // Check for excessive path depth
        if (result.pathDepth > 5) {
            result.hasDeepPathStructure = true;
            result.suspiciousElements.add("Unusually deep path structure: " + result.pathDepth + " levels");
        }

        // Check for suspicious file extensions
        for (String ext : SUSPICIOUS_EXTENSIONS) {
            if (path.toLowerCase().endsWith("." + ext)) {
                result.hasSuspiciousFileExtension = true;
                result.suspiciousElements.add("Uses potentially suspicious file extension: " + ext);
                break;
            }
        }

        // Check for brand names in path
        for (String brand : TARGET_BRANDS) {
            if (path.toLowerCase().contains(brand)) {
                result.hasBrandNameInPath = true;
                result.suspiciousElements.add("Contains brand name in path: " + brand);
                break;
            }
        }

        // Look for suspicious keywords in path
        String[] suspiciousPathKeywords = {"login", "secure", "account", "signin", "verify", "update", "confirm"};
        for (String keyword : suspiciousPathKeywords) {
            if (path.toLowerCase().contains(keyword)) {
                result.hasSuspiciousPathKeywords = true;
                result.suspiciousElements.add("Contains suspicious keyword in path: " + keyword);
                break;
            }
        }

        // Check for hexadecimal or random-looking strings in path
        for (String segment : segments) {
            if (segment.length() > 16 && (segment.matches("[a-fA-F0-9]+") ||
                    containsRandomLookingString(segment))) {
                result.hasRandomOrHexStrings = true;
                result.suspiciousElements.add("Contains long hexadecimal or random-looking string in path");
                break;
            }
        }

        // Check for repetitive patterns in path
        if (hasRepetitivePatterns(path)) {
            result.hasRepetitivePatterns = true;
            result.suspiciousElements.add("Contains repetitive patterns in path");
        }
    }

    /**
     * Analyzes the query parameters of the URL
     */
    private void analyzeQueryParams(String query, URLStructureResult result) {
        if (query == null || query.isEmpty()) {
            return;
        }

        // Count query parameters
        String[] params = query.split("&");
        result.queryParamCount = params.length;

        // Check for excessive query parameters
        if (result.queryParamCount > 8) {
            result.hasExcessiveParams = true;
            result.suspiciousElements.add("Excessive number of query parameters: " + result.queryParamCount);
        }

        // Parse and analyze individual parameters
        Map<String, String> paramMap = new HashMap<>();
        for (String param : params) {
            String[] keyValue = param.split("=", 2);
            if (keyValue.length > 0) {
                String key = keyValue[0].toLowerCase();
                String value = (keyValue.length > 1) ? keyValue[1] : "";

                paramMap.put(key, value);

                // Check for suspicious parameter names
                if (SUSPICIOUS_PARAMS.contains(key)) {
                    result.hasSuspiciousParamNames = true;
                    result.suspiciousElements.add("Contains suspicious query parameter: " + key);
                }

                // Check for encoded JavaScript in parameters
                if (value.contains("%3Cscript") || value.contains("%3C/script") ||
                        value.contains("%3Calert") || value.contains("javascript%3A")) {
                    result.hasEncodedScript = true;
                    result.suspiciousElements.add("Contains encoded JavaScript in query parameters");
                }

                // Check for unusually long parameter values (potential XSS or SQL injection)
                if (value.length() > 100) {
                    result.hasLongParamValues = true;
                    result.suspiciousElements.add("Contains unusually long parameter value");
                }

                // Check for redirect parameters
                if (key.contains("redirect") || key.contains("return") ||
                        key.contains("url") || key.contains("link") || key.contains("goto")) {
                    result.hasRedirectParams = true;
                    result.suspiciousElements.add("Contains redirect parameter: " + key);
                }
            }
        }
    }

    /**
     * Calculates an overall suspicion score based on the analysis results
     */
    private void calculateSuspicionScore(URLStructureResult result) {
        int score = 0;

        // Domain-related factors
        if (result.isIPAddress) score += 20;
        if (result.hasExcessiveSubdomains) score += 15;
        if (result.hasBrandInSubdomain) score += 15;
        if (result.hasNumericOnlyDomain) score += 10;
        if (result.hasExcessiveHyphens) score += 10;
        if (result.isPotentialIDNHomograph) score += 25;

        // Path-related factors
        if (result.hasDeepPathStructure) score += 10;
        if (result.hasSuspiciousFileExtension) score += 5;
        if (result.hasBrandNameInPath) score += 10;
        if (result.hasSuspiciousPathKeywords) score += 10;
        if (result.hasRandomOrHexStrings) score += 15;
        if (result.hasRepetitivePatterns) score += 10;

        // Query-related factors
        if (result.hasExcessiveParams) score += 5;
        if (result.hasSuspiciousParamNames) score += 10;
        if (result.hasEncodedScript) score += 20;
        if (result.hasLongParamValues) score += 5;
        if (result.hasRedirectParams) score += 10;

        // Other factors
        if (result.hasCredentialsInUrl) score += 20;
        if (result.hasUncommonPort) score += 10;
        if (result.matchesSuspiciousPattern) score += 15;

        // Cap at 100
        result.suspicionScore = Math.min(score, 100);

        // Determine the risk level based on the score
        if (result.suspicionScore >= 70) {
            result.riskLevel = "High";
        } else if (result.suspicionScore >= 40) {
            result.riskLevel = "Medium";
        } else if (result.suspicionScore >= 20) {
            result.riskLevel = "Low";
        } else {
            result.riskLevel = "Minimal";
        }
    }

    /**
     * Checks if a string appears to be randomly generated
     */
    private boolean containsRandomLookingString(String str) {
        // Calculate character class distribution
        int lowercase = 0, uppercase = 0, digits = 0, others = 0;

        for (char c : str.toCharArray()) {
            if (Character.isLowerCase(c)) lowercase++;
            else if (Character.isUpperCase(c)) uppercase++;
            else if (Character.isDigit(c)) digits++;
            else others++;
        }

        // Calculate entropy
        double total = str.length();
        double entropy = 0;

        if (lowercase > 0) {
            double p = lowercase / total;
            entropy -= p * Math.log(p) / Math.log(2);
        }
        if (uppercase > 0) {
            double p = uppercase / total;
            entropy -= p * Math.log(p) / Math.log(2);
        }
        if (digits > 0) {
            double p = digits / total;
            entropy -= p * Math.log(p) / Math.log(2);
        }
        if (others > 0) {
            double p = others / total;
            entropy -= p * Math.log(p) / Math.log(2);
        }

        // String is considered random if it's at least 10 chars with high entropy
        // or has a mix of character types in a longer string
        return (str.length() >= 10 && entropy > 1.8) ||
                (str.length() >= 15 && lowercase > 0 && (uppercase > 0 || digits > 0));
    }

    /**
     * Checks if a string contains repetitive patterns
     */
    private boolean hasRepetitivePatterns(String str) {
        // Check for repeated substrings of length 3-10
        for (int len = 3; len <= 10 && len < str.length() / 2; len++) {
            for (int i = 0; i <= str.length() - len; i++) {
                String pattern = str.substring(i, i + len);
                int lastIndex = i;
                int count = 1;

                while (lastIndex != -1) {
                    lastIndex = str.indexOf(pattern, lastIndex + len);
                    if (lastIndex != -1) count++;
                }

                if (count >= 3) return true;
            }
        }

        return false;
    }

    /**
     * Result class for URL structure analysis
     */
    public static class URLStructureResult {
        public String url;
        public String error;

        // Domain information
        public String domainName = "";
        public String domainTLD = "";
        public int subdomainCount = 0;
        public boolean isIPAddress = false;
        public boolean hasExcessiveSubdomains = false;
        public boolean hasBrandInSubdomain = false;
        public boolean hasNumericOnlyDomain = false;
        public boolean hasExcessiveHyphens = false;
        public boolean isPotentialIDNHomograph = false;

        // Path information
        public int pathDepth = 0;
        public boolean hasDeepPathStructure = false;
        public boolean hasSuspiciousFileExtension = false;
        public boolean hasBrandNameInPath = false;
        public boolean hasSuspiciousPathKeywords = false;
        public boolean hasRandomOrHexStrings = false;
        public boolean hasRepetitivePatterns = false;

        // Query parameter information
        public int queryParamCount = 0;
        public boolean hasExcessiveParams = false;
        public boolean hasSuspiciousParamNames = false;
        public boolean hasEncodedScript = false;
        public boolean hasLongParamValues = false;
        public boolean hasRedirectParams = false;

        // Other suspicious elements
        public boolean hasCredentialsInUrl = false;
        public boolean hasUncommonPort = false;
        public boolean matchesSuspiciousPattern = false;

        // Overall assessment
        public int suspicionScore = 0;
        public String riskLevel = "Minimal";
        public Set<String> suspiciousElements = new HashSet<>();

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();

            if (error != null) {
                sb.append("Error: ").append(error).append("\n");
                return sb.toString();
            }

            sb.append("URL Structure Analysis Results:\n");
            sb.append("  Risk Level: ").append(riskLevel).append(" (Score: ").append(suspicionScore).append("/100)\n");

            if (!suspiciousElements.isEmpty()) {
                sb.append("\nSuspicious Elements:\n");
                for (String element : suspiciousElements) {
                    sb.append("  - ").append(element).append("\n");
                }
            }

            return sb.toString();
        }
    }
}