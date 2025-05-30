package com.example.samyuck

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // 2초 후에 로그인 화면으로 이동
        Handler(Looper.getMainLooper()).postDelayed({
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                // 이미 로그인 되어 있으면 메인으로
                Log.d("Auth", "사용자가 로그인되어 있음: ${user.uid}")
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("userId", user.uid)
                startActivity(intent)
            } else {
                // 아니면 로그인 화면으로
                Log.d("Auth", "로그인 필요")
                startActivity(Intent(this, LoginActivity::class.java))
            }
            finish() // 현재 액티비티 종료
        }, 2000)
    }
}