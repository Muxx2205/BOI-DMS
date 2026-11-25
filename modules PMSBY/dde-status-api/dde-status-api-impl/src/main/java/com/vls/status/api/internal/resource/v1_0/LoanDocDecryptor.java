package com.vls.status.api.internal.resource.v1_0;

import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class LoanDocDecryptor {

	public static PrivateKey loadPrivateKey(String pfxFilePath, String password) throws Exception {
        KeyStore ks = KeyStore.getInstance("PKCS12");
        try (FileInputStream fis = new FileInputStream(pfxFilePath)) {
            ks.load(fis, password.toCharArray());
        }
        String alias = ks.aliases().nextElement();
        return (PrivateKey) ks.getKey(alias, password.toCharArray());
    }
    
	public static byte[] decryptSessionKey(String encryptedSessionKeyBase64, PrivateKey privateKey) throws Exception {
	    byte[] encryptedBytes = Base64.getDecoder().decode(encryptedSessionKeyBase64);

	    Cipher rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
	    rsaCipher.init(Cipher.DECRYPT_MODE, privateKey);

	    byte[] decryptedBytes = rsaCipher.doFinal(encryptedBytes);

	    String aesKeyBase64 = new String(decryptedBytes, StandardCharsets.UTF_8).trim();
	    return Base64.getDecoder().decode(aesKeyBase64);
	}

	
	public static byte[] decryptCipherText(String cipherTextBase64, byte[] aesKeyBytes) throws Exception {
	    byte[] encryptedBytes = Base64.getDecoder().decode(cipherTextBase64);

	    SecretKeySpec aesKey = new SecretKeySpec(aesKeyBytes, "AES");
	    Cipher aesCipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
	    aesCipher.init(Cipher.DECRYPT_MODE, aesKey);

	    return aesCipher.doFinal(encryptedBytes);
	}
}