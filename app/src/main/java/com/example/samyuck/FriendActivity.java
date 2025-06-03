package com.example.samyuck;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

public class FriendActivity extends AppCompatActivity {

    private LinearLayout contentLayout;
    private Button tabFriends, tabRequests;

    private FirebaseAuth mAuth;
    private DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_list);

        tabFriends = findViewById(R.id.tabFriends);
        tabRequests = findViewById(R.id.tabRequests);
        contentLayout = findViewById(R.id.contentLayout);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference();

        tabFriends.setOnClickListener(v -> {
            setActiveTab(tabFriends, tabRequests);
            loadFriendList();
        });

        tabRequests.setOnClickListener(v -> {
            setActiveTab(tabRequests, tabFriends);
            loadFriendRequests();
        });

        // 기본 탭은 친구 목록
        setActiveTab(tabFriends, tabRequests);
        loadFriendList();
        LinearLayout feedNav = findViewById(R.id.feedNav);
        LinearLayout exploreNav = findViewById(R.id.exploreNav);

        feedNav.setOnClickListener(v -> {
            Intent intent = new Intent(FriendActivity.this, MainActivity.class);
            startActivity(intent);
        });
        exploreNav.setOnClickListener(v -> {
            Intent intent = new Intent(FriendActivity.this, ExploreActivity.class);
            startActivity(intent);
        });


    }

    private void setActiveTab(Button active, Button inactive) {
        active.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.purple_200)));
        active.setTextColor(Color.WHITE);
        inactive.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#DDDDDD")));
        inactive.setTextColor(Color.BLACK);
    }

    private void loadFriendList() {
        contentLayout.removeAllViews();
        String currentUserId = mAuth.getCurrentUser().getUid();
        DatabaseReference friendsRef = database.child("friends").child(currentUserId);

        friendsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    addText("친구가 없습니다.");
                    return;
                }

                for (DataSnapshot friendSnapshot : snapshot.getChildren()) {
                    String friendId = friendSnapshot.getKey();

                    database.child("UserAccount").child(friendId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            UserAccount friend = snapshot.getValue(UserAccount.class);
                            if (friend != null) {
                                // uid 넘김으로써 클릭 시 일정 이동 가능
                                addUserItem(friend.getName(), friendId, null);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(FriendActivity.this, "친구 불러오기 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadFriendRequests() {
        contentLayout.removeAllViews();
        String currentUserId = mAuth.getCurrentUser().getUid();
        DatabaseReference requestsRef = database.child("friend_requests");

        requestsRef.orderByChild("toUserId").equalTo(currentUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            addText("받은 요청이 없습니다.");
                            return;
                        }

                        for (DataSnapshot requestSnapshot : snapshot.getChildren()) {
                            FriendRequest request = requestSnapshot.getValue(FriendRequest.class);
                            if (request != null && "pending".equals(request.getStatus())) {
                                String fromUserId = request.getFromUserId();

                                database.child("UserAccount").child(fromUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        UserAccount sender = snapshot.getValue(UserAccount.class);
                                        if (sender != null) {
                                            addUserItem(sender.getName(), null, () -> acceptRequest(requestSnapshot.getKey(), fromUserId));
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {}
                                });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(FriendActivity.this, "요청 불러오기 실패", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void acceptRequest(String requestId, String fromUserId) {
        String currentUserId = mAuth.getCurrentUser().getUid();

        DatabaseReference ref = database;

        // 친구 관계 양방향 추가
        ref.child("friends").child(currentUserId).child(fromUserId).setValue(true);
        ref.child("friends").child(fromUserId).child(currentUserId).setValue(true);

        // 요청 상태 변경
        ref.child("friend_requests").child(requestId).child("status").setValue("accepted");

        Toast.makeText(this, "친구 요청을 수락했습니다.", Toast.LENGTH_SHORT).show();
        loadFriendRequests(); // 목록 새로고침
    }

    private void addUserItem(String name, String uid, Runnable onAccept) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(8, 16, 8, 16);
        row.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        ImageView profileImage = new ImageView(this);
        profileImage.setImageResource(R.drawable.default_profile);
        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(100, 100);
        imageParams.setMargins(0, 0, 32, 0);
        profileImage.setLayoutParams(imageParams);

        TextView nameText = new TextView(this);
        nameText.setText(name);
        nameText.setTextSize(16);
        nameText.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        // 이름 클릭 시 친구 일정 보기
        if (uid != null) {
            nameText.setOnClickListener(v -> {
                Intent intent = new Intent(FriendActivity.this, MainActivity.class);
                intent.putExtra("targetUserId", uid);
                intent.putExtra("targetName", name);
                startActivity(intent);
            });
        }

        row.addView(profileImage);  // 이미지 먼저
        row.addView(nameText);      // 이름 다음

        if (onAccept != null) {
            Button acceptBtn = new Button(this);
            acceptBtn.setText("수락");
            acceptBtn.setOnClickListener(v -> onAccept.run());
            row.addView(acceptBtn);
        }

        contentLayout.addView(row);
    }


    private void addText(String message) {
        TextView text = new TextView(this);
        text.setText(message);
        text.setTextSize(16);
        text.setPadding(16, 16, 16, 16);
        contentLayout.addView(text);
    }
}
