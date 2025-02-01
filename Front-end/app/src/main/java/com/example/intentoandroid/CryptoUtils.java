package com.example.intentoandroid;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CryptoUtils {
    private static final String TAG = "CryptoUtils";
    // Cambiamos la transformación a AES/GCM/NoPadding
    private static final String AES_TRANSFORMATION = "AES/GCM/NoPadding";
    private static final String AES_ALGORITHM = "AES";
    // Para GCM se recomienda un IV de 12 bytes
    private static final int IV_SIZE = 12;
    // Longitud del tag en bits (16 bytes = 128 bits)
    private static final int TAG_LENGTH_BIT = 128;
    private static final String SECRET_KEY = "1234567890123456"; // Debe ser de 16 bytes para AES-128


    public static File encryptFile(File inputFile, File outputFile) throws Exception {
        Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
        SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes("UTF-8"), AES_ALGORITHM);
        byte[] iv = new byte[IV_SIZE];
        new SecureRandom().nextBytes(iv);
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmParameterSpec);

        try (FileChannel inChannel = new FileInputStream(inputFile).getChannel();
             FileChannel outChannel = new FileOutputStream(outputFile).getChannel()) {

            // Escribir el IV al inicio del archivo de salida
            ByteBuffer ivBuffer = ByteBuffer.wrap(iv);
            outChannel.write(ivBuffer);

            long fileSize = inChannel.size();
            long chunkSize = 1024 * 1024; // 1 MB por chunk (ajusta según convenga)
            long position = 0;
            while (position < fileSize) {
                long remaining = fileSize - position;
                long mapSize = Math.min(chunkSize, remaining);
                // Mapea el siguiente chunk del archivo
                MappedByteBuffer buffer = inChannel.map(FileChannel.MapMode.READ_ONLY, position, mapSize);
                byte[] inputChunk = new byte[(int) mapSize];
                buffer.get(inputChunk);
                byte[] outputChunk = cipher.update(inputChunk);
                if (outputChunk != null) {
                    outChannel.write(ByteBuffer.wrap(outputChunk));
                }
                position += mapSize;
            }
            // Procesar el bloque final
            byte[] finalBytes = cipher.doFinal();
            if (finalBytes != null) {
                outChannel.write(ByteBuffer.wrap(finalBytes));
            }
        }
        return outputFile;
    }

    // Método auxiliar para convertir bytes a hexadecimal (para depuración)
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }
}
