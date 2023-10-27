package com.example.gcalendars;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.gcalendars.LogIn.UserCalendar;
import com.example.gcalendars.custom.CustomCalendar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private FirebaseUser user;
    private LinearLayout calendarButtonsLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        calendarButtonsLayout = findViewById(R.id.calendarButtonsLayout);

        if (user != null) {
            loadUserCalendars();
        }

        Button buttonAddCalendar = findViewById(R.id.addButton);

        buttonAddCalendar.setOnClickListener(v -> {
            // 다이얼로그 띄우기
            showAddCalendarDialog();
        });
        // 그룹 생성 버튼의 클릭 이벤트 처리
        Button btnCreateGroup = findViewById(R.id.btnCreateGroup);
        btnCreateGroup.setOnClickListener(v -> startActivity(new Intent(this, CreateGroupCalendarActivity.class)));
    }

    private void showAddCalendarDialog() {
        AddCalendarDialog addCalendarDialog = new AddCalendarDialog(this);
        addCalendarDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.nav_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_profile_settings) {
            // "개인정보 설정" 메뉴를 눌렀을 때의 동작
            startActivity(new Intent(this, personalSettings.class)); // 개인정보 설정 화면으로 이동
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadUserCalendars() {
        String userUid = user.getUid();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userUid).child("calendars");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<UserCalendar> userCalendars = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String calendarId = dataSnapshot.getKey();
                    String calendarName = dataSnapshot.child("calendarName").getValue(String.class);
                    userCalendars.add(new UserCalendar(calendarId, calendarName));
                }
                // 사용자의 캘린더 정보를 로드한 후 버튼을 생성
                createCalendarButtons(userCalendars);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // 처리 중에 오류가 발생한 경우 처리할 내용 추가
            }
        });
    }

    private void createCalendarButtons(List<UserCalendar> userCalendars) {
        for (UserCalendar calendarInfo : userCalendars) {
            Button calendarButton = new Button(this);
            calendarButton.setText(calendarInfo.getCalendarName());

            calendarButton.setOnClickListener(view -> {
                // 캘린더 버튼 클릭 시 커스텀 캘린더 클래스로 이동하고
                // 캘린더 아이디와 컬렉션명을 전달
                openCustomCalendar(calendarInfo.getCalendarId(), calendarInfo.getCalendarName());
            });

            calendarButtonsLayout.addView(calendarButton);
        }
    }

    private void openCustomCalendar(String calendarId, String calendarName) {
        // 커스텀 캘린더 클래스로 이동하고 정보 전달
        Intent intent = new Intent(this, CustomCalendar.class);
        intent.putExtra("calendarId", calendarId);
        intent.putExtra("calendarName", calendarName);
        startActivity(intent);
    }
}
