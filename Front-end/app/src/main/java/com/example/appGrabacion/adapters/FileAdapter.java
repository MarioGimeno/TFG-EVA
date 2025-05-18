package com.example.appGrabacion.adapters;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.webkit.MimeTypeMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appGrabacion.R;
import com.example.appGrabacion.models.FileEntry;
import com.example.appGrabacion.screens.ImageViewerActivity;
import com.example.appGrabacion.screens.PdfViewerActivity;
import com.example.appGrabacion.screens.TextViewerActivity;
import com.example.appGrabacion.screens.VideoPlayerActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.VH> {
    private final List<FileEntry> entries;
    private final Context ctx;

    public FileAdapter(Context ctx, List<FileEntry> entries) {
        this.ctx = ctx;
        this.entries = entries;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(ctx)
                .inflate(R.layout.item_file, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int pos) {
        FileEntry e    = entries.get(pos);
        String name    = e.getName();
        String lower   = name.toLowerCase(Locale.ROOT);
        String url     = e.getUrl();
        String created = e.getCreated();
        long   size    = e.getSize();

        // 1) Icono según tipo
        int iconRes;
        if (lower.endsWith(".mp4") || lower.endsWith(".mkv")) {
            iconRes = R.drawable.ic_video;
        } else if (lower.matches(".*\\.(jpg|jpeg|png|gif)$")) {
            iconRes = R.drawable.ic_imagen;
        } else if (lower.endsWith(".pdf")) {
            iconRes = R.drawable.ic_pdf;
        } else if (lower.endsWith(".txt") || lower.endsWith(".md")) {
            iconRes = R.drawable.ic_txt;
        } else {
            iconRes = R.drawable.ic_file;
        }
        holder.imgPreview.setImageResource(iconRes);

        // 2) Nombre
        holder.tvName.setText(name);

        // 3) Tamaño y fecha en TextViews separados
        holder.tvSize.setText(formatSize(size));
        holder.tvDate.setText(formatDate(created));

        // 4) Click en el icono para abrir la vista adecuada
        holder.imgPreview.setOnClickListener(v -> {
            Intent i;
            if (lower.endsWith(".mp4") || lower.endsWith(".mkv")) {
                i = new Intent(ctx, VideoPlayerActivity.class);
            } else if (lower.matches(".*\\.(jpg|jpeg|png|gif)$")) {
                i = new Intent(ctx, ImageViewerActivity.class);
            } else if (lower.endsWith(".pdf")) {
                i = new Intent(ctx, PdfViewerActivity.class);
            } else if (lower.endsWith(".txt") || lower.endsWith(".md")) {
                i = new Intent(ctx, TextViewerActivity.class);
            } else {
                i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            }
            i.putExtra("url", url);
            ctx.startActivity(i);
        });

        // 5) Click en descarga
        holder.btnDownload.setOnClickListener(v ->
                downloadFile(url, name)
        );
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    private void downloadFile(String fileUrl, String fileName) {
        try {
            String ext  = fileName.substring(fileName.lastIndexOf('.') + 1);
            String mime = MimeTypeMap.getSingleton()
                    .getMimeTypeFromExtension(ext);

            DownloadManager.Request req = new DownloadManager.Request(
                    Uri.parse(fileUrl)
            );
            req.setTitle(fileName);
            req.setDescription("Descargando " + fileName);
            req.allowScanningByMediaScanner();
            if (mime != null) req.setMimeType(mime);
            req.setNotificationVisibility(
                    DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
            );
            req.setAllowedOverMetered(true);
            req.setAllowedOverRoaming(false);

            DownloadManager dm =
                    (DownloadManager) ctx.getSystemService(Context.DOWNLOAD_SERVICE);
            dm.enqueue(req);

            Toast.makeText(ctx,
                    "Descarga iniciada: " + fileName,
                    Toast.LENGTH_SHORT).show();
        } catch (Exception ex) {
            Toast.makeText(ctx,
                    "Error al descargar: " + ex.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    // Helper: human-readable tamaño
    private String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        char pre = "KMGTPE".charAt(exp - 1);
        return String.format(Locale.getDefault(), "%.1f %sB",
                bytes / Math.pow(1024, exp), pre);
    }

    // Helper: formatea ISO-8601 a dd/MM/yyyy HH:mm
    private String formatDate(String isoDate) {
        try {
            // 1) Parseador en UTC
            SimpleDateFormat in = new SimpleDateFormat(
                    "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            in.setTimeZone(TimeZone.getTimeZone("UTC"));

            // 2) Formateador en zona local
            SimpleDateFormat out = new SimpleDateFormat(
                    "dd/MM/yyyy HH:mm", Locale.getDefault());
            out.setTimeZone(TimeZone.getDefault());

            // 3) Parse y formatea
            Date d = in.parse(isoDate);
            return out.format(d);
        } catch (ParseException ex) {
            return isoDate;
        }
    }


    static class VH extends RecyclerView.ViewHolder {
        ImageView   imgPreview;
        TextView    tvName, tvSize, tvDate;
        ImageButton btnDownload;

        VH(View item) {
            super(item);
            imgPreview  = item.findViewById(R.id.imgPreview);
            tvName      = item.findViewById(R.id.tvName);
            tvSize      = item.findViewById(R.id.tvSize);
            tvDate      = item.findViewById(R.id.tvDate);
            btnDownload = item.findViewById(R.id.btnDownload);
        }
    }
}
