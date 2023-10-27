package com.example.gcalendars.custom;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.icu.util.Calendar;
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
    private final String selectedDate; // 현재 날짜
    private final List<String> dates; // 날짜 목록
    private final List<String> content; // 이벤트 내용
    private String privacy; // 이벤트 프라이버시
    private final String collectionName; // Firestore 컬렉션 이름

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private EditText titleEditText;
    private EditText startDateEditText;
    private EditText endDateEditText;
    private EditText contentEditText;

    // 업데이트 이벤트 리스너
    public interface OnUpdateEventListener {
        void onUpdateEvent();
    }

    private OnUpdateEventListener onUpdateEventListener;

    public EditEventDialog(Context context, String title, String selectedDate, List<String> dates, List<String> content, String privacy, String collectionName) {
        super(context);
        this.title = title;
        this.selectedDate = selectedDate;
        this.dates = dates;
        this.content = content;
        this.privacy = privacy;
        this.collectionName = collectionName;
    }

    // 이벤트 리스너 등록 메소드
    public void setOnUpdateEventListener(OnUpdateEventListener listener) {
        this.onUpdateEventListener = listener;
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

        startDateEditText.setOnClickListener(v -> {
            // 클릭 시 날짜 다이얼로그를 표시하는 코드를 추가
            showStartDateDialog();
        });

        endDateEditText.setOnClickListener(v -> {
            // 클릭 시 날짜 다이얼로그를 표시하는 코드를 추가
            showEndDateDialog();
        });

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

            // 업데이트된 시작날짜와 끝나는 날짜를 arrDate 변수에 저장
            List<String> arrDate = new ArrayList<>();
            arrDate.add(updatedStartDate);
            arrDate.add(updatedEndDate);

            if (!updatedTitle.isEmpty() && !updatedStartDate.isEmpty() && !updatedEndDate.isEmpty()) {
                updateEventInFirestore(updatedTitle, arrDate, updatedContent, privacy);
            } else {
                Toast.makeText(getContext(), "일정 제목, 시작 날짜, 종료 날짜를 입력해 주세요.", Toast.LENGTH_SHORT).show();
            }
        });

        cancelButton.setOnClickListener(v -> dismiss());
    }

    // 업데이트 메소드
    private void updateEventInFirestore(String updatedTitle, List<String> updatedDates, List<String> updatedContent, String updatedPrivacy) {
        // Firestore에서 해당 일정을 검색합니다.
        db.collection(collectionName)
                .whereArrayContains("dates", selectedDate) // 여기서 dates 대신 selectedDate를 사용
                .whereEqualTo("title", title)
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
                                    // 업데이트 이벤트 발생
                                    if (onUpdateEventListener != null) {
                                        onUpdateEventListener.onUpdateEvent();
                                    }
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

    private void showStartDateDialog() {
        // 현재 날짜 정보 가져오기
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        // DatePickerDialog를 사용하여 시작 날짜 선택 다이얼로그를 표시
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (view, year1, month1, dayOfMonth1) -> {
            // 사용자가 선택한 날짜를 여기에서 처리
            String selectedDate = year1 + "-" + (month1 + 1) + "-" + dayOfMonth1; // 날짜 형식에 따라 수정

            // startDateEditText에 선택한 날짜를 설정
            startDateEditText.setText(selectedDate);
        }, year, month, dayOfMonth);

        // 다이얼로그 표시
        datePickerDialog.show();
        datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(Color.BLUE);
        datePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(Color.BLUE);
    }

    private void showEndDateDialog() {
        // 현재 날짜 정보 가져오기
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        // DatePickerDialog를 사용하여 종료 날짜 선택 다이얼로그를 표시
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (view, year1, month1, dayOfMonth1) -> {
            // 사용자가 선택한 날짜를 여기에서 처리
            String selectedDate = year1 + "-" + (month1 + 1) + "-" + dayOfMonth1; // 날짜 형식에 따라 수정

            // endDateEditText에 선택한 날짜를 설정
            endDateEditText.setText(selectedDate);
        }, year, month, dayOfMonth);

        // 다이얼로그 표시
        datePickerDialog.show();
        datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(Color.BLUE);
        datePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(Color.BLUE);
    }
}
