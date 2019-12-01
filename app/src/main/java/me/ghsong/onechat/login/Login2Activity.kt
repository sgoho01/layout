package me.ghsong.onechat.login

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import me.ghsong.onechat.BuildConfig
import me.ghsong.onechat.MainActivity
import me.ghsong.onechat.R
import me.ghsong.onechat.databinding.ActivityLogin2Binding

class Login2Activity : AppCompatActivity() {
    lateinit var binding: ActivityLogin2Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login2)

        binding.tvVersion2.text = "(v${BuildConfig.VERSION_NAME})"

        /*binding.btnLogin2.setOnClickListener {
            // 특정 url로 이동한다.
            var startWebIntent = Intent(Intent.ACTION_VIEW)
            startWebIntent.data = Uri.parse("https://www.kmong.com")
            startActivity(startWebIntent)
        }*/

        binding.btnLogin2.setOnClickListener {
            // 로그인 액티비티 -> 메인 액티비티를 실행한다.
            var startMainActivityIntent = Intent(this@Login2Activity, MainActivity::class.java)
            startActivity(startMainActivityIntent)
            // 로그인 액티비티를 종료한다.
            finish()
        }


        binding.btnGuest2.setOnClickListener(object: View.OnClickListener{
            override fun onClick(v: View?) {
                // 전화 다이얼로 이동한다.
                var startDialIntent = Intent(Intent.ACTION_DIAL)
                startDialIntent.data = Uri.parse("tel:01011111111")
                startActivity(startDialIntent)
            }
        })

    }
}
