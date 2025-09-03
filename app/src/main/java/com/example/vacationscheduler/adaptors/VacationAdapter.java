package com.example.vacationscheduler.adaptors;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vacationscheduler.entities.Vacation;

import java.util.List;

public class VacationAdapter extends RecyclerView.Adapter<VacationAdapter.VacationViewHolder> {
    private List<Vacation> vacations;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Vacation vacation);
    }

    public VacationAdapter(List<Vacation> vacations, OnItemClickListener listener) {
        this.vacations = vacations;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VacationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        return new VacationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VacationViewHolder holder, int position) {
        Vacation vacation = vacations.get(position);
        holder.textView.setText(vacation.getTitle());
        holder.itemView.setOnClickListener(v -> listener.onItemClick(vacation));
    }

    @Override
    public int getItemCount() {
        return vacations.size();
    }

    public void setVacations(List<Vacation> vacations) {
        this.vacations = vacations;
        notifyDataSetChanged();
    }

    static class VacationViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        VacationViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(android.R.id.text1);
        }
    }
}