package com.example.gcalendars.custom;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gcalendars.R;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class GroupCalendar extends AppCompatActivity implements CalendarAdapter.OnItemListener {

    private TextView monthYearText;
    private RecyclerView calendarRecyclerView;
    private LocalDate selectedDate;
    private DatabaseReference databaseReference; // Firebase 데이터베이스 레퍼런스
    private TextView dateTextView; // 추가된 TextView

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendar_group);
        initWidgets();
        selectedDate = LocalDate.now();
        setMonthView();

        // "일정 추가" 버튼 클릭 이벤트 처리
        Button addButton = findViewById(R.id.buttonAdd);

        addButton.setOnClickListener(v -> {
            // 선택한 날짜를 인텐트에 추가
            Intent intent = new Intent(GroupCalendar.this, AddEvent.class);
            intent.putExtra("selectedDate", selectedDate.format(DateTimeFormatter.ofPattern("yyyy MM dd")));
            startActivity(intent); // AddEvent 액티비티 시작
        });

        // "일정 삭제" 버튼 클릭 이벤트 처리
        Button deleteButton = findViewById(R.id.deleteBtn);
        deleteButton.setOnClickListener(v -> deleteEventForDate(selectedDate.format(DateTimeFormatter.ofPattern("yyyy MM dd"))));
    }

    // Firebase 데이터베이스 초기화 및 레퍼런스 설정
    private void initFirebase() {
        databaseReference = FirebaseDatabase.getInstance().getReference("events");
    }

    private void initWidgets() {
        // XML 레이아웃에서 위젯을 초기화하는 메서드.
        calendarRecyclerView = findViewById(R.id.calendarRecyclerView);
        monthYearText = findViewById(R.id.monthYearTV);
        dateTextView = findViewById(R.id.editTextDate); // 수정된 TextView 초기화
        initFirebase(); // Firebase 초기화 호출
    }

    // 월 단위로 뷰 설정
    private void setMonthView() {
        monthYearText.setText(monthYearFromDate(selectedDate));
        ArrayList<String> daysInMonth = daysInMonthArray(selectedDate);

        CalendarAdapter calendarAdapter = new CalendarAdapter(daysInMonth, this);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 7);
        calendarRecyclerView.setLayoutManager(layoutManager);
        calendarRecyclerView.setAdapter(calendarAdapter);
    }

    // 선택한 월의 날짜 목록을 생성하는 메서드
    @NonNull
    private ArrayList<String> daysInMonthArray(LocalDate date) {
        ArrayList<String> daysInMonthArray = new ArrayList<>();
        YearMonth yearMonth = YearMonth.from(date);
        int daysInMonth = yearMonth.lengthOfMonth();
        LocalDate firstOfMonth = selectedDate.withDayOfMonth(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue();

        for (int i = 1; i <= 42; i++) {
            int dayOfMonth = i - dayOfWeek;
            if (dayOfMonth > 0 && dayOfMonth <= daysInMonth) {
                daysInMonthArray.add(String.valueOf(dayOfMonth));
            } else {
                daysInMonthArray.add("");
            }
        }
        return daysInMonthArray;
    }

    // 월과 년도를 문자열로 변환
    private String monthYearFromDate(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
        return date.format(formatter);
    }

    // 이전 달로 이동
    public void previousMonthAction(View view) {
        selectedDate = selectedDate.minusMonths(1);
        setMonthView();
    }

    // 다음 달로 이동
    public void nextMonthAction(View view) {
        selectedDate = selectedDate.plusMonths(1);
        setMonthView();
    }

    @Override
    public void onItemClick(int position, String dayText) {
        // 캘린더의 날짜를 클릭했을 때 호출되는 메서드입니다.
        if (!dayText.equals("")) {
            String message = "선택한 날짜: " + dayText + " " + monthYearFromDate(selectedDate);
            dateTextView.setText(message); // TextView에 표시

            // 선택한 날짜에 대한 이벤트를 가져와 표시
            displayEventForDate(dayText);
        }
    }

    // Firebase에서 해당 날짜의 일정을 가져와 표시하는 메서드
    private void displayEventForDate(final String date) {
        databaseReference.child(date).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String eventText = dataSnapshot.getValue(String.class);
                if (eventText != null && !eventText.isEmpty()) {
                    // 해당 날짜에 일정이 있는 경우 텍스트 뷰에 표시
                    TextView dateTitle = findViewById(R.id.dateTitle);
                    dateTitle.setText(eventText);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // 오류 처리 (예: Firebase 연결 오류)
                Toast.makeText(GroupCalendar.this, "Firebase 연결 오류", Toast.LENGTH_LONG).show();
            }
        });
    }

    // 선택한 날짜의 일정 삭제하는 메서드
    private void deleteEventForDate(String date) {
        databaseReference.child(date).removeValue((databaseError, databaseReference) -> {
            if (databaseError == null) {
                // 삭제 성공 메시지 표시 또는 다른 작업 수행
                Toast.makeText(GroupCalendar.this, "일정이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                // 삭제 후 화면 업데이트 등 추가 작업 가능
            } else {
                // 삭제 오류 처리
                Toast.makeText(GroupCalendar.this, "일정 삭제 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
