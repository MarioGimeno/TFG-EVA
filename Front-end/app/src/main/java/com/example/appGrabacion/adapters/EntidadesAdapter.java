package com.example.appGrabacion.adapters;

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
import com.example.appGrabacion.models.Entidad;
import com.squareup.picasso.Picasso;

public class EntidadesAdapter extends ListAdapter<Entidad, EntidadesAdapter.EntidadViewHolder> {

    public EntidadesAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<Entidad> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Entidad>() {
                @Override
                public boolean areItemsTheSame(@NonNull Entidad oldItem, @NonNull Entidad newItem) {
                    return oldItem.getIdEntidad() == newItem.getIdEntidad();
                }

                @Override
                public boolean areContentsTheSame(@NonNull Entidad oldItem, @NonNull Entidad newItem) {
                    return oldItem.equals(newItem);
                }
            };

    @NonNull
    @Override
    public EntidadViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_entidad, parent, false);
        return new EntidadViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EntidadViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    static class EntidadViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imgEntidad;
        private final TextView tvEmail, tvDireccion, tvTelefono;

        EntidadViewHolder(@NonNull View itemView) {
            super(itemView);
            imgEntidad    = itemView.findViewById(R.id.imgEntidad);
            tvEmail       = itemView.findViewById(R.id.tvEmailEntidad);
            tvDireccion   = itemView.findViewById(R.id.tvDireccionEntidad);
            tvTelefono    = itemView.findViewById(R.id.tvTelefonoEntidad);
        }

        void bind(Entidad ent) {
            tvEmail.setText(ent.getEmail());
            tvDireccion.setText(ent.getDireccion());
            tvTelefono.setText(ent.getTelefono());
            // Carga imagen con Picasso (o Glide)
            if (ent.getImagen() != null && !ent.getImagen().isEmpty()) {
                Picasso.get()
                        .load(ent.getImagen())
                        .placeholder(R.drawable.eva)
                        .error(R.drawable.eva)
                        .into(imgEntidad);
            } else {
                imgEntidad.setImageResource(R.drawable.eva);
            }
        }
    }
}
