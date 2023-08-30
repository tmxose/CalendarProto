package com.example.gcalendars;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class CalendarViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
    public final TextView dayOfMonth;
    private final CalendarAdapter.OnItemListner onItemListner;
    public CalendarViewHolder(@NonNull View itemView, CalendarAdapter.OnItemListner onItemListner) {
        super(itemView);
        dayOfMonth = itemView.findViewById(R.id.cellDayText);
        this.onItemListner = onItemListner;
        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        onItemListner.onItemClick(getAdapterPosition(), (String) dayOfMonth.getText());
    }
}
