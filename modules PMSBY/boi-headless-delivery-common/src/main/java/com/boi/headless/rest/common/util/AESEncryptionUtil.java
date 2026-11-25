package com.boi.headless.rest.common.util;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.Base64;
import com.liferay.portal.kernel.util.Validator;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * AES-256 Encryption Utility for PII data
 * Algorithm: AES/CBC/PKCS5Padding
 * Key Size: 256 bits
 * IV: Random 16 bytes prepended to encrypted data
 */
public class AESEncryptionUtil {

    private static final Log LOG = LogFactoryUtil.getLog(AESEncryptionUtil.class);
    
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String KEY_ALGORITHM = "AES";
    private static final int IV_SIZE = 16;
    private static final int KEY_SIZE = 256;
    
    private static SecretKey secretKey;
    private static boolean encryptionEnabled = false;
    
    /**
     * Initialize encryption with Base64 encoded key
     * @param base64Key Base64 encoded AES-256 key
     */
    public static void init(String base64Key) {
        if (Validator.isNull(base64Key)) {
            LOG.warn("AES encryption key is not configured. Encryption is disabled.");
            encryptionEnabled = false;
            return;
        }
        
        try {
            byte[] decodedKey = Base64.decode(base64Key);
            if (decodedKey.length != 32) {
                LOG.error("Invalid AES key length: " + decodedKey.length + ". Expected 32 bytes for AES-256.");
                encryptionEnabled = false;
                return;
            }
            secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, KEY_ALGORITHM);
            encryptionEnabled = true;
            LOG.info("AES-256 encryption initialized successfully");
        } catch (Exception e) {
            LOG.error("Failed to initialize AES encryption: " + e.getMessage());
            encryptionEnabled = false;
        }
    }
    
    /**
     * Check if encryption is enabled
     */
    public static boolean isEncryptionEnabled() {
        return encryptionEnabled;
    }
    
    /**
     * Encrypt data using AES-256 CBC
     * @param data Plain text data to encrypt
     * @return Base64 encoded string with IV prepended, or original data if encryption disabled
     */
    public static String encrypt(String data) {
        if (!encryptionEnabled || Validator.isNull(data)) {
            return data;
        }
        
        try {
            // Generate random IV
            byte[] iv = new byte[IV_SIZE];
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            
            // Initialize cipher
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
            
            // Encrypt data
            byte[] encryptedBytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            
            // Combine IV + Encrypted Data
            byte[] combined = new byte[iv.length + encryptedBytes.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encryptedBytes, 0, combined, iv.length, encryptedBytes.length);
            
            // Return Base64 encoded
            return Base64.encode(combined);
            
        } catch (Exception e) {
            LOG.error("Encryption failed: " + e.getMessage());
            return data;
        }
    }
    
    /**
     * Decrypt data using AES-256 CBC
     * @param encryptedData Base64 encoded string with IV prepended
     * @return Decrypted plain text, or original data if decryption fails
     */
    public static String decrypt(String encryptedData) {
        if (!encryptionEnabled || Validator.isNull(encryptedData)) {
            return encryptedData;
        }
        
        try {
            byte[] combined = Base64.decode(encryptedData);
            
            if (combined.length < IV_SIZE) {
                LOG.warn("Encrypted data too short, returning as is");
                return encryptedData;
            }
            
            // Extract IV (first 16 bytes)
            byte[] iv = new byte[IV_SIZE];
            System.arraycopy(combined, 0, iv, 0, iv.length);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            
            // Extract encrypted data
            byte[] encrypted = new byte[combined.length - iv.length];
            System.arraycopy(combined, iv.length, encrypted, 0, encrypted.length);
            
            // Initialize cipher
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
            
            // Decrypt
            byte[] decryptedBytes = cipher.doFinal(encrypted);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
            
        } catch (Exception e) {
            LOG.error("Decryption failed: " + e.getMessage());
            return encryptedData;
        }
    }
    
    /**
     * Generate a new AES-256 key for sharing with CBS team
     * @return Base64 encoded AES-256 key
     */
    public static String generateNewKey() {
        try {
            javax.crypto.KeyGenerator keyGen = javax.crypto.KeyGenerator.getInstance(KEY_ALGORITHM);
            keyGen.init(KEY_SIZE, new SecureRandom());
            SecretKey key = keyGen.generateKey();
            return Base64.encode(key.getEncoded());
        } catch (Exception e) {
            LOG.error("Failed to generate AES key: " + e.getMessage());
            return null;
        }
    }
}