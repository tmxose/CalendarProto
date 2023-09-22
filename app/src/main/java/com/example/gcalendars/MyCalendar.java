package com.example.gcalendars;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MyCalendar extends AppCompatActivity {
    private TextView editTextDate;
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy MM dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendar_my);

        editTextDate = findViewById(R.id.editTextDate);
        Button addButton = findViewById(R.id.buttonAdd);
        CalendarView calendarView = findViewById(R.id.calendarView);

        // 캘린더 뷰의 날짜 선택 이벤트 처리
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                Calendar selectedCalendar = Calendar.getInstance();
                selectedCalendar.set(year, month, dayOfMonth);
                String selectedDate = dateFormatter.format(selectedCalendar.getTime());
                editTextDate.setText(selectedDate);
            }
        });

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MyCalendar.this, AddEvent.class);
                startActivity(intent);
            }
        });
        editTextDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });
    }
    // 선택한 날짜 박스에 보여 주는 함수
    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(android.widget.DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        String selectedDate = year + "년 " + (monthOfYear + 1) + "월 " + dayOfMonth + "일";
                        editTextDate.setText(selectedDate);
                    }
                }, year, month, day);

        datePickerDialog.show();
    }
}
