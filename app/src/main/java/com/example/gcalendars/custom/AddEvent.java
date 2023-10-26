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
import java.util.List;
import java.util.Map;

public class AddEvent extends AppCompatActivity {

    private EditText eventStartDateEditText;
    private EditText eventEndDateEditText;
    private EditText eventTitleEditText;
    private EditText eventContentEditText;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String collectionName;

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

    private void showDatePickerDialog(EditText editText) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDayOfMonth) -> {
            String selectedDate = selectedYear + " " + (selectedMonth + 1) + " " + selectedDayOfMonth;
            editText.setText(selectedDate);
        }, year, month, day);

        datePickerDialog.show();
        datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(Color.BLUE);
        datePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(Color.BLUE);
    }

    private void saveEvent(RadioGroup privacyRadioGroup) {
        String startDate = eventStartDateEditText.getText().toString();
        String endDate = eventEndDateEditText.getText().toString();
        String title = eventTitleEditText.getText().toString();
        String content = eventContentEditText.getText().toString();
        String privacy = getPrivacySelection(privacyRadioGroup);

        if (!title.isEmpty() && !startDate.isEmpty() && !endDate.isEmpty()) {
            Map<String, Object> event = new HashMap<>();
            event.put("privacy", privacy);
            event.put("startDate", startDate);
            event.put("endDate", endDate);
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

    private String getPrivacySelection(RadioGroup privacyRadioGroup) {
        int selectedId = privacyRadioGroup.getCheckedRadioButtonId();
        return (selectedId == R.id.radioPublic) ? "public" : "private";
    }
}
