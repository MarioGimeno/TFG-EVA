package com.example.appGrabacion.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appGrabacion.R;
import com.example.appGrabacion.models.Recurso;
import com.example.appGrabacion.screens.RecursoDetailActivity;
import com.squareup.picasso.Picasso;

public class RecursosDetailAdapter
        extends ListAdapter<Recurso, RecursosDetailAdapter.RecursoViewHolder> {

    private final Context context;

    public RecursosDetailAdapter(Context context) {
        super(DIFF_CALLBACK);
        this.context = context;
    }

    private static final DiffUtil.ItemCallback<Recurso> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Recurso>() {
                @Override public boolean areItemsTheSame(@NonNull Recurso o1, @NonNull Recurso o2) {
                    return o1.getId() == o2.getId();
                }
                @Override public boolean areContentsTheSame(@NonNull Recurso o1, @NonNull Recurso o2) {
                    return o1.equals(o2);
                }
            };

    @NonNull @Override
    public RecursoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_slider_recurso, parent, false);
        return new RecursoViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecursoViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class RecursoViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imgRecurso;
        RecursoViewHolder(@NonNull View itemView) {
            super(itemView);
            imgRecurso = itemView.findViewById(R.id.imgSliderRecurso);
        }
        void bind(final Recurso rec) {
            if (rec.getImagen() != null && !rec.getImagen().isEmpty()) {
                Picasso.get()
                        .load(rec.getImagen())
                        .error(R.drawable.eva)
                        .into(imgRecurso);
            } else {
                imgRecurso.setImageResource(R.drawable.eva);
            }
            imgRecurso.setOnClickListener(v -> {
                Intent intent = new Intent(context, RecursoDetailActivity.class);
                intent.putExtra("id_recurso", rec.getId());
                context.startActivity(intent);
            });
        }
    }
}
