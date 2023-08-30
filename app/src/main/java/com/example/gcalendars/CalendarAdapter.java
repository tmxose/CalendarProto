package com.example.gcalendars;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarViewHolder> {

    private final ArrayList<String> dayOfMonth;
    private final OnItemListner onItemListner;

    public CalendarAdapter(ArrayList<String> dayOfMonth, CalendarAdapter.OnItemListner onItemListner) {
        this.dayOfMonth = dayOfMonth;
        this.onItemListner = onItemListner;
    }

    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.calendar_cell, parent, false);
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = (int) (parent.getHeight() * 0.16666666);
        return new CalendarViewHolder(view, onItemListner);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position) {
        holder.dayOfMonth.setText(dayOfMonth.get(position));
    }

    @Override
    public int getItemCount() {

        return dayOfMonth.size();
    }

    public interface OnItemListner {
        void onItemClick(int position, String dayText);
    }
}
