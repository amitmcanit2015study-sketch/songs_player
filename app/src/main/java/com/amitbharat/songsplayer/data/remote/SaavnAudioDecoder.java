package com.amitbharat.songsplayer.data.remote;

import android.util.Base64;

import java.nio.charset.StandardCharsets;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

/**
 * High Performance Stream Decryptor for 320kbps Master Audio Streams.
 */
public class SaavnAudioDecoder {

    private static final String DES_KEY = "38346591";

    public static String decryptMediaUrl(String encryptedUrl) {
        if (encryptedUrl == null || encryptedUrl.trim().isEmpty()) {
            return null;
        }
        try {
            byte[] keyBytes = DES_KEY.getBytes(StandardCharsets.UTF_8);
            DESKeySpec keySpec = new DESKeySpec(keyBytes);
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey key = keyFactory.generateSecret(keySpec);

            Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, key);

            byte[] decoded = Base64.decode(encryptedUrl.trim(), Base64.DEFAULT);
            byte[] decrypted = cipher.doFinal(decoded);
            String url = new String(decrypted, StandardCharsets.UTF_8);

            // Enhance to 320kbps Master Audio Stream
            if (url.endsWith("_96.mp4")) {
                return url.replace("_96.mp4", "_320.mp4");
            } else if (url.endsWith("_160.mp4")) {
                return url.replace("_160.mp4", "_320.mp4");
            }
            return url;
        } catch (Exception e) {
            return null;
        }
    }
}
