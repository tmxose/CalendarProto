package com.example.gcalendars;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button buttonPersonalCalendar = findViewById(R.id.buttonPersonalCalendar);
        Button buttonGroupCalendar = findViewById(R.id.buttonGroupCalendar);

        buttonPersonalCalendar.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MyCalendar.class);
            startActivity(intent);
        });

        buttonGroupCalendar.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, GroupCalendar.class);
            startActivity(intent);
        });
    }
}
