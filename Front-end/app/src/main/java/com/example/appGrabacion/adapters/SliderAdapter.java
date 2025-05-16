package com.example.appGrabacion.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appGrabacion.R;
import com.example.appGrabacion.models.Entidad;
import com.example.appGrabacion.models.Recurso;
import com.squareup.picasso.Picasso;

import java.util.List;

public class SliderAdapter extends RecyclerView.Adapter<SliderAdapter.VH> {
    public interface Item { String getImageUrl(); }
    private final List<Item> items;
    private final OnClick onClick;
    public interface OnClick { void onItemClick(Item item); }

    public SliderAdapter(List<Item> items, OnClick onClick) {
        this.items = items;
        this.onClick = onClick;
    }

    @Override public int getItemCount() { return items.size(); }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_slider, parent, false);
        // ancho al 70% de pantalla
        int screenW = parent.getContext()
                .getResources()
                .getDisplayMetrics().widthPixels;
        v.getLayoutParams().width = (int)(screenW * 0.7f);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Item it = items.get(position);
        Picasso.get()
                .load(it.getImageUrl())
                .fit()
                .centerCrop()
                .into(holder.img);
        holder.itemView.setOnClickListener(v -> onClick.onItemClick(it));
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView img;
        VH(@NonNull View v) {
            super(v);
            img = v.findViewById(R.id.sliderImage);
        }
    }

    // Adaptadores de Entidad y Recurso
    public static class EntidadItem implements Item {
        private final Entidad e;
        public EntidadItem(Entidad e) { this.e = e; }
        @Override public String getImageUrl() { return e.getImagen(); }
        public Entidad getEntidad() { return e; }
    }
    public static class RecursoItem implements Item {
        private final Recurso r;
        public RecursoItem(Recurso r) { this.r = r; }
        @Override public String getImageUrl() { return r.getImagen(); }
        public Recurso getRecurso() { return r; }
    }
}
