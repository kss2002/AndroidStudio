package com.example.samyuck

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // 게스트 버튼 클릭 리스너 설정
        findViewById<View>(R.id.guestButton).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    fun onSignUpClick(view: View) {
        val intent = Intent(this, SignUpActivity::class.java)
        startActivity(intent)
    }

    fun onLoginClick(view: View) {
        val intent = Intent(this, InputLogActivity::class.java)
        startActivity(intent)
    }
}