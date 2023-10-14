package com.example.gcalendars.calendar;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.gcalendars.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MyCalendar extends AppCompatActivity {
    private TextView editTextDate;  // 선택한 날짜를 표시하는 TextView
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy MM dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendar_my);

        Button addButton = findViewById(R.id.buttonAdd); // "일정 추가" 버튼
        // XML 레이아웃에서 해당 TextView 찾기
        editTextDate = findViewById(R.id.editTextDate);

        CalendarView calendarView = findViewById(R.id.calendarView); // CalendarView

        // 캘린더 뷰의 날짜 선택 이벤트 처리
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar selectedCalendar = Calendar.getInstance();
            selectedCalendar.set(year, month, dayOfMonth);
            String selectedDate = dateFormatter.format(selectedCalendar.getTime());
            editTextDate.setText(selectedDate); // 선택한 날짜를 TextView에 표시
        });

        addButton.setOnClickListener(v -> {
            String selectedDate = editTextDate.getText().toString();
            // 선택한 날짜를 인텐트에 추가
            Intent intent = new Intent(MyCalendar.this, AddEvent.class);
            intent.putExtra("selectedDate", selectedDate);
            startActivity(intent); // AddEvent 액티비티 시작
        });

        editTextDate.setOnClickListener(v -> {
            showDatePicker(); // 날짜 선택 다이얼로그 표시
        });
    }

    // 선택한 날짜를 다이얼로그에서 보여주는 함수
    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    String selectedDate = year1 + "년 " + (monthOfYear + 1) + "월 " + dayOfMonth + "일";
                    editTextDate.setText(selectedDate); // 선택한 날짜를 TextView에 표시
                }, year, month, day);

        datePickerDialog.show(); // 날짜 선택 다이얼로그 표시
    }
}