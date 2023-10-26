package com.example.gcalendars.custom;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
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
    private final String date; // 이벤트 날짜
    private final List<String> content; // 이벤트 내용 (변경된 부분: List<String>으로 변경)
    private String privacy; // 이벤트 프라이버시

    private final String collectionName = "CustomCalendar"; // Firestore 컬렉션 이름
    private final FirebaseFirestore db = FirebaseFirestore.getInstance(); // Firestore 인스턴스

    private EditText titleEditText;
    private EditText dateEditText;
    private EditText contentEditText;
    private RadioButton selectedPrivacyRadioButton;

    private OnEventUpdatedListener eventUpdatedListener; // 이벤트 업데이트 리스너

    public EditEventDialog(Context context, String title, String date, List<String> content, String privacy) {
        super(context);
        this.title = title;
        this.date = date;
        this.content = content;
        this.privacy = privacy;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_event_dialog); // 커스텀 다이얼로그 레이아웃 설정

        // 다이얼로그 내의 UI 요소 초기화
        titleEditText = findViewById(R.id.editTextEventTitle);
        dateEditText = findViewById(R.id.editTextEventDate);
        contentEditText = findViewById(R.id.editTextEventContent);
        RadioGroup privacyRadioGroup = findViewById(R.id.privacyRadioGroup);

        // 기존 이벤트 정보를 UI에 설정
        titleEditText.setText(title);
        dateEditText.setText(date);
        contentEditText.setText(TextUtils.join("\n", content)); // List<String>을 개행 문자로 구분하여 표시

        if (privacy.equals("public")) {
            privacyRadioGroup.check(R.id.radioPublic);
        } else {
            privacyRadioGroup.check(R.id.radioPrivate);
        }

        selectedPrivacyRadioButton = findViewById(privacyRadioGroup.getCheckedRadioButtonId());
        if (selectedPrivacyRadioButton != null) {
            privacy = getPrivacySelection(selectedPrivacyRadioButton); // 선택한 프라이버시 설정
        }

        // 업데이트 버튼과 취소 버튼의 클릭 이벤트 처리
        Button updateButton = findViewById(R.id.updateButton);
        Button cancelButton = findViewById(R.id.cancelButton);

        updateButton.setOnClickListener(v -> {
            // 사용자가 입력한 업데이트된 정보 가져오기
            String updatedTitle = titleEditText.getText().toString();
            String updatedDate = dateEditText.getText().toString();
            List<String> updatedContent = new ArrayList<>(Arrays.asList(contentEditText.getText().toString().split("\n"))); // 개행 문자로 구분된 내용을 List<String>으로 변환
            String updatedPrivacy = getPrivacySelection(selectedPrivacyRadioButton);

            if (eventUpdatedListener != null) {
                updateEventInFirestore(updatedTitle, updatedDate, updatedContent, updatedPrivacy);
            }
        });

        cancelButton.setOnClickListener(v -> dismiss()); // 취소 버튼을 누르면 다이얼로그 닫기
    }

    private void updateEventInFirestore(String updatedTitle, String updatedDate, List<String> updatedContent, String updatedPrivacy) {
        // Firestore에서 해당 이벤트를 찾아 업데이트
        db.collection(collectionName).whereEqualTo("title", title)
                .whereEqualTo("date", date)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        if (document.exists()) {
                            String documentId = document.getId();
                            Map<String, Object> data = new HashMap<>();
                            data.put("title", updatedTitle);
                            data.put("date", updatedDate);
                            data.put("content", updatedContent);
                            data.put("privacy", updatedPrivacy);

                            db.collection(collectionName).document(documentId)
                                    .set(data)
                                    .addOnSuccessListener(aVoid -> {
                                        if (eventUpdatedListener != null) {
                                            eventUpdatedListener.onEventUpdated(updatedTitle, updatedDate, updatedContent, updatedPrivacy);
                                        }

                                        dismiss(); // 다이얼로그 닫기
                                        Toast.makeText(getContext(), "일정이 업데이트되었습니다.", Toast.LENGTH_SHORT).show(); // 성공 메시지

                                    })
                                    .addOnFailureListener(e -> {
                                        // 업데이트 실패 처리
                                        Toast.makeText(getContext(), "일정 업데이트에 실패했습니다.", Toast.LENGTH_SHORT).show(); // 실패 메시지

                                    });
                            return;
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    // 쿼리 실패 처리
                    Toast.makeText(getContext(), "일정 업데이트 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                });
    }

    public void setOnEventUpdatedListener(OnEventUpdatedListener listener) {
        this.eventUpdatedListener = listener;
    }

    public interface OnEventUpdatedListener {
        void onEventUpdated(String title, String date, List<String> content, String privacy);
    }

    private String getPrivacySelection(RadioButton radioButton) {
        int selectedId = radioButton.getId();
        if (selectedId == R.id.radioPublic) {
            return "public";
        } else {
            return "private";
        }
    }
}
