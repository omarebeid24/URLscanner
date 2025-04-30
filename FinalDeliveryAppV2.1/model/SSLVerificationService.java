package model;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Service for verifying SSL certificates of websites
 */
public class SSLVerificationService {

    private static final long CERTIFICATE_EXPIRY_WARNING_DAYS = 30;
    private static final int SSL_TIMEOUT_MS = 5000; // 5 seconds

    /**
     * Checks the SSL certificate of the given URL
     *
     * @param urlStr The URL to check
     * @return A SSLVerificationResult object containing the verification results
     */
    public SSLVerificationResult verifyCertificate(String urlStr) {
        SSLVerificationResult result = new SSLVerificationResult();

        try {
            // Convert http to https if needed
            URL url = new URL(urlStr);
            if (url.getProtocol().equals("http")) {
                urlStr = urlStr.replaceFirst("http", "https");
                url = new URL(urlStr);
                result.isHttps = true;
            } else if (url.getProtocol().equals("https")) {
                result.isHttps = true;
            } else {
                result.isHttps = false;
                result.errorMessage = "URL does not use HTTP/HTTPS protocol";
                return result;
            }

            // Create SSL context and initialize trust manager
            SSLContext sslContext = SSLContext.getInstance("TLS");
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }
                    }
            };

            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            // Create a custom hostname verifier
            CustomHostnameVerifier hostnameVerifier = new CustomHostnameVerifier();

            // Set SSL context and hostname verifier
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);

            // Connect to the URL
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setConnectTimeout(SSL_TIMEOUT_MS);
            connection.setReadTimeout(SSL_TIMEOUT_MS);
            connection.connect();

            // Get the certificate information
            Certificate[] certificates = connection.getServerCertificates();

            // Check if any certificates were returned
            if (certificates.length == 0) {
                result.hasCertificate = false;
                result.errorMessage = "No SSL certificate found";
                return result;
            }

            result.hasCertificate = true;

            // Analyze the first certificate (the server's certificate)
            if (certificates[0] instanceof X509Certificate) {
                X509Certificate cert = (X509Certificate) certificates[0];

                // Check certificate validity
                try {
                    cert.checkValidity();
                    result.isValid = true;
                } catch (CertificateExpiredException e) {
                    result.isValid = false;
                    result.errorMessage = "Certificate has expired";
                } catch (CertificateNotYetValidException e) {
                    result.isValid = false;
                    result.errorMessage = "Certificate is not yet valid";
                }

                // Get certificate details
                result.issuer = cert.getIssuerX500Principal().getName();
                result.subject = cert.getSubjectX500Principal().getName();
                result.validFrom = cert.getNotBefore();
                result.validTo = cert.getNotAfter();
                result.serialNumber = cert.getSerialNumber().toString(16);

                // Check certificate matching the domain
                result.matchesDomain = hostnameVerifier.isMatchingDomain(cert, url.getHost());

                // Check if certificate is self-signed
                result.isSelfSigned = cert.getIssuerX500Principal().equals(cert.getSubjectX500Principal());

                // Check if certificate is nearing expiration
                long daysToExpiration = getDaysToExpiration(cert.getNotAfter());
                result.daysToExpiration = daysToExpiration;
                result.isNearingExpiration = daysToExpiration <= CERTIFICATE_EXPIRY_WARNING_DAYS;

                // Extract common name (CN) from the subject
                String subject = cert.getSubjectX500Principal().getName();
                String[] parts = subject.split(",");
                for (String part : parts) {
                    if (part.startsWith("CN=")) {
                        result.commonName = part.substring(3);
                        break;
                    }
                }
            }

            connection.disconnect();

        } catch (MalformedURLException e) {
            result.errorMessage = "Malformed URL: " + e.getMessage();
        } catch (SSLHandshakeException e) {
            result.hasCertificate = false;
            result.isValid = false;
            result.errorMessage = "SSL Handshake failed: " + e.getMessage();
        } catch (IOException e) {
            result.errorMessage = "I/O error: " + e.getMessage();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            result.errorMessage = "SSL context initialization error: " + e.getMessage();
        } catch (Exception e) {
            result.errorMessage = "Unexpected error: " + e.getMessage();
        }

        return result;
    }

    /**
     * Calculate days to certificate expiration
     */
    private long getDaysToExpiration(Date expirationDate) {
        Date now = new Date();
        long diffInMillies = expirationDate.getTime() - now.getTime();
        return TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
    }

    /**
     * Custom hostname verifier that checks if the certificate matches the domain
     */
    private static class CustomHostnameVerifier implements HostnameVerifier {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            // Always return true so we can connect to any site for analysis
            return true;
        }

        /**
         * Check if the certificate actually matches the domain
         */
        public boolean isMatchingDomain(X509Certificate cert, String hostname) {
            try {
                String cn = null;
                String subjectPrincipal = cert.getSubjectX500Principal().getName();

                // Extract CN
                for (String part : subjectPrincipal.split(",")) {
                    if (part.trim().startsWith("CN=")) {
                        cn = part.trim().substring(3);
                        break;
                    }
                }

                if (cn != null) {
                    // Direct match
                    if (cn.equalsIgnoreCase(hostname)) {
                        return true;
                    }

                    // Wildcard match
                    if (cn.startsWith("*.") && hostname.indexOf('.') != -1) {
                        String hostWithoutSubdomain = hostname.substring(hostname.indexOf('.'));
                        String cnWithoutWildcard = cn.substring(1); // Remove *
                        return hostWithoutSubdomain.equalsIgnoreCase(cnWithoutWildcard);
                    }
                }

                // No match found
                return false;

            } catch (Exception e) {
                return false;
            }
        }
    }

    /**
     * Result class for SSL verification
     */
    public static class SSLVerificationResult {
        public boolean isHttps = false;
        public boolean hasCertificate = false;
        public boolean isValid = false;
        public boolean matchesDomain = false;
        public boolean isSelfSigned = false;
        public boolean isNearingExpiration = false;
        public long daysToExpiration = 0;
        public String issuer = "";
        public String subject = "";
        public String commonName = "";
        public Date validFrom = null;
        public Date validTo = null;
        public String serialNumber = "";
        public String errorMessage = null;

        public int getScore() {
            int score = 0;

            // Basic checks
            if (!isHttps) score -= 2;
            if (!hasCertificate) score -= 5;
            if (!isValid) score -= 5;

            // More detailed checks
            if (!matchesDomain) score -= 5;
            if (isSelfSigned) score -= 3;
            if (isNearingExpiration) score -= 1;

            return score;
        }

        public boolean hasPotentialIssues() {
            return !isHttps || !hasCertificate || !isValid ||
                    !matchesDomain || isSelfSigned || isNearingExpiration;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();

            if (!isHttps) {
                sb.append("URL does not use HTTPS\n");
                return sb.toString();
            }

            if (errorMessage != null) {
                sb.append("Error: ").append(errorMessage).append("\n");
                return sb.toString();
            }

            if (!hasCertificate) {
                sb.append("No SSL certificate found\n");
                return sb.toString();
            }

            sb.append("Certificate Information:\n");
            sb.append("  Valid: ").append(isValid ? "Yes" : "No").append("\n");
            sb.append("  Matches Domain: ").append(matchesDomain ? "Yes" : "No").append("\n");
            sb.append("  Self-signed: ").append(isSelfSigned ? "Yes (Suspicious)" : "No").append("\n");
            sb.append("  Common Name: ").append(commonName).append("\n");
            sb.append("  Issuer: ").append(issuer).append("\n");
            sb.append("  Valid From: ").append(validFrom).append("\n");
            sb.append("  Valid To: ").append(validTo).append("\n");
            sb.append("  Days to Expiration: ").append(daysToExpiration).append("\n");

            return sb.toString();
        }
    }
}