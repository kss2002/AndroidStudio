package com.example.samyuck;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;

public abstract class BaseActivity extends AppCompatActivity {
    protected LinearLayout bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());

        // 하단 네비게이션 설정
        bottomNavigation = findViewById(R.id.bottomNavigation);
        if (bottomNavigation != null) {
            // 피드 탭
            LinearLayout feedNav = findViewById(R.id.feedNav);
            if (feedNav != null) {
                feedNav.setOnClickListener(v -> {
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                });
            }

            // 둘러보기 탭
            LinearLayout exploreNav = findViewById(R.id.exploreNav);
            if (exploreNav != null) {
                exploreNav.setOnClickListener(v -> {
                    startActivity(new Intent(this, FriendActivity.class));
                    finish();
                });
            }

            // 친구 탭
            LinearLayout friendNav = findViewById(R.id.friendNav);
            if (friendNav != null) {
                friendNav.setOnClickListener(v -> {
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                });
            }
        }
    }

    // 각 액티비티에서 구현해야 할 메서드들
    protected abstract int getLayoutId();
} 