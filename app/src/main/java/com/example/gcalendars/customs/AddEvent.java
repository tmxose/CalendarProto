package com.example.gcalendars.customs;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.gcalendars.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddEvent extends AppCompatActivity {
    private EditText eventStartDateEditText;
    private EditText eventEndDateEditText;
    private EditText eventTitleEditText;
    private EditText eventContentEditText;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String collectionName;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy MM dd");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_add);

        RadioGroup privacyRadioGroup = findViewById(R.id.privacyRadioGroup);
        eventStartDateEditText = findViewById(R.id.editTextStartDate);
        eventEndDateEditText = findViewById(R.id.editTextEndDate);
        eventTitleEditText = findViewById(R.id.editTextEventTitle);
        eventContentEditText = findViewById(R.id.editTextEventContent);

        Button saveButton = findViewById(R.id.buttonSaveEvent);

        collectionName = getIntent().getStringExtra("collectionName");

        eventStartDateEditText.setOnClickListener(v -> showDatePickerDialog(eventStartDateEditText));
        eventEndDateEditText.setOnClickListener(v -> showDatePickerDialog(eventEndDateEditText));

        saveButton.setOnClickListener(v -> saveEvent(privacyRadioGroup));
    }

    // 일정 날짜 선택 다이얼로그 함수
    private void showDatePickerDialog(EditText editText) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDayOfMonth) -> {
            // 월과 날짜를 2자리로 표시하고 월에 1을 더합니다.
            @SuppressLint("DefaultLocale")
            String selectedDate = String.format("%04d %02d %02d", selectedYear, selectedMonth + 1, selectedDayOfMonth);
            editText.setText(selectedDate);
        }, year, month, day);

        datePickerDialog.show();
        datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(Color.BLUE);
        datePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(Color.BLUE);
    }
    // 일정 저장
    private void saveEvent(RadioGroup privacyRadioGroup) {
        String startDate = eventStartDateEditText.getText().toString();
        String endDate = eventEndDateEditText.getText().toString();
        String title = eventTitleEditText.getText().toString();
        String content = eventContentEditText.getText().toString();
        String privacy = getPrivacySelection(privacyRadioGroup);

        if (!title.isEmpty() && !startDate.isEmpty() && !endDate.isEmpty()) {
            List<String> dates = getDatesBetween(startDate, endDate);
            Map<String, Object> event = new HashMap<>();
            event.put("privacy", privacy);
            event.put("dates", dates); // Use the dates array
            event.put("title", title);

            String[] contentLines = content.split("\n");
            List<String> contentList = new ArrayList<>(Arrays.asList(contentLines));
            event.put("content", contentList);

            db.collection(collectionName).add(event)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(getApplicationContext(), "일정이 추가되었습니다.", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(getApplicationContext(), "일정 추가에 실패했습니다.", Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(this, "일정 제목, 시작 날짜, 종료 날짜를 입력해 주세요.", Toast.LENGTH_SHORT).show();
        }
    }

    // 날짜를 선택한 시작 날짜부터 끝 날짜까지 기간의 날짜정보를 리스트 형태로 반환해주는 함수
    private List<String> getDatesBetween(String startDate, String endDate) {
        List<String> dates = new ArrayList<>();
        LocalDate start = LocalDate.parse(startDate, formatter);
        LocalDate end = LocalDate.parse(endDate, formatter);

        while (!start.isAfter(end)) {
            dates.add(start.format(formatter));
            start = start.plusDays(1);
        }
        return dates;
    }

    // 공개 비공개 라디오버튼 확인하여 문자열값 반환하는 함수
    private String getPrivacySelection(RadioGroup privacyRadioGroup) {
        int selectedId = privacyRadioGroup.getCheckedRadioButtonId();
        return (selectedId == R.id.radioPublic) ? "public" : "private";
    }
}