package com.boi.headless.rest.common.util;

import com.liferay.portal.kernel.util.Base64;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.SecureRandom;
import java.util.Scanner;

/**
 * Utility to generate AES-256 key for sharing with CBS team
 * 
 * ⚠️ IMPORTANT: Run this ONLY ONCE to generate the shared key!
 *    Running it again will generate a DIFFERENT key and break existing setup.
 * 
 * How to run:
 *   1. Right-click on this file
 *   2. Run As → Java Application
 *   3. Type 'CONFIRM' when prompted
 *   4. Copy the generated key immediately
 *   5. Save it securely
 *   6. DO NOT run again unless rotating keys
 * 
 * @author Liferay Team
 */
public class GenerateAESKey {
    
    public static void main(String[] args) throws Exception {
        System.out.println();
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║                 AES-256 KEY GENERATION UTILITY              ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        System.out.println();
        
        System.out.println("⚠️  WARNING: This will generate a NEW AES-256 key.");
        System.out.println("   If you already have a key configured in Liferay,");
        System.out.println("   do NOT run this unless you are rotating the key.");
        System.out.println();
        System.out.println("   Running this will generate a DIFFERENT key each time.");
        System.out.println("   The old key will no longer work if you replace it.");
        System.out.println();
        System.out.print("   Type 'CONFIRM' to continue: ");
        
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();
        scanner.close();
        
        if (!"CONFIRM".equalsIgnoreCase(input)) {
            System.out.println();
            System.out.println("❌ Key generation cancelled.");
            System.out.println("   Your existing key remains unchanged.");
            System.out.println();
            return;
        }
        
        System.out.println();
        System.out.println("🔄 Generating AES-256 key...");
        System.out.println();
        
        // Generate AES-256 key
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256, new SecureRandom());
        SecretKey secretKey = keyGen.generateKey();
        
        // Convert to Base64
        String base64Key = Base64.encode(secretKey.getEncoded());
        
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║                  🔑 GENERATED AES-256 KEY                    ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("    " + base64Key);
        System.out.println();
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║                     ENCRYPTION SPECIFICATIONS                ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("  ┌─────────────────────────────────────────────────────────┐");
        System.out.println("  │  Algorithm  : AES/CBC/PKCS5Padding                      │");
        System.out.println("  │  Key Size   : 256 bits                                 │");
        System.out.println("  │  IV Size    : 16 bytes (random, prepended to data)     │");
        System.out.println("  │  Encoding   : Base64                                   │");
        System.out.println("  └─────────────────────────────────────────────────────────┘");
        System.out.println();
        
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║                 ⚠️  IMPORTANT INSTRUCTIONS                   ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("  1️⃣  COPY THIS KEY NOW - It will NOT be shown again!");
        System.out.println("  2️⃣  Store in Liferay System Settings (aesEncryptionKey)");
        System.out.println("  3️⃣  Share securely with CBS team (NOT via regular email)");
        System.out.println("  4️⃣  Do NOT run this generator again unless rotating keys");
        System.out.println("  5️⃣  Save this key in a password manager or secure vault");
        System.out.println();
        
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║                    📋  KEY TO SAVE                           ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("  ┌─────────────────────────────────────────────────────────┐");
        System.out.println("  │  Key: " + base64Key);
        System.out.println("  └─────────────────────────────────────────────────────────┘");
        System.out.println();
        
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║                  ✅  KEY GENERATION COMPLETED                 ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        System.out.println();
    }
}