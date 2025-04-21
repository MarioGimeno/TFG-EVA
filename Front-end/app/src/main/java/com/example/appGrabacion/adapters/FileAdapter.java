package com.example.appGrabacion.adapters;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
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

import com.bumptech.glide.Glide;
import com.example.appGrabacion.R;
import com.example.appGrabacion.models.FileEntry;
import com.example.appGrabacion.screens.ImageViewerActivity;
import com.example.appGrabacion.screens.PdfViewerActivity;
import com.example.appGrabacion.screens.TextViewerActivity;
import com.example.appGrabacion.screens.VideoPlayerActivity;

import java.util.List;

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
        FileEntry e = entries.get(pos);
        String name = e.getName();          // nombre con extensión
        String lower = name.toLowerCase();
        String url  = e.getUrl();

        holder.tvName.setText(name);

        // Preview thumbnail/icon
        if (lower.endsWith(".mp4")) {
            Glide.with(ctx)
                    .asBitmap()
                    .load(url)
                    .frame(1_000_000)
                    .into(holder.imgPreview);
            holder.btnType.setImageResource(R.drawable.ic_play_circle);

        } else if (lower.endsWith(".jpg") || lower.endsWith(".png")) {
            Glide.with(ctx).load(url).into(holder.imgPreview);
            holder.btnType.setImageResource(R.drawable.ic_img);

        } else if (lower.endsWith(".pdf")) {
            holder.imgPreview.setImageResource(R.drawable.ic_file);
            holder.btnType.setImageResource(R.drawable.ic_file);

        } else if (lower.endsWith(".txt")) {
            holder.imgPreview.setImageResource(R.drawable.ic_txt);
            holder.btnType.setImageResource(R.drawable.ic_txt);

        } else {
            holder.imgPreview.setImageResource(R.drawable.ic_file);
            holder.btnType.setImageResource(R.drawable.ic_file);
        }        // Clic en el resto del item: abrir siempre en navegador
        holder.itemView.setOnClickListener(v -> {
            Intent browser = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            ctx.startActivity(browser);
        });

        // Clic en el botón de descarga
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
            String ext = fileName.substring(fileName.lastIndexOf('.') + 1);
            String mime = MimeTypeMap.getSingleton()
                    .getMimeTypeFromExtension(ext);

            // Let the DownloadManager pick the default public Downloads location
            DownloadManager.Request req = new DownloadManager.Request(
                    Uri.parse(fileUrl)
            );
            req.setTitle(fileName);
            req.setDescription("Descargando " + fileName);
            req.allowScanningByMediaScanner();
            if (mime != null) {
                req.setMimeType(mime);
            }
            req.setNotificationVisibility(
                    DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
            );
            req.setAllowedOverMetered(true);
            req.setAllowedOverRoaming(false);
            // Remove custom destination: default Downloads folder will be used

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


    static class VH extends RecyclerView.ViewHolder {
        ImageView   imgPreview;
        ImageButton btnType, btnDownload;
        TextView    tvName;

        VH(View item) {
            super(item);
            imgPreview   = item.findViewById(R.id.imgPreview);
            btnType      = item.findViewById(R.id.btnType);
            btnDownload  = item.findViewById(R.id.btnDownload);
            tvName       = item.findViewById(R.id.tvName);
        }
    }
}
