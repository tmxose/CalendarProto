package com.example.gcalendars.custom;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AddEvent extends AppCompatActivity {

    private EditText eventStartDateEditText; // 시작 날짜를 입력할 EditText
    private EditText eventEndDateEditText; // 종료 날짜를 입력할 EditText
    private EditText eventTitleEditText;
    private EditText eventContentEditText;
    private DatePickerDialog startDatePickerDialog;
    private DatePickerDialog endDatePickerDialog;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    String collectionName; // 캘린더 컬렉션 이름

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

        // 캘린더 아이디와 이름을 인텐트에서 받아와서 컬렉션 이름 설정
        collectionName = getIntent().getStringExtra("collectionName");

        // 시작 날짜 선택 다이얼로그 표시
        eventStartDateEditText.setOnClickListener(v -> showStartDateDialog());

        // 종료 날짜 선택 다이얼로그 표시
        eventEndDateEditText.setOnClickListener(v -> showEndDateDialog());

        // 저장 버튼에 리스너 추가
        saveButton.setOnClickListener(v -> saveEvent(privacyRadioGroup));
    }

    // 시작 날짜 선택 다이얼로그 표시
    private void showStartDateDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        startDatePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDayOfMonth) -> {
                    String selectedStartDate = selectedYear + " " + (selectedMonth + 1) + " " + selectedDayOfMonth;
                    eventStartDateEditText.setText(selectedStartDate);
                    startDatePickerDialog.dismiss();
                }, year, month, day);
        startDatePickerDialog.show();
        startDatePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(Color.BLUE);
        startDatePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(Color.BLUE);
    }

    // 종료 날짜 선택 다이얼로그 표시
    private void showEndDateDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        endDatePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDayOfMonth) -> {
                    String selectedEndDate = selectedYear + " " + (selectedMonth + 1) + " " + selectedDayOfMonth;
                    eventEndDateEditText.setText(selectedEndDate);
                    endDatePickerDialog.dismiss();
                }, year, month, day);
        endDatePickerDialog.show();
        endDatePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(Color.BLUE);
        endDatePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(Color.BLUE);
    }

    // 일정을 저장하는 함수
    private void saveEvent(RadioGroup privacyRadioGroup) {
        String eventStartDate = eventStartDateEditText.getText().toString();
        String eventEndDate = eventEndDateEditText.getText().toString();
        String eventTitle = eventTitleEditText.getText().toString();
        String eventContent = eventContentEditText.getText().toString();
        String eventPrivacy = getPrivacySelection(privacyRadioGroup);

        if (!eventTitle.isEmpty() && !eventStartDate.isEmpty() && !eventEndDate.isEmpty()) {
            Map<String, Object> event = new HashMap<>();
            event.put("privacy", eventPrivacy);
            event.put("startDate", eventStartDate); // 시작 날짜 저장
            event.put("endDate", eventEndDate);     // 종료 날짜 저장
            event.put("title", eventTitle);

            String[] contentLines = eventContent.split("\n");
            ArrayList<String> contentList = new ArrayList<>(Arrays.asList(contentLines));
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

    private String getPrivacySelection(RadioGroup privacyRadioGroup) {
        int selectedId = privacyRadioGroup.getCheckedRadioButtonId();
        if (selectedId == R.id.radioPublic) {
            return "public";
        } else {
            return "private";
        }
    }
}
