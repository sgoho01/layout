package me.ghsong.onechat

import android.app.Application
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class ChatApplication : Application() {
    lateinit var auth: FirebaseAuth
    var user: FirebaseUser? = null

    override fun onCreate() {
        super.onCreate()

        // 파이어베이스 인증
        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        user = auth.currentUser
    }

    /**
     * 로그아웃
     */
    fun singOut() {
        auth.signOut()
    }

}