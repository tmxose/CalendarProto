package com.example.gcalendars;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.ActionBar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.gcalendars.custom.CustomCalendar;
import com.google.android.material.navigation.NavigationView;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button buttonCustomCalendar = findViewById(R.id.buttonCustomCalendar);

        buttonCustomCalendar.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, CustomCalendar.class)));

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu); // 액션바에 버튼 아이콘 설정
        }

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        // "메뉴 열기" 버튼 클릭 이벤트
        Objects.requireNonNull(actionBar).setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        actionBar.setHomeButtonEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // 액션바의 버튼을 누를 때 사이드바 열기
            drawerLayout.openDrawer(navigationView, true);
            return true;
        }else if (id == R.id.menu_profile_settings) {
            // "개인정보 설정" 메뉴를 눌렀을 때의 동작을 여기에 추가
            startActivity(new Intent(this, personalSettings.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
