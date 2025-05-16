package com.example.appGrabacion.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class GenericAdapter<T> extends RecyclerView.Adapter<GenericAdapter.ViewHolder> {
    public interface Binder<T> {
        void bind(View itemView, T item);
    }

    private final @LayoutRes int layoutRes;
    private final List<T> items;
    private final Binder<T> binder;

    public GenericAdapter(@LayoutRes int layoutRes, List<T> items, Binder<T> binder) {
        this.layoutRes = layoutRes;
        this.items = items;
        this.binder = binder;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(layoutRes, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder h, int pos) {
        binder.bind(h.itemView, items.get(pos));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ViewHolder(View v) { super(v); }
    }
}
