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
import java.util.Objects;

public class CustomCalendar extends AppCompatActivity implements CalendarAdapter.OnItemListener {
    private String collectionName;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy MM dd");
    private TextView monthYearText;
    private RecyclerView calendarRecyclerView;
    private LocalDate selectedDate = LocalDate.now();
    private TextView dateTextView;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String title;
    private List<String> content;
    private String privacy;
    private LocalDate selectedStartDate;
    private LocalDate selectedEndDate;

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
        return date.format(formatter);
    }

    private void deleteEventsForSelectedRange() {
        db.collection(collectionName)
                .whereArrayContainsAny("dates", getDatesBetween(formatDate(selectedStartDate), formatDate(selectedEndDate)))
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            document.getReference().delete();
                        }
                        Toast.makeText(CustomCalendar.this, "일정을 삭제했습니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e(TAG, "Error getting documents: " + Objects.requireNonNull(task.getException()).getMessage(), task.getException());
                        Toast.makeText(CustomCalendar.this, "일정 삭제 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void initWidgets() {
        calendarRecyclerView = findViewById(R.id.calendarRecyclerView);
        monthYearText = findViewById(R.id.monthYearTV);
        dateTextView = findViewById(R.id.textDate);
    }

    private void setMonthView() {
        monthYearText.setText(monthYearFromDate(selectedDate));
        ArrayList<String> daysInMonth = daysInMonthArray(selectedDate);

        CalendarAdapter calendarAdapter = new CalendarAdapter(daysInMonth, this);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 7);
        calendarRecyclerView.setLayoutManager(layoutManager);
        calendarRecyclerView.setAdapter(calendarAdapter);
    }

    private ArrayList<String> daysInMonthArray(LocalDate date) {
        ArrayList<String> daysInMonthArray = new ArrayList<>();
        YearMonth yearMonth = YearMonth.from(date);
        int daysInMonth = yearMonth.lengthOfMonth();
        LocalDate firstOfMonth = date.withDayOfMonth(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue();
        dayOfWeek %= 7;

        for (int i = 0; i < 42; i++) {
            if (i < dayOfWeek || i >= dayOfWeek + daysInMonth) {
                daysInMonthArray.add("");
            } else {
                int dayOfMonth = i - dayOfWeek + 1;
                daysInMonthArray.add(String.valueOf(dayOfMonth));
            }
        }

        return daysInMonthArray;
    }

    private String monthYearFromDate(LocalDate date) {
        DateTimeFormatter customFormatter = DateTimeFormatter.ofPattern("yy년 MM월");
        return date.format(customFormatter);
    }

    public void previousMonthAction(View view) {
        selectedDate = selectedDate.minusMonths(1);
        setMonthView();
    }

    public void nextMonthAction(View view) {
        selectedDate = selectedDate.plusMonths(1);
        setMonthView();
    }

    @Override
    public void onItemClick(int position, String dayText) {
        if (!dayText.equals("")) {
            int dayOfMonth = Integer.parseInt(dayText);
            if (selectedStartDate == null) {
                selectedStartDate = LocalDate.of(selectedDate.getYear(), selectedDate.getMonth(), dayOfMonth);
            } else {
                selectedEndDate = LocalDate.of(selectedDate.getYear(), selectedDate.getMonth(), dayOfMonth);
            }

            String eventDate = selectedStartDate.format(formatter);
            dateTextView.setText(eventDate);
            displayEventsForDateRange(eventDate, eventDate);
        }
    }

    private void displayEventsForDateRange(final String startDate, final String endDate) {
        db.collection(collectionName)
                .whereArrayContainsAny("dates", getDatesBetween(startDate, endDate))
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
                                            String contentStr = TextUtils.join("<br>", content);
                                            dateContent.setText(Html.fromHtml(contentStr, Html.FROM_HTML_MODE_LEGACY));
                                        } else {
                                            dateContent.setText("내용 없음");
                                        }
                                    });
                                }
                            }
                        }
                    } else {
                        Exception exception = task.getException();
                        if (exception != null) {
                            Log.e(TAG, "Error getting documents: " + exception.getMessage(), exception);
                        } else {
                            Log.e(TAG, "Error getting documents: Unknown error");
                        }
                        runOnUiThread(() -> Toast.makeText(CustomCalendar.this, "데이터 가져오기 오류", Toast.LENGTH_LONG).show());
                    }
                });
    }

    private List<String> getDatesBetween(String startDate, String endDate) {
        List<String> dates = new ArrayList<>();
        LocalDate start = LocalDate.parse(startDate, formatter);
        LocalDate end = LocalDate.parse(endDate, formatter);

        while (!start.isAfter(end)) {
            dates.add(start.format(formatter));
            start = start.plusDays(1);
        }
        return dates;
    }

    private boolean isDateInRange(String date, String startDate, String endDate) {
        LocalDate eventDate = LocalDate.parse(date, formatter);
        LocalDate rangeStartDate = LocalDate.parse(startDate, formatter);
        LocalDate rangeEndDate = LocalDate.parse(endDate, formatter);

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
            Intent intent = new Intent(this, personalSettings.class);
            intent.putExtra("calendarId", collectionName);
            startActivity(intent);
            return true;
        } else if (id == R.id.move_to_main) {
            startActivity(new Intent(this, MainActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
