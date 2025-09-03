package com.example.vacationscheduler.adaptors;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vacationscheduler.entities.Excursion;

import java.util.List;

public class ExcursionAdapter extends RecyclerView.Adapter<ExcursionAdapter.ExcursionViewHolder> {
    private List<Excursion> excursions;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Excursion excursion);
    }

    public ExcursionAdapter(List<Excursion> excursions, OnItemClickListener listener) {
        this.excursions = excursions;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ExcursionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ExcursionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExcursionViewHolder holder, int position) {
        Excursion excursion = excursions.get(position);
        holder.textView.setText(excursion.getTitle());
        holder.itemView.setOnClickListener(v -> listener.onItemClick(excursion));
    }

    @Override
    public int getItemCount() {
        return excursions.size();
    }

    public void setExcursions(List<Excursion> excursions) {
        this.excursions = excursions;
        notifyDataSetChanged();
    }

    static class ExcursionViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        ExcursionViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(android.R.id.text1);
        }
    }
}