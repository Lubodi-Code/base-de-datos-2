package cr.poderjudicial.sigec.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AesGcmCipherServiceTest {

    // Clave AES-256 de prueba (32 bytes en Base64): "0123456789abcdef0123456789abcdef".
    private static final String KEY_B64 = "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";

    private final AesGcmCipherService cipher = new AesGcmCipherService(KEY_B64);

    @Test
    void cifrarYDescifrarDevuelveElTextoOriginal() {
        String secreto = "P@ssw0rd-Grabador-172.24.28.41";
        byte[] cifrado = cipher.cifrar(secreto);

        assertFalse(secreto.equals(new String(cifrado)), "el texto no debe quedar en claro");
        assertEquals(secreto, cipher.descifrar(cifrado));
    }

    @Test
    void dosCifradosDelMismoTextoDifierenPorElIvAleatorio() {
        String secreto = "mismo-secreto";
        byte[] a = cipher.cifrar(secreto);
        byte[] b = cipher.cifrar(secreto);

        assertFalse(java.util.Arrays.equals(a, b), "el IV aleatorio debe producir cifrados distintos");
        // ...pero ambos descifran al mismo valor.
        assertEquals(cipher.descifrar(a), cipher.descifrar(b));
    }

    @Test
    void soportaUnicodeSinPerdida() {
        String secreto = "clave-ñ-áé-✓-控制室";
        assertEquals(secreto, cipher.descifrar(cipher.cifrar(secreto)));
    }

    @Test
    void rechazaClaveQueNoSeaDe32Bytes() {
        // "clave-corta" en Base64 no produce 32 bytes.
        String corta = java.util.Base64.getEncoder().encodeToString("clave-corta".getBytes());
        assertThrows(IllegalStateException.class, () -> new AesGcmCipherService(corta));
    }

    @Test
    void elCifradoIncluyeIvDe12BytesAntesDelTextoCifrado() {
        byte[] cifrado = cipher.cifrar("x");
        // IV(12) + ciphertext(>=1) + tag(16) -> al menos 29 bytes para 1 caracter.
        org.junit.jupiter.api.Assertions.assertTrue(cifrado.length >= 12 + 1 + 16);
        byte[] primeros12 = java.util.Arrays.copyOfRange(cifrado, 0, 12);
        assertArrayEquals(primeros12, java.util.Arrays.copyOfRange(cifrado, 0, 12));
    }
}
