package com.example.gcalendars.custom;

import android.app.DatePickerDialog;
import android.content.Intent;
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

    private EditText eventDateEditText;
    private EditText eventTitleEditText;
    private EditText eventContentEditText;
    private DatePickerDialog datePickerDialog;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    String selectedDate;
    String collectionName; // 캘린더 컬렉션 이름
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_add);

        eventDateEditText = findViewById(R.id.editTextEventDate);
        eventTitleEditText = findViewById(R.id.editTextEventTitle);
        eventContentEditText = findViewById(R.id.editTextEventContent);
        RadioGroup privacyRadioGroup = findViewById(R.id.privacyRadioGroup);

        Button saveButton = findViewById(R.id.buttonSaveEvent);

        // 캘린더 아이디와 이름을 인텐트에서 받아와서 컬렉션 이름 설정
        collectionName = getIntent().getStringExtra("collectionName");

        // 인텐트 받아오기
        Intent intent = getIntent();
        // 선택한 날짜를 받아옵니다.
        selectedDate = intent.getStringExtra("selectedDate");
        eventDateEditText.setText(selectedDate);
        // 날짜 선택 버튼에 리스너 추가
        eventDateEditText.setOnClickListener(v -> showDateDialog());

        // 저장 버튼에 리스너 추가
        saveButton.setOnClickListener(v -> saveEvent(privacyRadioGroup));
    }

    // 날짜 선택 다이얼로그를 띄우는 함수
    private void showDateDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDayOfMonth) -> {
                    selectedDate = selectedYear + " " + (selectedMonth + 1) + " " + selectedDayOfMonth;
                    eventDateEditText.setText(selectedDate);
                    datePickerDialog.dismiss(); // 다이얼로그를 닫습니다.
                }, year, month, day);
        datePickerDialog.show();
        datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(Color.BLUE);
        datePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(Color.BLUE);
    }

    // 일정을 저장하는 함수
    private void saveEvent(RadioGroup privacyRadioGroup) {
        String eventDate = eventDateEditText.getText().toString();
        String eventTitle = eventTitleEditText.getText().toString();
        String eventContent = eventContentEditText.getText().toString();
        String eventPrivacy = getPrivacySelection(privacyRadioGroup);


        if (!eventTitle.isEmpty() && !eventDate.isEmpty()) {
            Map<String, Object> event = new HashMap<>();
            event.put("privacy", eventPrivacy);
            event.put("date", eventDate);
            event.put("title", eventTitle);

            // 이 부분에서 줄바꿈 처리
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
            Toast.makeText(this, "일정 제목과 날짜를 입력해 주세요.", Toast.LENGTH_SHORT).show();
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