package com.example.appGrabacion.encriptación;

import java.io.*;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CryptoUtils {

    // Parámetros para AES/GCM/NoPadding
    private static final String AES_TRANSFORMATION = "AES/GCM/NoPadding";
    private static final String AES_ALGORITHM = "AES";
    private static final int IV_SIZE = 12;               // 12 bytes para GCM
    private static final int TAG_LENGTH_BIT = 128;       // Tag de 128 bits (16 bytes)
    private static final String SECRET_KEY = "1234567890123456"; // Clave de 16 bytes para AES-128

    // Umbral en bytes para elegir el método: aquí 16 MB
    private static final long THRESHOLD = 16L * 1024 * 1024;

    // Magic header para identificar archivos encriptados por chunks
    private static final byte[] MAGIC;

    static {
        try {
            MAGIC = "CHNK".getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Encripta un archivo usando AES/GCM/NoPadding.
     * Si el archivo es pequeño (<= THRESHOLD) usa streaming,
     * y si es muy grande usa chunked encryption (con header "CHNK").
     *
     * @param inputFile  archivo de entrada a encriptar
     * @param outputFile archivo de salida encriptado
     * @return el archivo de salida
     * @throws Exception en caso de error
     */
    public static File encryptFileFlexible(File inputFile, File outputFile) throws Exception {
        if (inputFile.length() <= THRESHOLD) {
            // Método de streaming (para archivos pequeños/medianos)
            Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
            SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes("UTF-8"), AES_ALGORITHM);
            byte[] iv = new byte[IV_SIZE];
            new SecureRandom().nextBytes(iv);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);

            try (FileInputStream fis = new FileInputStream(inputFile);
                 FileOutputStream fos = new FileOutputStream(outputFile)) {

                // Escribir el IV al inicio del archivo para usarlo en la desencriptación
                fos.write(iv);

                // Encriptar y escribir los datos en bloques pequeños
                try (CipherOutputStream cos = new CipherOutputStream(fos, cipher)) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        cos.write(buffer, 0, bytesRead);
                    }
                }
            }
        } else {
            // Método chunked (para archivos muy grandes)
            SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes("UTF-8"), AES_ALGORITHM);
            SecureRandom random = new SecureRandom();
            int chunkSize = (int) THRESHOLD; // Usamos el umbral como tamaño de chunk

            try (FileInputStream fis = new FileInputStream(inputFile);
                 DataOutputStream dos = new DataOutputStream(new FileOutputStream(outputFile))) {

                // Escribir el header: Magic header "CHNK" y luego el tamaño de chunk
                dos.write(MAGIC);               // Escribe 4 bytes ("CHNK")
                dos.writeInt(chunkSize);         // Escribe el tamaño de chunk

                byte[] buffer = new byte[chunkSize];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    // Para cada chunk se genera un nuevo IV
                    byte[] iv = new byte[IV_SIZE];
                    random.nextBytes(iv);
                    GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
                    Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
                    cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);

                    // Encriptar el chunk
                    byte[] encryptedChunk = cipher.doFinal(buffer, 0, bytesRead);

                    // Escribir el IV: primero la longitud del IV, luego el IV
                    dos.writeInt(iv.length);
                    dos.write(iv);
                    // Escribir el chunk encriptado: primero la longitud, luego los datos
                    dos.writeInt(encryptedChunk.length);
                    dos.write(encryptedChunk);
                }
            }
        }
        return outputFile;
    }
}
