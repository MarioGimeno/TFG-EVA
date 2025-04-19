package com.example.appGrabacion.adapters;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
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
import com.example.appGrabacion.screens.ImageViewerActivity;
import com.example.appGrabacion.screens.PdfViewerActivity;
import com.example.appGrabacion.screens.TextViewerActivity;
import com.example.appGrabacion.screens.VideoPlayerActivity;

import java.util.List;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.VH> {
    private final List<String> urls;
    private final Context ctx;
    private final Drawable txtIcon;

    public FileAdapter(Context ctx, List<String> urls) {
        this.ctx = ctx;
        this.urls = urls;
        txtIcon = ctx.getDrawable(R.drawable.ic_txt);
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(ctx).inflate(R.layout.item_file, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        String url = urls.get(position);
        String name = url.substring(url.lastIndexOf('/') + 1).toLowerCase();
        holder.tvName.setText(name);

        // Preview according to file type
        if (name.endsWith(".mp4")) {
            Glide.with(ctx)
                    .asBitmap()
                    .load(url)
                    .frame(1_000_000)
                    .into(holder.imgPreview);
            holder.btnType.setImageResource(R.drawable.ic_play_circle);

        } else if (name.endsWith(".jpg") || name.endsWith(".png")) {
            Glide.with(ctx).load(url).into(holder.imgPreview);
            holder.btnType.setImageResource(R.drawable.ic_img);

        } else if (name.endsWith(".pdf")) {
            holder.imgPreview.setImageResource(R.drawable.ic_file);
            holder.btnType.setImageResource(R.drawable.ic_file);

        } else if (name.endsWith(".txt")) {
            holder.imgPreview.setImageDrawable(txtIcon);
            holder.btnType.setImageDrawable(txtIcon);

        } else {
            holder.imgPreview.setImageResource(R.drawable.ic_file);
            holder.btnType.setImageResource(R.drawable.ic_file);
        }

        // Click listener for the rest of the item -> open appropriate viewer
        holder.itemView.setOnClickListener(v -> {
            Intent intent;
            if (name.endsWith(".mp4")) {
                intent = new Intent(ctx, VideoPlayerActivity.class);
                intent.putExtra("url", url);

            } else if (name.endsWith(".txt")) {
                intent = new Intent(ctx, TextViewerActivity.class);
                intent.putExtra("url", url);

            } else if (name.matches(".*\\.(jpg|png)$")) {
                intent = new Intent(ctx, ImageViewerActivity.class);
                intent.putExtra("url", url);

            } else if (name.endsWith(".pdf")) {
                intent = new Intent(ctx, PdfViewerActivity.class);
                intent.putExtra("url", url);

            } else {
                // Fallback: open in browser
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            }
            ctx.startActivity(intent);
        });

        // Click listener for download button -> download file
        holder.btnDownload.setOnClickListener(v -> downloadFile(url, name));
    }

    @Override
    public int getItemCount() {
        return urls.size();
    }

    /**
     * Uses DownloadManager to download the file into the public Downloads folder.
     */
    private void downloadFile(String fileUrl, String fileName) {
        try {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(fileUrl))
                    .setTitle(fileName)
                    .setDescription("Descargando " + fileName)
                    .setNotificationVisibility(
                            DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
                    )
                    .setAllowedOverMetered(true)
                    .setAllowedOverRoaming(false)
                    .setDestinationInExternalPublicDir(
                            Environment.DIRECTORY_DOWNLOADS,
                            fileName
                    );

            DownloadManager dm = (DownloadManager) ctx.getSystemService(Context.DOWNLOAD_SERVICE);
            dm.enqueue(request);
            Toast.makeText(ctx, "Descarga iniciada: " + fileName, Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(ctx, "Error al descargar: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView imgPreview;
        ImageButton btnType;
        ImageButton btnDownload;
        TextView  tvName;

        VH(View item) {
            super(item);
            imgPreview = item.findViewById(R.id.imgPreview);
            btnType    = item.findViewById(R.id.btnType);
            btnDownload= item.findViewById(R.id.btnDownload); // nuevo botón
            tvName     = item.findViewById(R.id.tvName);
        }
    }
}
