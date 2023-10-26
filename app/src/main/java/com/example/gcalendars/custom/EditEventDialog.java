package com.example.gcalendars.custom;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.gcalendars.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditEventDialog extends Dialog {
    private final String title; // 이벤트 제목
    private final String startDate; // 시작 날짜
    private final String endDate; // 종료 날짜
    private final List<String> content; // 이벤트 내용
    private String privacy; // 이벤트 프라이버시
    private final String collectionName; // Firestore 컬렉션 이름

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private EditText titleEditText;
    private EditText startDateEditText;
    private EditText endDateEditText;
    private EditText contentEditText;

    public EditEventDialog(Context context, String title, String startDate, String endDate, List<String> content, String privacy, String collectionName) {
        super(context);
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
        this.content = content;
        this.privacy = privacy;
        this.collectionName = collectionName;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_event_dialog);

        titleEditText = findViewById(R.id.editTextEventTitle);
        startDateEditText = findViewById(R.id.editTextEventStartDate);
        endDateEditText = findViewById(R.id.editTextEventEndDate);
        contentEditText = findViewById(R.id.editTextEventContent);
        RadioGroup privacyRadioGroup = findViewById(R.id.privacyRadioGroup);

        titleEditText.setText(title);
        startDateEditText.setText(startDate);
        endDateEditText.setText(endDate);
        contentEditText.setText(TextUtils.join("\n", content));

        if (privacy.equals("public")) {
            privacyRadioGroup.check(R.id.radioPublic);
        } else {
            privacyRadioGroup.check(R.id.radioPrivate);
        }

        privacyRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioPublic) {
                privacy = "public";
            } else {
                privacy = "private";
            }
        });

        Button updateButton = findViewById(R.id.updateButton);
        Button cancelButton = findViewById(R.id.cancelButton);

        updateButton.setOnClickListener(v -> {
            String updatedTitle = titleEditText.getText().toString();
            String updatedStartDate = startDateEditText.getText().toString();
            String updatedEndDate = endDateEditText.getText().toString();
            List<String> updatedContent = new ArrayList<>(Arrays.asList(contentEditText.getText().toString().split("\n")));

            updateEventInFirestore(updatedTitle, updatedStartDate, updatedEndDate, updatedContent, privacy);
        });

        cancelButton.setOnClickListener(v -> dismiss());
    }

    private void updateEventInFirestore(String updatedTitle, String updatedStartDate, String updatedEndDate, List<String> updatedContent, String updatedPrivacy) {
        db.collection(collectionName)
                .whereEqualTo("title", title)
                .whereGreaterThanOrEqualTo("startDate", updatedStartDate) // 시작 날짜가 선택 날짜보다 같거나 이후
                .whereLessThanOrEqualTo("endDate", updatedEndDate)     // 종료 날짜가 선택 날짜보다 같거나 이전
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        if (document.exists()) {
                            String documentId = document.getId();
                            Map<String, Object> data = new HashMap<>();
                            data.put("title", updatedTitle);
                            data.put("startDate", updatedStartDate);
                            data.put("endDate", updatedEndDate);
                            data.put("content", updatedContent);
                            data.put("privacy", updatedPrivacy);

                            db.collection(collectionName).document(documentId)
                                    .set(data)
                                    .addOnSuccessListener(aVoid -> {
                                        dismiss();
                                        Toast.makeText(getContext(), "일정이 업데이트되었습니다.", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(getContext(), "일정 업데이트에 실패했습니다.", Toast.LENGTH_SHORT).show());
                            return;
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "일정 업데이트 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show());
    }

}
