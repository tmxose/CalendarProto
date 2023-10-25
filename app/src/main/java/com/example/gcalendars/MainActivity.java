package com.example.gcalendars;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.ActionBar;
import androidx.drawerlayout.widget.DrawerLayout;
import com.example.gcalendars.custom.CustomCalendar;
import com.google.android.material.navigation.NavigationView;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    // 변수 선언
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // "일정 보기" 버튼 초기화 및 클릭 이벤트 처리
        Button buttonCustomCalendar = findViewById(R.id.buttonCustomCalendar);

        buttonCustomCalendar.setOnClickListener(v -> {
            // CustomCalendar 액티비티로 이동
            startActivity(new Intent(MainActivity.this, CustomCalendar.class));
        });

        // 액션바 설정
        setupActionBar();

        // 레이아웃 요소 초기화
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
    }

    // 액션바 설정 및 사용자 정의 아이콘 추가
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu); // 사용자 정의 아이콘 설정
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.nav_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // 사용자 정의 아이콘을 클릭했을 때의 동작
            // 여기에 사용자 정의 아이콘을 클릭했을 때 수행할 동작을 추가
            drawerLayout.openDrawer(navigationView, true); // 사이드바 열기
            return true;
        } else if (id == R.id.menu_profile_settings) {
            // "개인정보 설정" 메뉴를 눌렀을 때의 동작
            startActivity(new Intent(this, personalSettings.class)); // 개인정보 설정 화면으로 이동
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
