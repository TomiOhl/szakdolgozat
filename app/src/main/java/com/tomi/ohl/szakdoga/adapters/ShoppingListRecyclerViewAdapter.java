package com.tomi.ohl.szakdoga.adapters;

import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tomi.ohl.szakdoga.R;
import com.tomi.ohl.szakdoga.models.ShoppingListItem;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.Set;

public class ShoppingListRecyclerViewAdapter extends RecyclerView.Adapter<ShoppingListViewHolder> {
    private LinkedHashMap<String, ShoppingListItem> items;
    private ArrayList<String> keys;

    public ShoppingListRecyclerViewAdapter(LinkedHashMap<String, ShoppingListItem> list) {
        items = list;
        keys = new ArrayList<>(items.keySet());
    }

    @NonNull
    @Override
    public ShoppingListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.shopping_list_item, parent, false);
        return new ShoppingListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShoppingListViewHolder holder, int position) {
        String key = keys.get(position);
        boolean isChecked = Objects.requireNonNull(items.get(key)).isChecked();
        String name = Objects.requireNonNull(items.get(key)).getName();
        holder.getShoppingCheck().setChecked(isChecked);
        holder.getShoppingName().setText(name);
        // Az utolsó elem kap egy nagy paddinget a FAB miatt
        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) holder.itemView.getLayoutParams();
        if (position == getItemCount() -1) {
            DisplayMetrics displayMetrics = holder.getShoppingItem().getResources().getDisplayMetrics();
            params.bottomMargin = (int) ((88 * displayMetrics.density) + 0.5);
        }
        else
            params.bottomMargin = 0;
        holder.itemView.setLayoutParams(params);
    }

    @Override
    public int getItemCount() {
        return keys.size();
    }

    public void updateKeys(Set<String> newKeys) {
        keys.clear();
        keys.addAll(new ArrayList<>(newKeys));
        notifyDataSetChanged();
    }
}