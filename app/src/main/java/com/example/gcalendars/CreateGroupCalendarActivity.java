package com.example.gcalendars;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class CreateGroupCalendarActivity extends AppCompatActivity {

    private EditText groupNameEditText;
    private DatabaseReference groupDatabaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group_calendar);

        groupNameEditText = findViewById(R.id.groupNameEditText);
        Button createGroupButton = findViewById(R.id.createGroupButton);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        String userId = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();

        groupDatabaseRef = FirebaseDatabase.getInstance().getReference().child("users").child(userId).child("group-calendar");

        createGroupButton.setOnClickListener(v -> createGroup());
    }

    private void createGroup() {
        String groupName = groupNameEditText.getText().toString().trim();
        if (groupName.isEmpty()) {
            groupNameEditText.setError("그룹 이름을 입력하세요.");
            return;
        }

        final String groupId = groupDatabaseRef.push().getKey(); // 새로운 그룹 ID 생성

        // 그룹 정보를 Realtime Database에 저장
        DatabaseReference groupRef = groupDatabaseRef.child(Objects.requireNonNull(groupId));
        groupRef.child("groupId").setValue(groupId); // 그룹 ID를 "groupId" 속성에 저장
        groupRef.child("group-calendarName").setValue(groupName); // 그룹 이름 저장


        finish(); // 액티비티 종료

    }
}
