package com.example.appGrabacion.adapters;

import android.util.Log;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appGrabacion.R;
import com.example.appGrabacion.models.Recurso;
import com.squareup.picasso.Picasso;

public class CategoriasRecursosAdapter
        extends ListAdapter<Recurso, CategoriasRecursosAdapter.ViewHolder> {

    public CategoriasRecursosAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<Recurso> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Recurso>() {
                @Override
                public boolean areItemsTheSame(@NonNull Recurso o1, @NonNull Recurso o2) {
                    return o1.getId() == o2.getId();
                }
                @Override
                public boolean areContentsTheSame(@NonNull Recurso o1, @NonNull Recurso o2) {
                    return o1.equals(o2);
                }
            };

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recurso, parent, false);
        return new ViewHolder(v);
    }



    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Recurso r = getItem(position);
        Log.d("CRECAdapter", "URL imagen: " + r.getImagen());

        holder.tvServicio.setText(r.getServicio());
        holder.tvDireccion.setText(r.getDireccion());
        holder.tvTelefono.setText(r.getTelefono());

        if (r.getImagen() != null && !r.getImagen().isEmpty()) {
            Picasso.get()
                    .load(r.getImagen())
                    .placeholder(R.drawable.eva)
                    .error(R.drawable.eva)
                    .into(holder.imgRecurso);
        } else {
            holder.imgRecurso.setImageResource(R.drawable.eva);
        }

    }



    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgRecurso;
        TextView tvServicio, tvDireccion, tvTelefono;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgRecurso   = itemView.findViewById(R.id.imgRecurso);
            tvServicio   = itemView.findViewById(R.id.tvServicioRecurso);
            tvDireccion  = itemView.findViewById(R.id.tvDireccionRecurso);
            tvTelefono   = itemView.findViewById(R.id.tvTelefonoRecurso);
        }
    }
}