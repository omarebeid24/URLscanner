package backend;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class URLFeatureExtractor {

    private static final String[] SUSPICIOUS_WORDS = {
            "secure", "account", "update", "login", "verify", "password", "bank", "confirm",
            "ebay", "paypal", "signin", "webscr", "invoice", "billing"
    };

    private static final String[] SHORTENERS = {
            "bit.ly", "goo.gl", "tinyurl", "ow.ly"
    };

    public static double[] extractFeatures(String urlString) {
        double[] features = new double[13];

        try {
            // Add protocol if missing (match Python urlparse behavior)
            if (!urlString.startsWith("http://") && !urlString.startsWith("https://")) {
                urlString = "http://" + urlString;
            }

            URL url = new URL(urlString);

            // Extract TLD components (similar to tldextract in Python)
            TLDExtract tldInfo = extractTLD(url.getHost());
            String domain = tldInfo.domain + "." + tldInfo.suffix;

            // 1. URL length
            features[0] = urlString.length();

            // 2. Has IP address (match Python regex pattern)
            boolean hasIP = Pattern.matches(".*\\d+\\.\\d+\\.\\d+\\.\\d+.*", url.getHost());
            features[1] = hasIP ? 1 : 0;

            // 3. Has HTTPS
            features[2] = url.getProtocol().equalsIgnoreCase("https") ? 1 : 0;

            // 4. Number of dots (full URL like Python)
            features[3] = urlString.length() - urlString.replace(".", "").length();

            // 5. Has '@' symbol (full URL check like Python)
            features[4] = urlString.contains("@") ? 1 : 0;

            // 6. URL depth (match Python's approach)
            String path = url.getPath();
            if (path != null && !path.isEmpty()) {
                String[] segments = path.split("/");
                features[5] = (int) java.util.Arrays.stream(segments)
                        .filter(s -> !s.isEmpty()).count();
            } else {
                features[5] = 0;
            }

            // 7. Is shortened URL (match Python's approach)
            boolean isShortened = false;
            for (String shortener : SHORTENERS) {
                if (urlString.contains(shortener)) {
                    isShortened = true;
                    break;
                }
            }
            features[6] = isShortened ? 1 : 0;

            // 8 & 9. Suspicious word check and count (match Python's approach)
            int suspiciousWordCount = 0;
            for (String word : SUSPICIOUS_WORDS) {
                if (urlString.toLowerCase().contains(word)) suspiciousWordCount++;
            }
            features[7] = suspiciousWordCount > 0 ? 1 : 0;
            features[8] = suspiciousWordCount;

            // 10. Has encoded chars (match Python's approach)
            features[9] = Pattern.compile("%[0-9a-fA-F]{2}").matcher(urlString).find() ? 1 : 0;

            // 11. Number of subdomains (match Python's approach)
            features[10] = tldInfo.subdomains.length;

            // 12. Domain entropy (match Python's approach - calculate on domain+TLD only)
            features[11] = Math.round(calculateShannonEntropy(domain) * 1000.0) / 1000.0;

            // 13. Has port number
            features[12] = url.getPort() != -1 ? 1 : 0;

        } catch (Exception e) {
            System.err.println("❌ Failed to parse URL: " + urlString);
            e.printStackTrace();
            for (int i = 0; i < features.length; i++) features[i] = 0;
        }

        return features;
    }

    // Shannon entropy calculation matching Python's implementation
    private static double calculateShannonEntropy(String input) {
        if (input == null || input.isEmpty()) return 0.0;

        Map<Character, Integer> frequencyMap = new HashMap<>();

        // Count character frequencies
        for (char c : input.toCharArray()) {
            frequencyMap.put(c, frequencyMap.getOrDefault(c, 0) + 1);
        }

        // Calculate Shannon entropy
        double entropy = 0.0;
        int len = input.length();
        for (int count : frequencyMap.values()) {
            double probability = (double) count / len;
            entropy -= probability * (Math.log(probability) / Math.log(2));
        }

        return entropy;
    }

    // Simple TLDExtract class to mimic Python's tldextract functionality
    private static class TLDExtract {
        String[] subdomains;
        String domain;
        String suffix;

        TLDExtract(String[] subdomains, String domain, String suffix) {
            this.subdomains = subdomains;
            this.domain = domain;
            this.suffix = suffix;
        }
    }

    // Extract TLD components similar to Python's tldextract
    private static TLDExtract extractTLD(String hostname) {
        if (hostname == null || hostname.isEmpty()) {
            return new TLDExtract(new String[0], "", "");
        }

        String[] parts = hostname.split("\\.");

        if (parts.length == 1) {
            return new TLDExtract(new String[0], parts[0], "");
        }

        // Handle common TLDs and determine domain vs subdomain
        String suffix = parts[parts.length - 1];
        String domain = parts[parts.length - 2];

        // Handle multi-part TLDs like co.uk, com.br
        if (parts.length > 2) {
            // Check for common country-specific TLDs
            if (parts.length > 3 &&
                    (suffix.equals("uk") || suffix.equals("jp") || suffix.equals("au") ||
                            suffix.equals("br") || suffix.equals("in"))) {
                String secondLevel = parts[parts.length - 2];
                if (secondLevel.equals("co") || secondLevel.equals("com") ||
                        secondLevel.equals("org") || secondLevel.equals("net") ||
                        secondLevel.equals("ac") || secondLevel.equals("gov")) {
                    suffix = secondLevel + "." + suffix;
                    domain = parts[parts.length - 3];
                }
            }
        }

        // Extract subdomains (everything before domain)
        ArrayList<String> subdomainList = new ArrayList<>();
        for (int i = 0; i < parts.length - (suffix.contains(".") ? 3 : 2); i++) {
            if (!parts[i].isEmpty()) {
                subdomainList.add(parts[i]);
            }
        }

        String[] subdomains = subdomainList.toArray(new String[0]);
        return new TLDExtract(subdomains, domain, suffix);
    }
}