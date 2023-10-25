package com.example.gcalendars.custom;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gcalendars.R;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CustomCalendar extends AppCompatActivity implements CalendarAdapter.OnItemListener {

    private final String collectionName = "CustomCalendar";

    private TextView monthYearText;
    private RecyclerView calendarRecyclerView;
    private LocalDate selectedDate;
    private TextView dateTextView; // 추가된 TextView
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private String title; // 이벤트 제목
    private String strDate; // 이벤트 날짜
    private String content; // 이벤트 내용
    private String privacy; // 이벤트 프라이버시

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendar_custom);
        initWidgets();
        selectedDate = LocalDate.now();
        setMonthView();

        Button addButton = findViewById(R.id.buttonAdd);
        Button deleteButton = findViewById(R.id.deleteBtn);
        Button editButton = findViewById(R.id.editButton);

        // "일정 추가" 버튼 클릭 이벤트 처리
        addButton.setOnClickListener(v -> {
            // 선택한 날짜를 인텐트에 추가
            Intent intent = new Intent(CustomCalendar.this, AddEvent.class);
            intent.putExtra("selectedDate", selectedDate.format(DateTimeFormatter.ofPattern("yyyy MM dd")));
            startActivity(intent); // AddEvent 액티비티 시작
        });
        // "일정 삭제" 버튼 클릭 이벤트 처리
        deleteButton.setOnClickListener(v -> deleteEventForDate(selectedDate.format(DateTimeFormatter.ofPattern("yyyy MM dd"))));
        // 버튼 클릭 이벤트에서 다이얼로그를 표시
        editButton.setOnClickListener(v -> {
            EditEventDialog editDialog = new EditEventDialog(this, title, strDate, content, privacy);
            // 이벤트가 업데이트되면 여기에서 Firestore에 업데이트하는 로직을 수행
            editDialog.setOnEventUpdatedListener(this::updateEvent);
            editDialog.show();
        });
    }

    // 이벤트를 업데이트하는 메서드
    private void updateEvent(String updatedTitle, String updatedDate, String updatedContent, String updatedPrivacy) {
        DocumentReference eventRef = db.collection(collectionName).document();
        Map<String, Object> data = new HashMap<>();
        data.put("title", updatedTitle);
        data.put("date", updatedDate);
        data.put("content", updatedContent);
        data.put("privacy", updatedPrivacy);

        eventRef.set(data)
                .addOnSuccessListener(aVoid -> Toast.makeText(CustomCalendar.this, "일정이 업데이트되었습니다.", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(CustomCalendar.this, "일정 업데이트에 실패했습니다.", Toast.LENGTH_SHORT).show());
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

        for (int i = 1; i <= 42; i++) { // 0부터 41까지
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

    @Override
    public void onItemClick(int position, String dayText) {
        // 캘린더의 날짜를 클릭했을 때 호출되는 메서드입니다.
        if (!dayText.equals("")) {
            int dayOfMonth = Integer.parseInt(dayText);
            selectedDate = selectedDate.withDayOfMonth(dayOfMonth);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy MM dd");
            String formattedDate = selectedDate.format(formatter);
            dateTextView.setText(formattedDate);
            // 선택한 날짜에 대한 이벤트를 가져와 표시
            displayEventForDate(formattedDate);
        }
    }

    // Firebase에서 해당 날짜의 일정을 가져와 표시하는 메서드
    private void displayEventForDate(final String date) {
        db.collection(collectionName).whereEqualTo("date", date)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            title = document.getString("title");
                            privacy = document.getString("privacy");
                            strDate = document.getString("date");
                            content = Objects.requireNonNull(document.get("content")).toString();

                            if (title != null && !title.isEmpty()) {
                                runOnUiThread(() -> {
                                    TextView dateTitle = findViewById(R.id.textDateTitle);
                                    dateTitle.setText(title);
                                    TextView dateContent = findViewById(R.id.textContent);
                                    if (content != null) {
                                        // HTML 줄 바꿈 태그로 개행 문자 처리
                                        content = content.replace("\n", "<br>");
                                        dateContent.setText(Html.fromHtml(content, Html.FROM_HTML_MODE_LEGACY));
                                    } else {
                                        dateContent.setText("내용 없음");
                                    }
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


    // 선택한 날짜의 일정 삭제하는 메서드
    private void deleteEventForDate(String date) {
        db.collection(collectionName).whereEqualTo("date", date)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            document.getReference().delete();
                            Toast.makeText(CustomCalendar.this, "일정을 삭제 했습니다.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                }).addOnFailureListener(e -> {
                    // 삭제 오류 처리
                    Toast.makeText(CustomCalendar.this, "일정 삭제 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                });

    }

}