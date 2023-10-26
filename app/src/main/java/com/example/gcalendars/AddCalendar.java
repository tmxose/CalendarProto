package com.example.gcalendars;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

class AddCalendarDialog extends Dialog {

    private EditText calendarNameEditText;

    private DatabaseReference databaseReference;
    private FirebaseUser user;

    public AddCalendarDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_calendar);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        user = FirebaseAuth.getInstance().getCurrentUser();

        calendarNameEditText = findViewById(R.id.editTextCalendarName);
        Button createButton = findViewById(R.id.buttonCreate);
        Button cancelButton = findViewById(R.id.buttonCancel);

        createButton.setOnClickListener(view -> {
            String calendarName = calendarNameEditText.getText().toString().trim();
            if (!calendarName.isEmpty()) {
                String userId = user.getUid();
                // 사용자의 UID를 사용하여 해당 사용자의 루트 노드 아래에 "calendars" 노드를 만들고 그 아래에 캘린더 정보를 저장합니다.
                DatabaseReference userCalendarsRef = databaseReference.child("users").child(userId).child("calendars");
                String calendarId = userCalendarsRef.push().getKey();
                assert calendarId != null;
                userCalendarsRef.child(calendarId).child("calendarName").setValue(calendarName);

                Toast.makeText(getContext(), "캘린더가 생성되었습니다.", Toast.LENGTH_SHORT).show();
                dismiss();
            }
        });

        cancelButton.setOnClickListener(view -> dismiss());
    }
}
