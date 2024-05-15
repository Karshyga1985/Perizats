package com.library.utility;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordVerifier {

    // Method to hash a password using SHA-256 algorithm
    public static String hashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashedBytes = digest.digest(password.getBytes());

        // Convert hashed bytes to hexadecimal representation
        StringBuilder hexString = new StringBuilder();
        for (byte b : hashedBytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    // Method to verify password by comparing hashes
    public static boolean verifyPassword(String inputPassword, String storedHashedPassword)
            throws NoSuchAlgorithmException {
        String inputHash = hashPassword(inputPassword);
        return inputHash.equals(storedHashedPassword);
    }

}
