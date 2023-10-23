package com.example.gcalendars.LogIn;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import com.example.gcalendars.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private EditText usernameEditText, passwordEditText, emailEditText;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        emailEditText = findViewById(R.id.emailEditText);
        Button registerButton = findViewById(R.id.registerButton);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        registerButton.setOnClickListener(v -> handleRegistration());
    }

    private void handleRegistration() {
        String username = usernameEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        String email = emailEditText.getText().toString();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password) || TextUtils.isEmpty(email)) {
            Toast.makeText(RegisterActivity.this, "모든 필드를 입력해 주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Firestore에 사용자 정보 저장
                            saveUserDataToFirestore(user.getUid(), username, email);
                            sendEmailVerification(user);
                        }
                    } else {
                        Toast.makeText(RegisterActivity.this, "회원가입 실패", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserDataToFirestore(String uid, String username, String email) {
        // "users" 컬렉션에 사용자 정보 저장
        db.collection("users")
                .document(uid)
                .set(getUserData(username, email))
                .addOnSuccessListener(aVoid -> Toast.makeText(RegisterActivity.this, "사용자 정보 저장 성공", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(RegisterActivity.this, "사용자 정보 저장 실패", Toast.LENGTH_SHORT).show());
    }

    private Map<String, Object> getUserData(String username, String email) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("username", username);
        userData.put("email", email);

        return userData;
    }

    private void sendEmailVerification(FirebaseUser user) {
        user.sendEmailVerification()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(RegisterActivity.this, "이메일 인증 이메일이 전송되었습니다.", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                        finish();
                    } else {
                        Toast.makeText(RegisterActivity.this, "이메일 인증 이메일 전송 실패", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
