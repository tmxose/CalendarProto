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

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy MM dd");
    private TextView monthYearText;
    private RecyclerView calendarRecyclerView;
    private LocalDate selectedDate = LocalDate.now();
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
        collectionName = getIntent().getStringExtra("calendarId");
        setTitle(getIntent().getStringExtra("calendarName"));
        setInitialDateRange();
        setMonthView();
        setupButtons();
    }

    private void setInitialDateRange() {
        selectedStartDate = LocalDate.now();
        selectedEndDate = LocalDate.now();
        updateDateRangeText();
    }

    private void setupButtons() {
        // 버튼 클릭 이벤트 처리 추가
        Button addButton = findViewById(R.id.buttonAdd);
        Button deleteButton = findViewById(R.id.deleteBtn);
        Button editButton = findViewById(R.id.editButton);
        addButton.setOnClickListener(v -> startAddEventActivity());
        deleteButton.setOnClickListener(v -> deleteEventsForSelectedRange());
        editButton.setOnClickListener(v -> showEditEventDialog());
    }

    private void startAddEventActivity() {
        Intent intent = new Intent(CustomCalendar.this, AddEvent.class);
        intent.putExtra("selectedStartDate", formatDate(selectedStartDate));
        intent.putExtra("selectedEndDate", formatDate(selectedEndDate));
        intent.putExtra("collectionName", collectionName);
        startActivity(intent);
    }

    private void showEditEventDialog() {
        EditEventDialog editDialog = new EditEventDialog(this, title, formatDate(selectedStartDate), formatDate(selectedEndDate), content, privacy, collectionName);
        editDialog.show();
    }

    @SuppressLint("SetTextI18n")
    private void updateDateRangeText() {
        dateTextView.setText(formatDate(selectedStartDate) + " - " + formatDate(selectedEndDate));
        displayEventsForDateRange(formatDate(selectedStartDate), formatDate(selectedEndDate));
    }

    private String formatDate(LocalDate date) {
        return date.format(DateTimeFormatter.ofPattern("yyyy MM dd"));
    }

    private void deleteEventsForSelectedRange() {
        db.collection(collectionName)
                .whereGreaterThanOrEqualTo("date", formatDate(selectedStartDate))
                .whereLessThanOrEqualTo("date", formatDate(selectedEndDate))
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
        db.collection(collectionName)
                .whereGreaterThanOrEqualTo("startDate", startDate)
                .whereLessThanOrEqualTo("endDate", endDate)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            title = document.getString("title");
                            privacy = document.getString("privacy");

                            String eventDate = selectedDate.format(formatter);
                            if (isDateInRange(eventDate, startDate, endDate)) {
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
                                        dateTextView.setText(eventDate); // 선택한 날짜 표시
                                    });
                                }
                            }
                        }
                    } else {
                        runOnUiThread(() -> Toast.makeText(CustomCalendar.this, "Firebase 연결 오류", Toast.LENGTH_LONG).show());
                    }
                });
    }

    private boolean isDateInRange(String date, String startDate, String endDate) {
        LocalDate eventDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy MM dd"));
        LocalDate rangeStartDate = LocalDate.parse(startDate, DateTimeFormatter.ofPattern("yyyy MM dd"));
        LocalDate rangeEndDate = LocalDate.parse(endDate, DateTimeFormatter.ofPattern("yyyy MM dd"));

        return !eventDate.isBefore(rangeStartDate) && !eventDate.isAfter(rangeEndDate);
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
