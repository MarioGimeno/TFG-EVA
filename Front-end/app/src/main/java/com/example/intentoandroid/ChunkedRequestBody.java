package com.example.intentoandroid;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;

public class ChunkedRequestBody extends RequestBody {
    private File file;
    private long offset;
    private long length;
    private MediaType contentType;

    public ChunkedRequestBody(File file, long offset, long length, MediaType contentType) {
        this.file = file;
        this.offset = offset;
        this.length = length;
        this.contentType = contentType;
    }

    @Override
    public MediaType contentType() {
        return contentType;
    }

    @Override
    public long contentLength() {
        return length;
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        // Usamos RandomAccessFile para leer desde un offset específico
        RandomAccessFile raf = new RandomAccessFile(file, "r");
        try {
            raf.seek(offset);
            byte[] buffer = new byte[8192]; // buffer de 8 KB
            long remaining = length;
            int read;
            while (remaining > 0 && (read = raf.read(buffer, 0, (int) Math.min(buffer.length, remaining))) != -1) {
                sink.write(buffer, 0, read);
                remaining -= read;
            }
        } finally {
            raf.close();
        }
    }
}
