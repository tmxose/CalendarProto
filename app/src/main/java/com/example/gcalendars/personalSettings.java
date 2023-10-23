package com.example.gcalendars;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.gcalendars.LogIn.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class personalSettings extends AppCompatActivity {
    private TextView usernameTextView;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_personal);

        usernameTextView = findViewById(R.id.usernameTextView);
        TextView emailEditText = findViewById(R.id.emailEditText);
        Button updateButton = findViewById(R.id.updateButton);
        Button logoutButton = findViewById(R.id.logoutButton);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            usernameTextView.setText(currentUser.getDisplayName());
            emailEditText.setText(currentUser.getEmail());
        }

        updateButton.setOnClickListener(v -> updateUsername());
        logoutButton.setOnClickListener(v -> logout());
        Button exitButton = findViewById(R.id.exitButton);
        exitButton.setOnClickListener(v -> startActivity(new Intent(this, MainActivity.class)));



    }

    private void updateUsername() {
        String newUsername = usernameTextView.getText().toString();

        // 사용자 프로필 업데이트 정보 생성
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(newUsername)
                .build();

        currentUser.updateProfile(profileUpdates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(personalSettings.this, "사용자 이름 업데이트 성공", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(personalSettings.this, "사용자 이름 업데이트 실패", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void logout() {
        mAuth.signOut();
        startActivity(new Intent(personalSettings.this, LoginActivity.class));
        finish();
    }
}
