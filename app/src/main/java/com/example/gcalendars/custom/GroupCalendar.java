package com.example.gcalendars.custom;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
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

public class GroupCalendar extends AppCompatActivity implements CalendarAdapter.OnItemListener {

    private TextView monthYearText;
    private RecyclerView calendarRecyclerView;
    private LocalDate selectedDate;
    private TextView dateTextView; // 추가된 TextView
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    // Firestore 컬렉션 레퍼런스 설정
    private final String collectionName = "CustomCalendar";
    private String eventId; // 이벤트 ID
    private String title; // 이벤트 제목
    private String strDate; // 이벤트 날짜
    private String content; // 이벤트 내용
    private String privacy; // 이벤트 프라이버시
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

        // 버튼 클릭 이벤트에서 다이얼로그를 표시
        Button editButton = findViewById(R.id.editButton);
        editButton.setOnClickListener(v -> {
            EditEventDialog editDialog = new EditEventDialog(this, eventId, title, strDate, content, privacy);
            // 이벤트가 업데이트되면 여기에서 Firestore에 업데이트하는 로직을 수행
            editDialog.setOnEventUpdatedListener(this::updateEvent);
            editDialog.show();
        });


    }

    // 이벤트를 업데이트하는 메서드
    private void updateEvent(String eventId, String updatedTitle, String updatedDate, String updatedContent, String updatedPrivacy) {
        DocumentReference eventRef = db.collection(collectionName).document(eventId);
        Map<String, Object> data = new HashMap<>();
        data.put("title", updatedTitle);
        data.put("date", updatedDate);
        data.put("content", updatedContent);
        data.put("privacy", updatedPrivacy);

        eventRef.set(data)
                .addOnSuccessListener(aVoid -> Toast.makeText(GroupCalendar.this, "일정이 업데이트되었습니다.", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(GroupCalendar.this, "일정 업데이트에 실패했습니다.", Toast.LENGTH_SHORT).show());
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
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue();

        boolean skipped = false; // 0번째 줄을 생략하기 위한 플래그

        for (int i = 1; i <= 42; i++) {
            if (i <= dayOfWeek) {
                daysInMonthArray.add(""); // 이전 달의 빈 공간
                skipped = true;
            } else if (i > daysInMonth + dayOfWeek) {
                daysInMonthArray.add(""); // 다음 달의 빈 공간
            } else {
                int dayOfMonth = i - dayOfWeek; // 날짜를 채웁니다.
                daysInMonthArray.add(String.valueOf(dayOfMonth));
            }

            // 0번째 줄이 모두 공백인 경우 다음 줄을 표시하기 위해 플래그를 사용
            if (skipped && !daysInMonthArray.get(i - 1).isEmpty()) {
                skipped = false; // 플래그를 리셋
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
        // date 필드와 일치하는 문서를 쿼리합니다.
        db.collection(collectionName).whereEqualTo("date", date)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            eventId = document.getId(); // 이벤트 ID 가져오기
                            title = document.getString("title"); // 이벤트 제목 가져오기
                            content = document.getString("content"); // 이벤트 내용 가져오기
                            privacy = document.getString("privacy"); // 이벤트 프라이버시 가져오기
                            strDate = document.getString("date");

                            if (title != null && !title.isEmpty()) {
                                // 해당 날짜에 일정이 있는 경우 텍스트 뷰에 표시
                                TextView dateTitle = findViewById(R.id.textDateTitle);
                                dateTitle.setText(title);
                                return; // 일치하는 첫 번째 문서를 찾았으므로 종료
                            }
                        }
                        // 해당 날짜에 일정이 없는 경우 처리
                        TextView dateTitle = findViewById(R.id.textDateTitle);
                        dateTitle.setText("일정 없음");
                    } else {
                        // 오류 처리 (예: Firebase 연결 오류)
                        Toast.makeText(GroupCalendar.this, "Firebase 연결 오류", Toast.LENGTH_LONG).show();
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
                            Toast.makeText(GroupCalendar.this, "일정을 삭제 했습니다.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                }).addOnFailureListener(e -> {
                    // 삭제 오류 처리
                    Toast.makeText(GroupCalendar.this, "일정 삭제 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                });

    }

}