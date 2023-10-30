package com.example.gcalendars;

import androidx.annotation.NonNull;

public class Friend {
    private String friendID;
    private String friendName;
    private boolean isAccepted;

    public Friend() {
        // Default constructor required for Firebase
    }

    public Friend(String friendID, String friendName, boolean isAccepted) {
        this.friendID = friendID;
        this.friendName = friendName;
        this.isAccepted = isAccepted;
    }

    public String getFriendID() {
        return friendID;
    }

    // 이 toString 메서드를 추가하여 친구 객체의 이름을 반환합니다.
    @NonNull
    @Override
    public String toString() {
        return friendName;
    }

    public String getFriendName() {
        return friendName;
    }

    public boolean isAccepted() { // 수정: 반환 형식을 boolean으로 변경
        return isAccepted;
    }
}

