package com.example.gcalendars.LogIn;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.gcalendars.MainActivity;
import com.example.gcalendars.R;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {
    private EditText emailEditText, passwordEditText;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        mAuth = FirebaseAuth.getInstance();

        findViewById(R.id.loginButton).setOnClickListener(v -> handleLogin());

        findViewById(R.id.registerButton).setOnClickListener(v -> {
            // 회원가입 화면으로 이동
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

        // 비밀번호 찾기 버튼
        Button forgotPasswordButton = findViewById(R.id.forgotPasswordButton);
        forgotPasswordButton.setOnClickListener(v -> handleForgotPassword());
    }

    private void handleLogin() {
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(LoginActivity.this, "이메일과 비밀번호를 입력해 주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // 로그인 성공
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish(); // 로그인 액티비티 종료
                    } else {
                        // 로그인 실패
                        Toast.makeText(LoginActivity.this, "로그인 실패", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void handleForgotPassword() {
        String emailAddress = emailEditText.getText().toString();

        if (TextUtils.isEmpty(emailAddress)) {
            Toast.makeText(LoginActivity.this, "이메일을 입력해 주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Firebase 비밀번호 재설정 이메일 보내기
        FirebaseAuth.getInstance().sendPasswordResetEmail(emailAddress)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // 이메일이 성공적으로 보내진 경우
                        Toast.makeText(LoginActivity.this, "비밀번호 재설정 이메일이 전송되었습니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        // 이메일 보내기 실패한 경우
                        Toast.makeText(LoginActivity.this, "비밀번호 재설정 이메일 보내기 실패", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
