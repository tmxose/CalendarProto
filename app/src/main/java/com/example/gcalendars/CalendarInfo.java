package com.example.gcalendars;

public class CalendarInfo {
    private final String calendarId;
    private final String groupCalendarName;

    public CalendarInfo(String calendarId, String groupCalendarName) {
        this.calendarId = calendarId;
        this.groupCalendarName = groupCalendarName;
    }

    public String getCalendarId() {
        return calendarId;
    }

    public String getGroupCalendarName() {
        return groupCalendarName;
    }
}
