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
import com.example.appGrabacion.models.Categoria;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class CategoriasAdapter
        extends ListAdapter<Categoria, CategoriasAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Categoria categoria);
    }

    private final OnItemClickListener listener;

    public CategoriasAdapter(OnItemClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Categoria> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Categoria>() {
                @Override
                public boolean areItemsTheSame(@NonNull Categoria o1, @NonNull Categoria o2) {
                    return o1.getIdCategoria() == o2.getIdCategoria();
                }
                @Override
                public boolean areContentsTheSame(@NonNull Categoria o1, @NonNull Categoria o2) {
                    return o1.equals(o2);
                }
            };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_categoria, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder vh, int pos) {
        vh.bind(getItem(pos), listener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private static final Map<Integer,Integer> ICONS = new HashMap<>();
        static {
            ICONS.put(1, R.drawable.ic_juridicos);
            ICONS.put(2, R.drawable.ic_laboral);
            ICONS.put(3, R.drawable.ic_subsidio);
            ICONS.put(4, R.drawable.ic_emergencia);
            ICONS.put(5, R.drawable.ic_informacion);
            ICONS.put(6, R.drawable.ic_especializados);
            ICONS.put(7, R.drawable.ic_programas);
            ICONS.put(8, R.drawable.ic_discapacidad);
            // añade aquí hasta el último id_categoria que necesites…
        }

        ImageView img;
        TextView tvNombre;

        ViewHolder(@NonNull View v) {
            super(v);
            img      = v.findViewById(R.id.imgCategoria);
            tvNombre = v.findViewById(R.id.tvNombreCategoria);
        }

        void bind(final Categoria c, final OnItemClickListener l) {
            tvNombre.setText(c.getNombre());
            Integer res = ICONS.get(c.getIdCategoria());
            if (res != null) {
                img.setImageResource(res);
            } else {
                img.setImageResource(R.drawable.ic_atras);
            }
            itemView.setOnClickListener(v -> l.onItemClick(c));
        }
    }
}
