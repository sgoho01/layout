package me.ghsong.onechat.login

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import me.ghsong.onechat.BuildConfig
import me.ghsong.onechat.R
import me.ghsong.onechat.main.MainActivity

class LoginActivity : AppCompatActivity() {

    // 버전을 보여주는 텍스트뷰
    lateinit var versionTextVIew: TextView
    // 로그인 버튼뷰
    lateinit var loginButton: Button
    // 게스트 로그인 버튼뷰
    lateinit var guestLoginButton: Button
    lateinit var constraintLayoutButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        versionTextVIew = findViewById(R.id.tvVersion)
        versionTextVIew.text = "(v${BuildConfig.VERSION_NAME})"

        loginButton = findViewById(R.id.btnLogin)
        guestLoginButton = findViewById(R.id.btnGuest)

        loginButton.setOnClickListener {
            // 특정 url로 이동한다.
            var startWebIntent = Intent(Intent.ACTION_VIEW)
            startWebIntent.data = Uri.parse("https://www.kmong.com")
            startActivity(startWebIntent)

            /*// 로그인 액티비티 -> 메인 액티비티를 실행한다.
            var startMainActivityIntent = Intent(this@LoginActivity, MainActivity::class.java)
            startActivity(startMainActivityIntent)*/

        }

        guestLoginButton.setOnClickListener(object: View.OnClickListener{
            override fun onClick(v: View?) {
                // 전화 다이얼로 이동한다.
                var startDialIntent = Intent(Intent.ACTION_DIAL)
                startDialIntent.data = Uri.parse("tel:01011111111")
                startActivity(startDialIntent)
            }
        })

        constraintLayoutButton = findViewById(R.id.btnConstraint)
        constraintLayoutButton.setOnClickListener {
            var constraintLayoutIntent = Intent(this, Login2Activity::class.java)
            startActivity(constraintLayoutIntent)
        }
    }
}
