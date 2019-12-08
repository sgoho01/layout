package me.ghsong.onechat.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import me.ghsong.onechat.BuildConfig
import me.ghsong.onechat.R
import me.ghsong.onechat.SharedPreferencesUtil
import me.ghsong.onechat.databinding.ActivityLogin2Binding
import me.ghsong.onechat.main.MainActivity

class Login2Activity : AppCompatActivity() {
    lateinit var binding: ActivityLogin2Binding
    private lateinit var auth: FirebaseAuth
    lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login2)

        // 파이어베이스 인증
        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)


        binding.tvVersion2.text = "(v${BuildConfig.VERSION_NAME})"


        binding.btnLogin2.setOnClickListener {
            signIn()
            /*// 로그인을 저장한다.
            SharedPreferencesUtil.putIsLogin(this@Login2Activity, true)

            // 결과를 설정한다.
            setResult(Activity.RESULT_OK)

            var startMainActivityIntent = Intent(this@Login2Activity, MainActivity::class.java)
            startActivity(startMainActivityIntent)

            // 로그인 액티비티를 종료한다.
            finish()*/
        }


        binding.btnGuest2.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                SharedPreferencesUtil.putIsLogin(this@Login2Activity, false)

                //  메인 액티비티 실행
                var startMainActivityIntent = Intent(this@Login2Activity, MainActivity::class.java)
                startActivity(startMainActivityIntent)

                // 액티비티 종료
                finish()
            }
        })

    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser

        //updateUI(currentUser)
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)
                // ...
            }
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.id!!)

        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // 성공
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    //updateUI(user)

                    // 로그인을 저장한다.
                    SharedPreferencesUtil.putIsLogin(this@Login2Activity, true)

                    // 결과를 설정한다.
                    setResult(Activity.RESULT_OK)
                    finish()
                } else {
                    // 실패
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Snackbar.make(binding.root, "Authentication Failed.", Snackbar.LENGTH_SHORT).show()
                    //updateUI(null)
                }

                // ...
            }
    }

    companion object {
        const val REQUEST_CODE = 1
        const val TAG = "LOGIN_ACTIVITY"
        const val RC_SIGN_IN = 100
    }
}
