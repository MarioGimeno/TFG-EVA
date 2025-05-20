// com/example/appGrabacion/adapters/RecursosAdapter.java
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
import com.example.appGrabacion.models.Recurso;
import com.squareup.picasso.Picasso;

public class RecursosAdapter
        extends ListAdapter<Recurso, RecursosAdapter.RecursoViewHolder> {

    /** Listener para clicks sobre un recurso */
    public interface OnItemClickListener {
        void onItemClick(Recurso recurso);
    }

    private final OnItemClickListener listener;

    public RecursosAdapter(OnItemClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Recurso> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Recurso>() {
                @Override
                public boolean areItemsTheSame(@NonNull Recurso oldItem, @NonNull Recurso newItem) {
                    return oldItem.getId() == newItem.getId();
                }
                @Override
                public boolean areContentsTheSame(@NonNull Recurso oldItem, @NonNull Recurso newItem) {
                    return oldItem.equals(newItem);
                }
            };

    @NonNull @Override
    public RecursoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recurso, parent, false);
        return new RecursoViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecursoViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    static class RecursoViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imgRecurso;
        private final TextView tvServicio, tvDireccion, tvTelefono;

        RecursoViewHolder(@NonNull View itemView) {
            super(itemView);
            imgRecurso  = itemView.findViewById(R.id.imgRecurso);
            tvServicio  = itemView.findViewById(R.id.tvServicioRecurso);
            tvDireccion = itemView.findViewById(R.id.tvDireccionRecurso);
            tvTelefono  = itemView.findViewById(R.id.tvTelefonoRecurso);
        }

        void bind(final Recurso rec, final OnItemClickListener l) {
            tvServicio.setText(rec.getServicio());
            tvDireccion.setText(rec.getDireccion());
            tvTelefono.setText(rec.getTelefono());
            if (rec.getImagen() != null && !rec.getImagen().isEmpty()) {
                Picasso.get()
                        .load(rec.getImagen())
                        .error(R.drawable.eva)
                        .into(imgRecurso);
            } else {
                imgRecurso.setImageResource(R.drawable.eva);
            }
            itemView.setOnClickListener(v -> l.onItemClick(rec));
        }
    }
}
