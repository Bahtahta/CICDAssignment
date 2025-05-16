// src/main/java/com/example/secureapp/TokenService.java
// this is a test phrase
package com.example.secureapp;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class TokenService {

    private static final String SECRET_KEY = "mySuperSecretDemoKey"; // Hardcoded for demo - BAD PRACTICE!
    private static final long DEFAULT_EXPIRY_MINUTES = 30;

    public String generateToken(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be null or empty.");
        }
        long expiryTime = System.currentTimeMillis() + (DEFAULT_EXPIRY_MINUTES * 60 * 1000);
        String dataToSign = userId + ":" + expiryTime;
        String signature = calculateSignature(dataToSign);
        return Base64.getEncoder().encodeToString((dataToSign + ":" + signature).getBytes(StandardCharsets.UTF_8));
    }

    public boolean validateToken(String token) throws InvalidTokenException {
        if (token == null || token.trim().isEmpty()) {
            throw new InvalidTokenException("Token cannot be null or empty.");
        }

        String decodedToken;
        try {
            decodedToken = new String(Base64.getDecoder().decode(token), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            throw new InvalidTokenException("Token is not valid Base64.", e);
        }

        String[] parts = decodedToken.split(":");
        if (parts.length != 3) {
            throw new InvalidTokenException("Token format is invalid.");
        }

        String userId = parts[0];
        long expiryTime;
        try {
            expiryTime = Long.parseLong(parts[1]);
        } catch (NumberFormatException e) {
            throw new InvalidTokenException("Token expiry time is not a valid number.", e);
        }
        String providedSignature = parts[2];

        if (System.currentTimeMillis() > expiryTime) {
            throw new InvalidTokenException("Token has expired.");
        }

        String dataToSign = userId + ":" + expiryTime;
        String expectedSignature = calculateSignature(dataToSign);

        if (!expectedSignature.equals(providedSignature)) {
            throw new InvalidTokenException("Token signature is invalid.");
        }
        // Potentially log successful validation or return user ID
        System.out.println("Token validated successfully for user: " + userId);
        return true;
    }

    // private
    String calculateSignature(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = digest.digest((data + SECRET_KEY).getBytes(StandardCharsets.UTF_8));
            // Using hex string for simplicity, Base64 could also be used
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashedBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            // This should not happen with SHA-256
            throw new RuntimeException("Could not create SHA-256 digest", e);
        }
    }
}