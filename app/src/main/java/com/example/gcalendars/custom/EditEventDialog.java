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
    private final List<String> dates; // 날짜 목록
    private final List<String> content; // 이벤트 내용
    private String privacy; // 이벤트 프라이버시
    private final String collectionName; // Firestore 컬렉션 이름

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private EditText titleEditText;
    private EditText startDateEditText;
    private EditText endDateEditText;
    private EditText contentEditText;

    public EditEventDialog(Context context, String title, List<String> dates, List<String> content, String privacy, String collectionName) {
        super(context);
        this.title = title;
        this.dates = dates;
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
        startDateEditText.setText(dates.get(0)); // 시작날짜를 설정
        endDateEditText.setText(dates.get(1)); // 끝나는 날짜를 설정
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

            if (!updatedTitle.isEmpty() && !updatedStartDate.isEmpty() && !updatedEndDate.isEmpty()) {
                // 업데이트된 시작날짜와 끝나는 날짜를 dates 리스트에 설정
                dates.set(0, updatedStartDate);
                dates.set(1, updatedEndDate);

                updateEventInFirestore(updatedTitle, dates, updatedContent, privacy);
            } else {
                Toast.makeText(getContext(), "일정 제목, 시작 날짜, 종료 날짜를 입력해 주세요.", Toast.LENGTH_SHORT).show();
            }
        });

        cancelButton.setOnClickListener(v -> dismiss());
    }

    private void updateEventInFirestore(String updatedTitle, List<String> updatedDates, List<String> updatedContent, String updatedPrivacy) {
        db.collection(collectionName)
                .whereEqualTo("title", title)
                .whereIn("dates", dates) // 원래 날짜 목록이 포함되어 있는 문서 찾기
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    boolean found = false;
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        found = true;
                        String documentId = document.getId();
                        Map<String, Object> data = new HashMap<>();
                        data.put("title", updatedTitle);
                        data.put("dates", updatedDates);
                        data.put("content", updatedContent);
                        data.put("privacy", updatedPrivacy);

                        db.collection(collectionName).document(documentId)
                                .set(data)
                                .addOnSuccessListener(aVoid -> {
                                    dismiss();
                                    Toast.makeText(getContext(), "일정이 업데이트되었습니다.", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> Toast.makeText(getContext(), "일정 업데이트에 실패했습니다.", Toast.LENGTH_SHORT).show());
                        break; // 이미 찾았으면 반복 중지
                    }

                    if (!found) {
                        Toast.makeText(getContext(), "해당 일정을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "일정 업데이트 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show());
    }
}
