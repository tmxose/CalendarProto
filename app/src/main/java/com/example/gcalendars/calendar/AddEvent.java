package com.example.gcalendars.calendar;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.gcalendars.R;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AddEvent extends AppCompatActivity {

    private EditText eventDateEditText;
    private EditText eventTitleEditText;
    private EditText eventContentEditText;
    private DatePickerDialog datePickerDialog;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    String selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_add);

        eventDateEditText = findViewById(R.id.editTextEventDate);
        eventTitleEditText = findViewById(R.id.editTextEventTitle);
        eventContentEditText = findViewById(R.id.editTextEventContent);
        Button saveButton = findViewById(R.id.buttonSaveEvent);

        // 인텐트 받아오기
        Intent intent = getIntent();
        // 선택한 날짜를 받아옵니다.
        selectedDate = intent.getStringExtra("selectedDate");

        // 날짜 선택 버튼에 리스너 추가
        eventDateEditText.setOnClickListener(v -> showDateDialog());

        // 저장 버튼에 리스너 추가
        saveButton.setOnClickListener(v -> saveEvent());
    }

    // 날짜 선택 다이얼로그를 띄우는 함수
    private void showDateDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDayOfMonth) -> {
                    selectedDate = selectedYear + "/" + (selectedMonth + 1) + "/" + selectedDayOfMonth;
                    eventDateEditText.setText(selectedDate);
                    datePickerDialog.dismiss(); // 다이얼로그를 닫습니다.
                }, year, month, day);
        datePickerDialog.show();
        datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(Color.BLUE);
        datePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(Color.BLUE);
    }

    // 일정을 저장하는 함수
    private void saveEvent() {
        String eventTitle = eventTitleEditText.getText().toString();
        String eventDate = eventDateEditText.getText().toString();
        String eventContent = eventContentEditText.getText().toString();

        if (!eventTitle.isEmpty() && !eventDate.isEmpty()) {
            Map<String, Object> event = new HashMap<>();
            event.put("title", eventTitle);
            event.put("date", eventDate);
            event.put("content", eventContent);

            // Firebase Firestore에 일정 정보 업로드
            db.collection("events")
                    .add(event)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(getApplicationContext(), "일정이 추가되었습니다.", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(getApplicationContext(), "일정 추가에 실패했습니다.", Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(this, "일정 제목과 날짜를 입력해주세요.", Toast.LENGTH_SHORT).show();
        }
    }
}
