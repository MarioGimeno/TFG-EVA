
// 4) ContactsAdapter.java
package com.example.appGrabacion.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appGrabacion.R;
import com.example.appGrabacion.models.ContactEntry;

import java.util.ArrayList;
import java.util.List;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.VH> {
    private List<ContactEntry> contacts = new ArrayList<>();

    public void setContacts(List<ContactEntry> list) {
        contacts = list;
        notifyDataSetChanged();
    }

    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_contact, parent, false);
        return new VH(itemView);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        ContactEntry c = contacts.get(pos);
        h.tvName.setText(c.getName());
        h.tvPhone.setText(c.getEmail());
    }

    @Override public int getItemCount() { return contacts.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvPhone;
        VH(View v) {
            super(v);
            tvName = v.findViewById(R.id.tvContactName);
            tvPhone= v.findViewById(R.id.tvContactPhone);
        }
    }
}

