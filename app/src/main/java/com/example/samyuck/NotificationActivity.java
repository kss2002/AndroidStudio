package com.example.samyuck;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class NotificationActivity extends AppCompatActivity {
    private LinearLayout notificationLayout;
    private FirebaseAuth mAuth;
    private DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        // XML 상의 contentLayout을 참조
        notificationLayout = findViewById(R.id.contentLayout);
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference();

        loadNotifications();
    }

    private void loadNotifications() {
        String currentUserId = mAuth.getCurrentUser().getUid();
        DatabaseReference notiRef = database.child("notifications").child(currentUserId);

        notiRef.orderByChild("timestamp").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                notificationLayout.removeAllViews();

                if (!snapshot.exists()) {
                    TextView empty = new TextView(NotificationActivity.this);
                    empty.setText("알림이 없습니다.");
                    empty.setTextSize(16);
                    empty.setPadding(16, 16, 16, 16);
                    empty.setTextColor(Color.GRAY);
                    notificationLayout.addView(empty);
                    return;
                }

                for (DataSnapshot notiSnap : snapshot.getChildren()) {
                    String message = notiSnap.child("message").getValue(String.class);
                    Boolean read = notiSnap.child("read").getValue(Boolean.class);

                    TextView textView = new TextView(NotificationActivity.this);
                    textView.setText(message);
                    textView.setTextSize(16);
                    textView.setPadding(16, 16, 16, 16);
                    textView.setBackgroundColor(Color.parseColor("#F9F9F9"));
                    textView.setTextColor((read != null && !read) ? Color.RED : Color.DKGRAY);

                    textView.setOnClickListener(v -> {
                        notiSnap.getRef().child("read").setValue(true);
                        textView.setTextColor(Color.GRAY);
                    });

                    notificationLayout.addView(textView);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // 에러 처리 로그 또는 사용자 알림
            }
        });
    }
}
