// com/example/appGrabacion/screens/EntidadesAdapter.java
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
import com.example.appGrabacion.entities.Entidad;
import com.squareup.picasso.Picasso;

public class EntidadesAdapter
        extends ListAdapter<Entidad, EntidadesAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Entidad entidad);
    }

    private final OnItemClickListener listener;

    public EntidadesAdapter(OnItemClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Entidad> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Entidad>() {
                @Override public boolean areItemsTheSame(@NonNull Entidad o1, @NonNull Entidad o2) {
                    return o1.getIdEntidad()==o2.getIdEntidad();
                }
                @Override public boolean areContentsTheSame(@NonNull Entidad o1, @NonNull Entidad o2) {
                    return o1.equals(o2);
                }
            };

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup p, int i) {
        View v = LayoutInflater.from(p.getContext())
                .inflate(R.layout.item_entidad, p, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder vh, int pos) {
        Entidad e = getItem(pos);
        vh.bind(e, listener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView img; TextView tvEmail, tvDir, tvTel;
        ViewHolder(@NonNull View v) {
            super(v);
            img      = v.findViewById(R.id.imgEntidad);
            tvEmail  = v.findViewById(R.id.tvEmailEntidad);
            tvDir    = v.findViewById(R.id.tvDireccionEntidad);
            tvTel    = v.findViewById(R.id.tvTelefonoEntidad);
        }
        void bind(final Entidad e, final OnItemClickListener l) {
            tvEmail.setText(e.getEmail());
            tvDir  .setText(e.getDireccion());
            tvTel  .setText(e.getTelefono());
            if (e.getImagen()!=null && !e.getImagen().isEmpty()) {
                Picasso.get()
                        .load(e.getImagen())
                        .error(R.drawable.eva)
                        .into(img);
            } else img.setImageResource(R.drawable.eva);

            itemView.setOnClickListener(v -> l.onItemClick(e));
        }
    }
}
