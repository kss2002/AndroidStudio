package com.example.samyuck

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class InputLogActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inputlog)

        // 뒤로가기 버튼 클릭 리스너 설정
        findViewById<View>(R.id.backButton).setOnClickListener {
            finish() // 현재 액티비티 종료하고 이전 화면으로 돌아가기
        }
    }

    // 시스템 뒤로가기 버튼 처리
    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}