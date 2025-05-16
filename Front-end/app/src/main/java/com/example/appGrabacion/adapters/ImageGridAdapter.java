package com.example.appGrabacion.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appGrabacion.R;
import com.squareup.picasso.Picasso;

public class ImageGridAdapter<T> extends ListAdapter<T, ImageGridAdapter.ViewHolder> {

    public interface Binder<T> {
        /**
         * Llama a Picasso u otro loader:
         * Picasso.get().load(url).into(imageView);
         */
        void bind(ImageView imageView, T item);
    }

    public interface OnItemClickListener<T> {
        void onItemClick(T item);
    }

    private final Binder<T> binder;
    private final OnItemClickListener<T> listener;

    public ImageGridAdapter(
            @NonNull DiffUtil.ItemCallback<T> diffCallback,
            Binder<T> binder,
            OnItemClickListener<T> listener
    ) {
        super(diffCallback);
        this.binder = binder;
        this.listener = listener;
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_image, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        T item = getItem(position);
        binder.bind(holder.image, item);
        holder.image.setOnClickListener(v -> listener.onItemClick(item));
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView image;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.imgGrid);
        }
    }
}
