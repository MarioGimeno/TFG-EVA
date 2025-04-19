package com.example.appGrabacion.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.appGrabacion.R;
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
        // cargamos el icono de texto
        txtIcon = ctx.getDrawable(R.drawable.button_circle);
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(ctx).inflate(R.layout.item_file, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        String url  = urls.get(pos);
        String name = Uri.parse(url).getLastPathSegment();
        h.tvName.setText(name);

        if (name.toLowerCase().endsWith(".mp4")) {
            // preview de vídeo con Glide
            Glide.with(ctx)
                    .load(url)
                    .thumbnail(0.1f)
                    .into(h.imgPreview);
            h.itemView.setOnClickListener(v -> {
                Intent it = new Intent(ctx, VideoPlayerActivity.class);
                it.putExtra("videoUrl", url);
                ctx.startActivity(it);
            });

        } else if (name.toLowerCase().endsWith(".txt")) {
            // icono de texto
            h.imgPreview.setImageDrawable(txtIcon);
            h.itemView.setOnClickListener(v -> {
                Intent it = new Intent(ctx, TextViewerActivity.class);
                it.putExtra("txtUrl", url);
                ctx.startActivity(it);
            });

        } else {
            // cualquier otro
            h.imgPreview.setImageDrawable(null);
            h.itemView.setOnClickListener(null);
        }
    }

    @Override
    public int getItemCount() {
        return urls.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView imgPreview;
        TextView  tvName;

        VH(View item) {
            super(item);
            imgPreview = item.findViewById(R.id.imgPreview);
            tvName     = item.findViewById(R.id.tvName);
        }
    }
}
