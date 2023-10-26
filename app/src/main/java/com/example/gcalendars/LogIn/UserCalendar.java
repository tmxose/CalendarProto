package com.example.gcalendars.LogIn;

public class UserCalendar {
    private String calendarId;
    private String calendarName;

    public UserCalendar() {
        // Default constructor required for Firebase
    }

    public UserCalendar(String calendarId, String calendarName) {
        this.calendarId = calendarId;
        this.calendarName = calendarName;
    }

    public String getCalendarId() {
        return calendarId;
    }

    public String getCalendarName() {
        return calendarName;
    }
}
