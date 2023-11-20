package com.example.gcalendars;

import static androidx.constraintlayout.widget.Constraints.TAG;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FriendsActivity extends AppCompatActivity {

    private EditText editTextFriendEmail;
    private static DatabaseReference databaseReference;
    private static String currentUserID;
    private final ArrayList<Friend> friendsList = new ArrayList<>();
    private final ArrayList<Friend> friendRequestsList = new ArrayList<>();
    private ArrayAdapter<Friend> adapter;
    private ArrayAdapter<Friend> requestsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        // 레이아웃 요소 초기화
        editTextFriendEmail = findViewById(R.id.editTextFriendEmail);
        Button buttonAddFriend = findViewById(R.id.buttonAddFriend);
        ListView listViewFriends = findViewById(R.id.listViewFriends);
        ListView listViewFriendRequests = findViewById(R.id.listViewFriendRequests);

        // Firebase 초기화
        databaseReference = FirebaseDatabase.getInstance().getReference();
        currentUserID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        // 어댑터 초기화
        adapter = new ArrayAdapter<>(this, R.layout.list_item_friend, friendsList);
        listViewFriends.setAdapter(adapter);
        requestsAdapter = new ArrayAdapter<>(this, R.layout.list_item_friend, friendRequestsList);
        listViewFriendRequests.setAdapter(requestsAdapter);

        // 버튼 이벤트 처리
        buttonAddFriend.setOnClickListener(view -> onAddFriendClick());
        listViewFriends.setOnItemClickListener((parent, view, position, id) -> onFriendItemClick(position));
        listViewFriendRequests.setOnItemClickListener((parent, view, position, id) -> onFriendRequestItemClick(position));

        // 친구 목록 및 친구 요청 목록 업데이트
        loadFriendsList();
        loadFriendRequests();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFriendsList();
        loadFriendRequests();
    }
    
    // '추가' 버튼 클릭 시 호출
    private void onAddFriendClick() {
        String friendEmail = editTextFriendEmail.getText().toString();
        if (!friendEmail.isEmpty()) {
            findFriendAndSendRequest(friendEmail);
        } else {
            Toast.makeText(this, "이메일을 입력하세요.", Toast.LENGTH_SHORT).show();
        }
    }

    // 친구 목록에서 항목 클릭 시 호출
    private void onFriendItemClick(int position) {
        Friend friend = friendsList.get(position);
        showFriendInfoDialog(friend);
    }

    // 친구 요청 목록에서 항목 클릭 시 호출
    private void onFriendRequestItemClick(int position) {
        Friend friend = friendRequestsList.get(position);
        showFriendRequestDialog(friend);
    }

    // email을 통한 Firebase의 사용자 유무 판별 및 이미 추가된 친구인지 확인하는 함수
    private void findFriendAndSendRequest(String friendEmail) {
        DatabaseReference usersRef = databaseReference.child("users");
        usersRef.orderByChild("email").equalTo(friendEmail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean isFriendAlreadyAdded = false; // 중복 추가 여부를 나타내는 플래그
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String friendID = userSnapshot.getKey();
                    String friendName = userSnapshot.child("username").getValue(String.class);
                    if (friendID != null) {
                        // 이미 추가된 친구인지 확인
                        if (isFriendAlreadyAdded(friendID)) {
                            Toast.makeText(FriendsActivity.this, friendName + "님은 이미 추가된 친구입니다.", Toast.LENGTH_SHORT).show();
                        } else {
                            sendFriendRequest(friendID, friendName); 
                        }
                        isFriendAlreadyAdded = true;
                    }
                }

                if (!isFriendAlreadyAdded) {
                    Toast.makeText(FriendsActivity.this, "친구를 찾을 수 없습니다: " + friendEmail, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(FriendsActivity.this, "친구 찾기 실패: " + friendEmail, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 이미 추가된 친구인지 확인하는 메서드
    private boolean isFriendAlreadyAdded(String friendID) {
        for (Friend friend : friendsList) {
            if (friendID.equals(friend.getFriendID())) {
                return true;
            }
        }
        return false;
    }

    // realtime database에 친구요청 관련 함수
    private void sendFriendRequest(String friendID, String friendName) {
        DatabaseReference friendRequestsRef = databaseReference.child("users").child(friendID).child("friend-requests");

        // getCurrentUserName 함수를 호출하여 사용자 이름을 가져온 후, 이를 사용하여 친구 요청을 보냅니다.
        getCurrentUserName(new CurrentUserNameCallback() {
            @Override
            public void onUserNameFetched(String username) {
                if (username != null) {
                    friendRequestsRef.child(currentUserID).child("friendName").setValue(username); // 요청에 친구의 이름을 추가
                    Toast.makeText(FriendsActivity.this, friendName + "님에게 친구 요청을 보냈습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(FriendsActivity.this, "사용자 이름이 없습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(FriendsActivity.this, "사용자 이름을 가져오는 동안 오류가 발생했습니다: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 인터페이스를 정의하여 사용자 이름을 가져오는 콜백을 처리
    private interface CurrentUserNameCallback {
        void onUserNameFetched(String username);

        void onError(String errorMessage);
    }

    // 유저이름이 정상적으로 존재하는지 확인하여 값을 가져오는 함수
    private void getCurrentUserName(CurrentUserNameCallback callback) {
        DatabaseReference userRef = databaseReference.child("users").child(currentUserID).child("username");
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String username = dataSnapshot.getValue(String.class);
                if (username != null) {
                    Log.d("CurrentUserName", "Current user name: " + username);
                    callback.onUserNameFetched(username);
                    // 여기에서 요청 보내는 로직을 호출하거나, 필요한 처리를 수행할 수 있습니다.
                } else {
                    Log.d("CurrentUserName", "User is not logged in or does not have a username.");
                    callback.onError("User is not logged in or does not have a username.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("CurrentUserName", "Error fetching current user name: " + databaseError.getMessage());
                callback.onError(databaseError.getMessage());
                // 데이터베이스에서 사용자 이름을 가져오는 동안 오류가 발생한 경우의 처리
            }
        });
    }

    // 친구 클릭시에 출력되는 다이얼로그 함수
    private void showFriendInfoDialog(Friend friend) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("친구 정보");
        builder.setMessage("이름: " + friend.getFriendName());

        builder.setPositiveButton("공유하기", (dialog, which) -> {

            String selectedFriendID = friend.getFriendID();

            // 현재 사용자의 그룹 캘린더 정보를 가져옵니다
            DatabaseReference currentGroupCalendarRef = databaseReference.child("users").child(currentUserID).child("group-calendar");
            // 캘린더 정보를 담을 리스트
            List<CalendarInfo> calendarList = new ArrayList<>();

            // 사용자의 캘린더 정보를 가져오고 리스트에 추가
            currentGroupCalendarRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot calendarSnapshot : dataSnapshot.getChildren()) {
                        String calendarId = calendarSnapshot.getKey();
                        String groupCalendarName = calendarSnapshot.child("group-calendarName").getValue(String.class);

                        if (calendarId != null && groupCalendarName != null) {
                            // CalendarInfo 객체를 생성하여 리스트에 추가
                            CalendarInfo calendarInfo = new CalendarInfo(calendarId, groupCalendarName);
                            calendarList.add(calendarInfo);
                        }
                    }

                    // 리스트를 표시하는 다이얼로그를 띄우는 함수 호출
                    showCalendarListDialog(calendarList, selectedFriendID);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(FriendsActivity.this, "캘린더 정보를 가져오는 동안 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                }
            });

        });


        builder.setNegativeButton("닫기", (dialog, which) -> dialog.dismiss());
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        // 버튼 스타일 조정
        Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setTextColor(ContextCompat.getColor(this, R.color.black)); // R.color.black는 원하는 색상 리소스로 변경
        Button negativeButton = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        negativeButton.setTextColor(ContextCompat.getColor(this, R.color.black)); // R.color.black는 원하는 색상 리소스로 변경
    }

    // 캘린더 공유 함수
    private void showCalendarListDialog(List<CalendarInfo> calendarList, String selectedFriendID) {
        // 다이얼로그에 표시할 항목 배열
        String[] calendarNames = new String[calendarList.size()];
        for (int i = 0; i < calendarList.size(); i++) {
            calendarNames[i] = calendarList.get(i).getGroupCalendarName();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("캘린더 선택");
        builder.setItems(calendarNames, (dialog, which) -> {
            // 사용자가 선택한 항목의 정보를 가져와서 그룹을 공유하는 함수 호출
            CalendarInfo selectedCalendar = calendarList.get(which);
            String selectedCalendarId = selectedCalendar.getCalendarId();
            String selectedCalendarName = selectedCalendar.getGroupCalendarName();
            shareSelectedCalendar(selectedCalendarId, selectedCalendarName, selectedFriendID);
        });

        builder.setNegativeButton("닫기", (dialog, which) -> dialog.dismiss());
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    // 캘린더 공유 정보 전달하는 함수
    private void shareSelectedCalendar(String calendarId, String calendarName, String selectedFriendID) {
        String shareMessage = "선택한 캘린더 " + calendarName + " 를 상대방에게 공유합니다.";
        Toast.makeText(this, shareMessage, Toast.LENGTH_SHORT).show();
        sendShareRequestToFriend(selectedFriendID, calendarName, calendarId);
    }

    // 상대방에게 캘린더를 공유하는 작업 수행
    private void sendShareRequestToFriend(String friendID, String groupCalendarName, String groupID) {
        DatabaseReference friendRequestsRef = databaseReference.child("users").child(friendID).child("group-calendar-requests").child(groupID);

        // 요청 정보를 생성하여 저장
        friendRequestsRef.child("groupID").setValue(groupID);
        friendRequestsRef.child("groupCalendarName").setValue(groupCalendarName);
        friendRequestsRef.child("status").setValue("pending"); // 요청 상태를 "대기 중"으로 설정

        // 사용자의 이름 가져오기
        DatabaseReference userNameRef = databaseReference.child("users").child(currentUserID).child("username");
        userNameRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String userName = dataSnapshot.getValue(String.class);
                if (userName != null) {
                    friendRequestsRef.child("username").setValue(userName);
                    Toast.makeText(FriendsActivity.this, "그룹 캘린더 공유 요청을 보냈습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(FriendsActivity.this, "사용자 이름을 가져오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(FriendsActivity.this, "사용자 이름을 가져오는 동안 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 친구 요청목록 리스트의 레이아웃 클릭시 호출 함수
    private void showFriendRequestDialog(Friend friend) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("친구 요청");
        builder.setMessage(friend.getFriendName() + "님으로부터 친구 요청이 도착했습니다. 수락하시겠습니까?");
        builder.setPositiveButton("수락", (dialog, which) -> acceptFriendRequest(friend));
        builder.setNegativeButton("거절", (dialog, which) -> rejectFriendRequest(friend));

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        // 버튼 스타일 조정
        Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setTextColor(ContextCompat.getColor(this, R.color.black)); // R.color.black는 원하는 색상 리소스로 변경
        Button negativeButton = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        negativeButton.setTextColor(ContextCompat.getColor(this, R.color.black)); // R.color.black는 원하는 색상 리소스로 변경
    }

    // 친구 요청 수락 함수
    private void acceptFriendRequest(Friend friend) {
        String friendId = friend.getFriendID();
        String friendName = friend.getFriendName();

        DatabaseReference currentUserFriendsRef = databaseReference.child("users").child(currentUserID).child("friends").child(friendId);
        currentUserFriendsRef.child("friendId").setValue(friendId);
        currentUserFriendsRef.child("friendName").setValue(friendName);

        // 친구 목록 업데이트
        updateFriendsList(friend);

        // 추가: 친구 목록에 친구를 추가하도록 수정
        friendsList.add(new Friend(friend.getFriendID(), friend.getFriendName(), true));
        adapter.notifyDataSetChanged();

        // 상대방의 친구 목록에도 자동으로 추가
        DatabaseReference friendFriendsRef = databaseReference.child("users").child(friendId).child("friends").child(currentUserID);
        friendFriendsRef.child("friendId").setValue(currentUserID);

        // 사용자 이름 가져오기
        DatabaseReference currentUserNameRef = databaseReference.child("users").child(currentUserID).child("username");
        currentUserNameRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String currentUserName = dataSnapshot.getValue(String.class);
                if (currentUserName != null) {
                    friendFriendsRef.child("friendName").setValue(currentUserName);
                    Toast.makeText(FriendsActivity.this, "친구 요청을 수락했습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(FriendsActivity.this, "사용자 이름을 가져오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(FriendsActivity.this, "사용자 이름을 가져오는 동안 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
        // 친구 요청 기록 삭제
        DatabaseReference currentUserRequestsRef = databaseReference.child("users").child(currentUserID).child("friend-requests");
        currentUserRequestsRef.child(friendId).removeValue()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "친구 요청을 수락하고 삭제했습니다.");
                    // 요청 목록에서도 제거
                    friendRequestsList.remove(friend);
                    requestsAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e(TAG, "친구 요청 수락 후 삭제 시 오류 발생: " + e.getMessage()));
    }
    // 친구 요청 거절 함수
    private void rejectFriendRequest(Friend friend) {
        DatabaseReference currentUserRequestsRef = databaseReference.child("users").child(currentUserID).child("friend-requests");
        currentUserRequestsRef.child(friend.getFriendID()).removeValue()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "친구 요청을 거절하고 삭제했습니다.");
                    // 요청 목록에서도 제거
                    friendRequestsList.remove(friend);
                    requestsAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e(TAG, "친구 요청 거부 후 삭제 시 오류 발생: " + e.getMessage()));
    }
    // 친구목록 초기화 함수
    private void loadFriendsList() {
        DatabaseReference databaseRef = databaseReference.child("users").child(currentUserID).child("friends");
        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<Friend> newFriendsList = new ArrayList<>();

                for (DataSnapshot friendSnapshot : dataSnapshot.getChildren()) {
                    String friendID = friendSnapshot.child("friendId").getValue(String.class);
                    String friendName = friendSnapshot.child("friendName").getValue(String.class);
                    if (friendID != null && friendName != null) {
                        newFriendsList.add(new Friend(friendID, friendName, true));
                    }
                    friendsList.clear();
                    friendsList.addAll(newFriendsList);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(FriendsActivity.this, "친구 목록 가져오기 실패.", Toast.LENGTH_SHORT).show();
            }
        });
    }
    // 친구 요청 리스트 초기화 함수
    private void loadFriendRequests() {
        DatabaseReference friendRequestsRef = databaseReference.child("users").child(currentUserID).child("friend-requests");
        friendRequestsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                friendRequestsList.clear();
                for (DataSnapshot requestSnapshot : dataSnapshot.getChildren()) {
                    String friendID = requestSnapshot.getKey();
                    String friendName = requestSnapshot.child("friendName").getValue(String.class);
                    if (friendName != null) {
                        // 사용자 이름을 가져오는 코드
                        DatabaseReference friendNameRef = databaseReference.child("users").child(Objects.requireNonNull(friendID)).child("username");
                        friendNameRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot nameSnapshot) {
                                String userName = nameSnapshot.getValue(String.class);
                                if (userName != null) {
                                    friendRequestsList.add(new Friend(friendID, userName, false));
                                    requestsAdapter.notifyDataSetChanged();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Toast.makeText(FriendsActivity.this, "친구의 이름을 가져오는 동안 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(FriendsActivity.this, "친구 요청 가져오기 실패.", Toast.LENGTH_SHORT).show();
            }
        });
    }
    // 자신의 친구목록 업데이트 관련 함수
    private void updateFriendsList(Friend friend) {
        // 본인의 친구 목록 업데이트
        friendsList.add(new Friend(friend.getFriendID(), friend.getFriendName(), true));
        adapter.notifyDataSetChanged();
    }
    // 그룹 캘린더 초대 요청 다이얼로그 함수
    public static void showGroupCalendarRequestDialog(Context context, String friendName, String groupID, String groupCalendarName) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("그룹 캘린더 공유 요청");
        builder.setMessage(friendName + "님으로부터 그룹 캘린더 공유 요청이 도착했습니다. 수락하시겠습니까?");
        builder.setPositiveButton("수락", (dialog, which) -> acceptGroupCalendarRequest(groupID, groupCalendarName));
        builder.setNegativeButton("거절", (dialog, which) -> rejectGroupCalendarRequest(groupID));

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        // 버튼 스타일 조정
        Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setTextColor(ContextCompat.getColor(context, R.color.black)); // R.color.black는 원하는 색상 리소스로 변경
        Button negativeButton = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        negativeButton.setTextColor(ContextCompat.getColor(context, R.color.black)); // R.color.black는 원하는 색상 리소스로 변경
    }
    // 그룹 초대 수락 함수
    private static void acceptGroupCalendarRequest(String groupID, String groupCalendarName) {
        // Firebase 초기화
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        String currentUserID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        DatabaseReference currentUserRequestsRef = databaseReference.child("users").child(currentUserID).child("group-calendar-requests").child(groupID);
        currentUserRequestsRef.child("status").setValue("accepted");

        DatabaseReference friendCalendarRef = databaseReference.child("users").child(currentUserID).child("group-calendar").child(groupID);
        friendCalendarRef.child("groupId").setValue(groupID);
        friendCalendarRef.child("group-calendarName").setValue(groupCalendarName);

        // 요청 항목 삭제
        currentUserRequestsRef.removeValue()
                .addOnSuccessListener(aVoid -> Log.d(TAG, "그룹 캘린더 공유 요청을 수락하고 삭제했습니다."))
                .addOnFailureListener(e -> Log.e(TAG, "그룹 캘린더 공유 요청 수락 후 삭제 시 오류 발생: " + e.getMessage()));
        Log.d(TAG, "그룹 캘린더 공유 요청을 수락했습니다.");
    }
    // 그룹 초대 거절 함수
    private static void rejectGroupCalendarRequest(String groupID) {
        // Firebase 초기화
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        String currentUserID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        DatabaseReference currentUserRequestsRef = databaseReference.child("users").child(currentUserID).child("group-calendar-requests").child(groupID);
        currentUserRequestsRef.removeValue()
                .addOnSuccessListener(aVoid -> Log.d(TAG, "그룹 캘린더 공유 요청을 거절하고 삭제했습니다."))
                .addOnFailureListener(e -> Log.e(TAG, "그룹 캘린더 공유 요청 거절 후 삭제 시 오류 발생: " + e.getMessage()));
        Log.d(TAG, "그룹 캘린더 공유 요청을 거절했습니다.");
    }
}
