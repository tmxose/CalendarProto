package com.example.gcalendars.custom;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gcalendars.MainActivity;
import com.example.gcalendars.R;

import com.example.gcalendars.personalSettings;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CustomCalendar extends AppCompatActivity implements CalendarAdapter.OnItemListener {
    private String collectionName; // 캘린더 컬렉션 이름

    private TextView monthYearText;
    private RecyclerView calendarRecyclerView;
    private LocalDate selectedDate;
    private TextView dateTextView; // 추가된 TextView
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private String title; // 이벤트 제목
    private List<String> content; // 이벤트 내용
    private String privacy; // 이벤트 프라이버시
    private LocalDate selectedStartDate; // 시작 날짜
    private LocalDate selectedEndDate;   // 종료 날짜

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendar_custom);
        initWidgets();

        // 캘린더 아이디와 이름을 인텐트에서 받아와서 컬렉션 이름 설정
        collectionName = getIntent().getStringExtra("calendarId");
        setTitle(getIntent().getStringExtra("calendarName"));

        selectedStartDate = LocalDate.now(); // 현재 날짜를 시작 날짜로 초기화
        selectedEndDate = LocalDate.now();   // 현재 날짜를 종료 날짜로 초기화

        setMonthView();

        Button addButton = findViewById(R.id.buttonAdd);
        Button deleteButton = findViewById(R.id.deleteBtn);
        Button editButton = findViewById(R.id.editButton);
        // onCreate 메서드 내에서 변수로 시작일과 종료일 문자열 선언
        String formattedStartDate = selectedStartDate.format(DateTimeFormatter.ofPattern("yyyy MM dd"));
        String formattedEndDate = selectedEndDate.format(DateTimeFormatter.ofPattern("yyyy MM dd"));

        // "일정 추가" 버튼 클릭 이벤트 처리
        addButton.setOnClickListener(v -> {
            Intent intent = new Intent(CustomCalendar.this, AddEvent.class);
            intent.putExtra("selectedStartDate", formattedStartDate);
            intent.putExtra("selectedEndDate", formattedEndDate);
            intent.putExtra("collectionName", collectionName); // 캘린더 컬렉션 이름을 전달
            startActivity(intent); // AddEvent 액티비티 시작
        });
        // "일정 삭제" 버튼 클릭 이벤트 처리
        deleteButton.setOnClickListener(v -> deleteEventsForDateRange(formattedStartDate,formattedEndDate));

        // 버튼 클릭 이벤트에서 다이얼로그를 표시
        editButton.setOnClickListener(v -> {
            EditEventDialog editDialog = new EditEventDialog(this, title, formattedStartDate, formattedEndDate, content, privacy, collectionName);
            editDialog.show();
        });
    }

    private void initWidgets() {
        // XML 레이아웃에서 위젯을 초기화하는 메서드.
        calendarRecyclerView = findViewById(R.id.calendarRecyclerView);
        monthYearText = findViewById(R.id.monthYearTV);
        dateTextView = findViewById(R.id.textDate); // 수정된 TextView 초기화
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

    @NonNull
    private ArrayList<String> daysInMonthArray(LocalDate date) {
        ArrayList<String> daysInMonthArray = new ArrayList<>();
        YearMonth yearMonth = YearMonth.from(date);
        int daysInMonth = yearMonth.lengthOfMonth();
        LocalDate firstOfMonth = date.withDayOfMonth(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue(); // 1부터 7까지 (월요일부터 일요일)

        // 0번째 칸부터 배열 시작하도록 수정
        dayOfWeek %= 7; // dayOfWeek를 0부터 6까지로 변경

        for (int i = 0; i < 42; i++) { // 0부터 41까지
            if (i < dayOfWeek || i >= dayOfWeek + daysInMonth) {
                daysInMonthArray.add(""); // 이전 달과 다음 달의 빈 공간
            } else {
                int dayOfMonth = i - dayOfWeek + 1; // 날짜를 채웁니다.
                daysInMonthArray.add(String.valueOf(dayOfMonth));
            }
        }

        return daysInMonthArray;
    }

    // 월과 년도를 문자열로 변환
    private String monthYearFromDate(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy MM dd");
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

    @SuppressLint("SetTextI18n")
    @Override
    public void onItemClick(int position, String dayText) {
        // 캘린더의 날짜를 클릭했을 때 호출되는 메서드입니다.
        if (!dayText.equals("")) {
            int dayOfMonth = Integer.parseInt(dayText);
            if (selectedStartDate == null) {
                selectedStartDate = LocalDate.of(selectedDate.getYear(), selectedDate.getMonth(), dayOfMonth);
            } else {
                selectedEndDate = LocalDate.of(selectedDate.getYear(), selectedDate.getMonth(), dayOfMonth);
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy MM dd");
            String formattedStartDate = selectedStartDate.format(formatter);
            String formattedEndDate = selectedEndDate.format(formatter);
            dateTextView.setText(formattedStartDate + " - " + formattedEndDate);
            // 선택한 날짜 범위에 대한 이벤트를 가져와 표시
            displayEventsForDateRange(formattedStartDate, formattedEndDate);
        }
    }

    private void displayEventsForDateRange(final String startDate, final String endDate) {
        String dateRange = startDate + " - " + endDate; // 시작일과 종료일을 합친 문자열 생성

        db.collection(collectionName)
                .whereGreaterThanOrEqualTo("date", startDate)
                .whereLessThanOrEqualTo("date", endDate)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            title = document.getString("title");
                            privacy = document.getString("privacy");

                            Object contentObj = document.get("content");

                            if (contentObj != null) {
                                if (contentObj instanceof String) {
                                    content = new ArrayList<>();
                                    content.add(contentObj.toString());
                                } else if (contentObj instanceof List) {
                                    content = (List<String>) contentObj;
                                }
                            } else {
                                content = null;
                            }

                            if (title != null && !title.isEmpty()) {
                                runOnUiThread(() -> {
                                    TextView dateTitle = findViewById(R.id.textDateTitle);
                                    dateTitle.setText(title);
                                    TextView dateContent = findViewById(R.id.textContent);
                                    if (content != null) {
                                        // HTML 줄 바꿈 태그로 개행 문자 처리
                                        String contentStr = TextUtils.join("<br>", content);
                                        dateContent.setText(Html.fromHtml(contentStr, Html.FROM_HTML_MODE_LEGACY));
                                    } else {
                                        dateContent.setText("내용 없음");
                                    }
                                    dateTextView.setText(dateRange); // 날짜 범위 업데이트
                                });
                                return;
                            }
                        }
                        runOnUiThread(() -> {
                            TextView dateTitle = findViewById(R.id.textDateTitle);
                            dateTitle.setText("일정 없음");
                            TextView dateContent = findViewById(R.id.textContent);
                            dateContent.setText("내용 없음");
                        });
                    } else {
                        runOnUiThread(() -> Toast.makeText(CustomCalendar.this, "Firebase 연결 오류", Toast.LENGTH_LONG).show());
                    }
                });
    }



    // 선택한 날짜 범위에 해당하는 일정 삭제하는 메서드
    private void deleteEventsForDateRange(String startDate, String endDate) {
        db.collection(collectionName)
                .whereGreaterThanOrEqualTo("date", startDate)
                .whereLessThanOrEqualTo("date", endDate)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            document.getReference().delete();
                        }
                        Toast.makeText(CustomCalendar.this, "일정을 삭제했습니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                        Toast.makeText(CustomCalendar.this, "일정 삭제 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
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
            Intent intent = new Intent(this, personalSettings.class);
            intent.putExtra("calendarId", collectionName); // 캘린더 컬렉션 이름을 전달
            startActivity(intent); // AddEvent 액티비티 시작 // 개인정보 설정 화면으로 이동
            return true;
        } else if (id == R.id.move_to_main) {
            // "개인정보 설정" 메뉴를 눌렀을 때의 동작
            startActivity(new Intent(this, MainActivity.class)); // 개인정보 설정 화면으로 이동
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
