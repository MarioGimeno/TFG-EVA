// src/main/java/com/example/appGrabacion/adapters/ContactsAdapter.java
package com.example.appGrabacion.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appGrabacion.R;
import com.example.appGrabacion.entities.ContactEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter para la lista de contactos, con callback de borrado.
 */
public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.VH> {
    private List<ContactEntry> contacts = new ArrayList<>();
    private final OnDeleteClickListener deleteListener;

    /** Interfaz para notificar borrado al Activity */
    public interface OnDeleteClickListener {
        void onDeleteClick(int contactId);
    }

    /** Recibe un listener que maneja el borrado */
    public ContactsAdapter(OnDeleteClickListener deleteListener) {
        this.deleteListener = deleteListener;
    }

    /** Actualiza la lista y refresca */
    public void setContacts(List<ContactEntry> list) {
        contacts = list;
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_contact, parent, false);
        return new VH(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        ContactEntry c = contacts.get(pos);
        h.tvName.setText(c.getName());
        h.tvPhone.setText(c.getEmail());

        // Botón de borrar
        h.btnDelete.setOnClickListener(v ->
                deleteListener.onDeleteClick(c.getId())
        );
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    /** ViewHolder con el btnDelete ya enlazado */
    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvPhone;
        ImageButton btnDelete;

        VH(View v) {
            super(v);
            tvName    = v.findViewById(R.id.tvContactName);
            tvPhone   = v.findViewById(R.id.tvContactPhone);
            btnDelete = v.findViewById(R.id.btnDelete);
        }
    }
}
