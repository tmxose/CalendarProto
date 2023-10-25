package com.example.gcalendars.custom;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.example.gcalendars.R;
public class EditEventDialog extends Dialog {
    private final String eventId;
    private final String title;
    private final String date;
    private final String content;
    private String privacy;

    private EditText titleEditText;
    private EditText dateEditText;
    private EditText contentEditText;
    private RadioButton selectedPrivacyRadioButton; // 추가된 부분


    private OnEventUpdatedListener eventUpdatedListener;

    public EditEventDialog(Context context, String eventId, String title, String date, String content, String privacy) {
        super(context);
        this.eventId = eventId;
        this.title = title;
        this.date = date;
        this.content = content;
        this.privacy = privacy;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_event_dialog);

        titleEditText = findViewById(R.id.editTextEventTitle);
        dateEditText = findViewById(R.id.editTextEventDate);
        contentEditText = findViewById(R.id.editTextEventContent);
        RadioGroup privacyRadioGroup = findViewById(R.id.privacyRadioGroup);

        // Firestore에서 가져온 정보로 EditText 및 RadioGroup 초기화
        titleEditText.setText(title);
        dateEditText.setText(date);

        // 문자열 내의 개행 문자를 \n으로 변환하여 contentEditText에 설정
        contentEditText.setText(content.replace("\\n", "\n"));

        // privacy를 기반으로 라디오 버튼 선택 설정
        if (privacy.equals("public")) {
            privacyRadioGroup.check(R.id.radioPublic);
        } else {
            privacyRadioGroup.check(R.id.radioPrivate);
        }

        selectedPrivacyRadioButton = findViewById(privacyRadioGroup.getCheckedRadioButtonId());
        if (selectedPrivacyRadioButton != null) {
            privacy = getPrivacySelection(selectedPrivacyRadioButton);
        }

        Button updateButton = findViewById(R.id.updateButton);
        Button cancelButton = findViewById(R.id.cancelButton);

        updateButton.setOnClickListener(v -> {
            // 수정된 정보를 가져와 리스너를 통해 업데이트 이벤트 호출
            String updatedTitle = titleEditText.getText().toString();
            String updatedDate = dateEditText.getText().toString();
            String updatedContent = contentEditText.getText().toString();
            String updatedPrivacy = getPrivacySelection(selectedPrivacyRadioButton); // 수정된 부분

            if (eventUpdatedListener != null) {
                eventUpdatedListener.onEventUpdated(eventId, updatedTitle, updatedDate, updatedContent, updatedPrivacy);
            }

            dismiss(); // 다이얼로그 닫기
        });

        cancelButton.setOnClickListener(v -> dismiss()); // 다이얼로그 닫기
    }
    public void setOnEventUpdatedListener(OnEventUpdatedListener listener) {
        this.eventUpdatedListener = listener;
    }

    public interface OnEventUpdatedListener {
        void onEventUpdated(String eventId, String title, String date, String content, String privacy);
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
