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
    private final String title;
    private final String date;
    private final String content;
    private String privacy;

    private EditText titleEditText;
    private EditText dateEditText;
    private EditText contentEditText;
    private RadioButton selectedPrivacyRadioButton;

    private OnEventUpdatedListener eventUpdatedListener;

    public EditEventDialog(Context context, String title, String date, String content, String privacy) {
        super(context);
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

        titleEditText.setText(title);
        dateEditText.setText(date);
        contentEditText.setText(content);

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
            String updatedTitle = titleEditText.getText().toString();
            String updatedDate = dateEditText.getText().toString();
            String updatedContent = contentEditText.getText().toString();
            String updatedPrivacy = getPrivacySelection(selectedPrivacyRadioButton);

            if (eventUpdatedListener != null) {
                eventUpdatedListener.onEventUpdated(updatedTitle, updatedDate, updatedContent, updatedPrivacy);
            }

            dismiss();
        });

        cancelButton.setOnClickListener(v -> dismiss());
    }

    public void setOnEventUpdatedListener(OnEventUpdatedListener listener) {
        this.eventUpdatedListener = listener;
    }

    public interface OnEventUpdatedListener {
        void onEventUpdated(String title, String date, String content, String privacy);
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
