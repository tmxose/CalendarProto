package com.example.gcalendars;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import com.example.gcalendars.custom.CustomCalendar;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // "일정 보기" 버튼 초기화 및 클릭 이벤트 처리
        Button buttonCustomCalendar = findViewById(R.id.buttonCustomCalendar);
        Button buttonAddCalendar = findViewById(R.id.addButton);

        buttonCustomCalendar.setOnClickListener(v -> {
            // CustomCalendar 액티비티로 이동
            startActivity(new Intent(MainActivity.this, CustomCalendar.class));
        });

        buttonAddCalendar.setOnClickListener(v ->{
            // 다이얼로그 띄우기
            showAddCalendarDialog();
        });
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
}
