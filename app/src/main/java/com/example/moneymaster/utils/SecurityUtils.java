package com.example.moneymaster.utils;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;


public class SecurityUtils {

    private SecurityUtils() {
        // Evitar instanciación
    }

    /**
     * Hashea una contraseña. Devuelve el salt:sha256hash.
     * Llamar al registrar usuario.
     */
    public static String hashPassword(String password) {
        String salt = generateSalt();
        String hash = sha256(salt + password); // ← salt PRIMERO
        return salt + ":" + hash;
    }

    /**
     * Verificamos si una contraseña plana coincide con el hash almacenado.
     * Llamar al hacer login.
     */
    public static boolean verifyPassword(String password, String storedHash) {
        if (storedHash == null) return false;
        if (!storedHash.contains(":")) return false;
        String[] parts = storedHash.split(":", 2);
        String salt         = parts[0];
        String expectedHash = parts[1];
        String inputHash    = sha256(salt + password); // ← salt PRIMERO (igual que arriba)
        return inputHash.equals(expectedHash);
    }

    private static String generateSalt() {
        SecureRandom rng = new SecureRandom();
        byte[] saltBytes = new byte[16];
        rng.nextBytes(saltBytes);
        return bytesToHex(saltBytes);
    }

    private static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes("UTF-8")); // ← UTF-8 explícito
            return bytesToHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 no disponible", e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        BigInteger bi = new BigInteger(1, bytes);
        String hex = bi.toString(16);
        int expected = bytes.length * 2;
        while (hex.length() < expected) hex = "0" + hex;
        return hex;
    }
}