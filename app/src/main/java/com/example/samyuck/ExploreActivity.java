package com.example.samyuck;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

public class ExploreActivity extends AppCompatActivity {

    private EditText searchInput;
    private LinearLayout searchResultsLayout;
    private FirebaseAuth mAuth;
    private DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore);

        searchInput = findViewById(R.id.exploreSearchInput);
        searchResultsLayout = findViewById(R.id.searchResultsLayout); // Ensure this ID exists in your XML

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference();

        // 뒤로가기 버튼
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            finish();
        });

        searchInput.setOnEditorActionListener((v, actionId, event) -> {
            String email = searchInput.getText().toString().trim();
            if (!email.isEmpty()) {
                searchUserByEmail(email);
            } else {
                Toast.makeText(this, "이메일을 입력하세요.", Toast.LENGTH_SHORT).show();
            }
            return true;
        });
    }

    private void searchUserByEmail(String email) {
        DatabaseReference usersRef = database.child("UserAccount");
        Query query = usersRef.orderByChild("emailId").equalTo(email);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                searchResultsLayout.removeAllViews();
                if (snapshot.exists()) {
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        UserAccount user = userSnapshot.getValue(UserAccount.class);
                        if (user != null) {
                            displayUserResult(user);
                        }
                    }
                } else {
                    Toast.makeText(ExploreActivity.this, "사용자를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ExploreActivity.this, "검색 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayUserResult(UserAccount user) {
        LinearLayout userLayout = new LinearLayout(this);
        userLayout.setOrientation(LinearLayout.HORIZONTAL);
        userLayout.setPadding(16, 16, 16, 16);

        TextView userName = new TextView(this);
        userName.setText(user.getName());
        userName.setTextSize(18);
        userName.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        Button requestButton = new Button(this);
        requestButton.setText("친구 요청");
        requestButton.setOnClickListener(v -> sendFriendRequest(user.getIdToken()));

        userLayout.addView(userName);
        userLayout.addView(requestButton);

        searchResultsLayout.addView(userLayout);
    }

    private void sendFriendRequest(String toUserId) {
        String fromUserId = mAuth.getCurrentUser().getUid();
        FriendRequest friendRequest = new FriendRequest(fromUserId, toUserId, "pending");

        DatabaseReference friendRequestsRef = database.child("friend_requests").push();
        friendRequestsRef.setValue(friendRequest).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "친구 요청을 보냈습니다.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "친구 요청을 보내는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
