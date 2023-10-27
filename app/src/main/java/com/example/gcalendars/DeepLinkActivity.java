package com.example.gcalendars;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.gcalendars.LogIn.LoginActivity;
import com.example.gcalendars.custom.CustomCalendar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class DeepLinkActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deep_link);

        // URL을 파싱하고 해당 화면을 표시하는 로직을 추가합니다.
        Uri data = getIntent().getData();
        String scheme = Objects.requireNonNull(data).getScheme();
        if ("http".equals(scheme) || "https".equals(scheme)) {
            // URL 파싱 및 처리 로직을 추가
            // 여기에서 로그인 여부를 확인하고, 로그인되어 있지 않다면 로그인 화면으로 이동
            if (!isLoggedIn()) {
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            } else {
                // 사용자가 로그인한 경우 초대를 처리하는 로직을 추가
                handleInvitation(data);
            }
        }
    }

    private boolean isLoggedIn() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        return user != null; // 사용자가 로그인한 경우 true를 반환, 로그인하지 않은 경우 false를 반환
    }

    private void handleInvitation(Uri data) {
        // URL을 파싱하고 초대를 처리하는 로직을 추가
        // 예를 들어, 초대된 그룹 캘린더 정보를 추출하고 사용자에게 수락 또는 거부 옵션을 표시
        String groupId = data.getQueryParameter("groupId");
        String groupName = data.getQueryParameter("groupName");

        // 초대를 처리하는 다이얼로그를 표시
        showInvitationDialog(groupId, groupName);
    }

    private void showInvitationDialog(String groupId, String groupName) {
        // 초대를 수락 또는 거부하는 다이얼로그를 표시하고 로직을 추가
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("그룹 캘린더 초대");
        builder.setMessage("그룹 '" + groupName + "'에 초대받았습니다. 수락하시겠습니까?");

        builder.setPositiveButton("수락", (dialog, which) -> {
            // 초대를 수락하는 로직을 추가
            acceptGroupInvitation(groupId, groupName);
        });

        builder.setNegativeButton("거부", (dialog, which) -> {
            // 초대를 거부하는 로직을 추가
            rejectGroupInvitation();
        });

        builder.show();
    }
    private void acceptGroupInvitation(String groupId, String groupName) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        String userId = Objects.requireNonNull(user).getUid();

        DatabaseReference userGroupRef = FirebaseDatabase.getInstance().getReference().child("users").child(userId).child("group-calendar");
        userGroupRef.child(groupId).setValue(true);

        Toast.makeText(this, "그룹 캘린더 '" + groupName + "'를 수락했습니다.", Toast.LENGTH_SHORT).show();
        // 초대를 수락한 후, 커스텀 캘린더 페이지로 이동
        openCustomCalendar(groupId, groupName);
    }
    private void openCustomCalendar(String groupId, String groupName) {
        // 커스텀 캘린더 페이지로 이동하도록 인텐트를 설정
        Intent intent = new Intent(this, CustomCalendar.class);
        intent.putExtra("calendarId", groupId);
        intent.putExtra("calendarName", groupName);
        startActivity(intent);

        // 현재 액티비티를 종료하여 DeepLinkActivity를 종료합니다.
        finish();
    }
    private void rejectGroupInvitation() {
        Toast.makeText(this, "그룹 캘린더 초대를 거부했습니다.", Toast.LENGTH_SHORT).show();
    }
}
