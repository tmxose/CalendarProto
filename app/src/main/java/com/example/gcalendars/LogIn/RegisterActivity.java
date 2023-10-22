package com.example.gcalendars.LogIn;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.gcalendars.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.Objects;

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
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        Button registerButton = findViewById(R.id.registerButton);
        registerButton.setOnClickListener(v -> {
            // 사용자가 입력한 정보 가져오기
            String username = usernameEditText.getText().toString();
            String password = passwordEditText.getText().toString();
            String email = emailEditText.getText().toString();

            // Firebase에 사용자 등록
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // 회원가입 성공
                            String uid = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

                            // Firestore에 사용자 정보 저장
                            User user = new User(username, email); // User 클래스를 활용
                            db.collection("users").document(uid)
                                    .set(user, SetOptions.merge())
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            // 사용자 정보 저장 성공
                                            Toast.makeText(RegisterActivity.this, "회원가입 성공", Toast.LENGTH_SHORT).show();
                                            finish(); // RegisterActivity 종료
                                        } else {
                                            // 사용자 정보 저장 실패
                                            Toast.makeText(RegisterActivity.this, "사용자 정보 저장 실패", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            // 회원가입 실패
                            Toast.makeText(RegisterActivity.this, "회원가입 실패", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}
