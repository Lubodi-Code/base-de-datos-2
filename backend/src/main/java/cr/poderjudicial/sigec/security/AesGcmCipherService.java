package cr.poderjudicial.sigec.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

/**
 * Cifrado autenticado AES-256-GCM para los secretos de equipos (ISO/IEC 27001).
 * Sustituye las credenciales en texto plano del archivo ODS original.
 * Formato persistido: IV(12 bytes) || ciphertext || tag(16 bytes).
 */
@Service
public class AesGcmCipherService {

    private static final int IV_LEN = 12;     // 96 bits, recomendado para GCM
    private static final int TAG_BITS = 128;

    private final SecretKeySpec key;
    private final SecureRandom random = new SecureRandom();

    public AesGcmCipherService(@Value("${sigec.security.aes.key-base64}") String keyB64) {
        byte[] raw = Base64.getDecoder().decode(keyB64);
        if (raw.length != 32) {
            throw new IllegalStateException("La clave AES debe ser de 32 bytes (AES-256).");
        }
        this.key = new SecretKeySpec(raw, "AES");
    }

    public byte[] cifrar(String textoPlano) {
        try {
            byte[] iv = new byte[IV_LEN];
            random.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
            byte[] ct = cipher.doFinal(textoPlano.getBytes(StandardCharsets.UTF_8));
            byte[] out = new byte[iv.length + ct.length];
            System.arraycopy(iv, 0, out, 0, iv.length);
            System.arraycopy(ct, 0, out, iv.length, ct.length);
            return out;
        } catch (Exception e) {
            throw new IllegalStateException("Error al cifrar el secreto", e);
        }
    }

    public String descifrar(byte[] datos) {
        try {
            byte[] iv = Arrays.copyOfRange(datos, 0, IV_LEN);
            byte[] ct = Arrays.copyOfRange(datos, IV_LEN, datos.length);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
            return new String(cipher.doFinal(ct), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Error al descifrar el secreto", e);
        }
    }
}
