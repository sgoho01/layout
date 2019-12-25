package me.ghsong.onechat.main

import android.app.Activity
import android.app.Notification
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.*
import com.google.gson.Gson
import me.ghsong.onechat.*
import me.ghsong.onechat.R
import me.ghsong.onechat.databinding.ActivityMainBinding
import me.ghsong.onechat.login.Login2Activity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    private var isLogin: Boolean = false
    private var chatItems = ArrayList<ChatItem>()
    private lateinit var adapter: MainChatAdapter

    private lateinit var chatRef: DatabaseReference

    // 로그인, 로그아웃 메뉴
    private var signMenu: MenuItem? = null

    // 푸시 토큰 저장 변수
    private var tokens = ArrayList<String>()

    /**
     * 푸시 토큰 정보를 설정한다,
     */
    private fun setAllToken() {
        FirebaseDatabase.getInstance().getReference("push")
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(p0: DataSnapshot) {
                    // 등록된 정보를 초기화하고 새로운 정보로 변경한다.
                    tokens.clear()

                    for (child in p0.children) {
                        var pushInfo = child.value as HashMap<String, String>

                        var email = pushInfo["email"]
                        var token = pushInfo["token"]!!

                        // 본인을 제외한 모든 토큰 정보를 추가한다.
                        var loginEmail = (application as ChatApplication).user!!.email
                        if (!email.equals(loginEmail)) tokens.add(token)
                    }
                }
            })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // firebase database setting
        val database = FirebaseDatabase.getInstance()
        chatRef = database.getReference("oneChat")

        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_main
        )

        isLogin = SharedPreferencesUtil.isLogin(this@MainActivity)
        // 토큰을 설정한다,
        if (isLogin) setAllToken()

        chatItems = getChatItemsInDatabase()

        adapter = MainChatAdapter(chatItems)
        var linearLayoutManager = LinearLayoutManager(this)
        // 구분선 추가
        var dividerItemDecoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        binding.rvChat.addItemDecoration(dividerItemDecoration)
        binding.rvChat.layoutManager = linearLayoutManager
        binding.rvChat.adapter = adapter

        // 최초 실행시 버튼 비활성화
        binding.btnSend.isEnabled = false

        // Edit의 텍스트 변화를 감지 체크 추가
        binding.etInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.btnSend.isEnabled = !s.isNullOrEmpty()
            }
        })

        binding.btnSend.setOnClickListener {
            if (!isLogin) {
                // 게스트 상태로 로그인 요청
                Snackbar.make(binding.root, "로그인이 필요합니다.", Snackbar.LENGTH_INDEFINITE)
                    .setAction("로그인") {
                        // 로그인 버튼을 누른경우 동작한다.
                        var startLoginActivityIntent = Intent(this, Login2Activity::class.java)
                        startActivityForResult(
                            startLoginActivityIntent,
                            Login2Activity.REQUEST_CODE
                        )
                    }
                    .show()

            } else {
                // 보내기를 눌렀을 경우 동작

                var message = binding.etInput.text.toString()
                var email = (application as ChatApplication).user?.email ?: "unknown"

                var chatItem = ChatItem(message, email)
                //adapter.addItem(chatItem)

                // db에 저장한다.
                insertChatItemInDatabase(chatItem)

                sendPush("테스트 제목", "테스트 내용")

                // 초기화
                binding.etInput.text.clear()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            Login2Activity.REQUEST_CODE -> {
                // 로그인이 정상적으로 되었는지 판단한다.
                if (resultCode === Activity.RESULT_OK) {
                    Toast.makeText(this, "로그인을 성공했습니다.", Toast.LENGTH_SHORT).show()

                    isLogin = true
                    signMenu?.title = "로그아웃"
                    setAllToken()
                } else {
                    Toast.makeText(this, "게스트로 실행합니다.", Toast.LENGTH_SHORT).show()

                    isLogin = false
                    signMenu?.title = "로그인"
                }
                isLogin = SharedPreferencesUtil.isLogin(this@MainActivity)
            }
        }
    }

    /**
     * 뒤로가기 클릭 시
     */
    override fun onBackPressed() {
        var dialog = AlertDialog.Builder(this)
            .setTitle("확인")
            .setMessage("앱을 종료할까요?")
            .setNegativeButton("취소", object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    // 취소 버튼을 눌렀을 경우
                    dialog?.cancel()
                }
            })
            .setPositiveButton("종료") { dialogInterface, i ->
                finish()
            }
            .create()

        dialog.show()
    }

    /**
     * 처음 한번만 실행. 메뉴를 연결한다
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_chat, menu)
        return super.onCreateOptionsMenu(menu)
    }

    /**
     * 메뉴 정보를 변경
     */
    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        signMenu = menu?.findItem(R.id.menu_sign)
        signMenu?.title = if (isLogin) "로그아웃" else "로그인"

        return super.onPrepareOptionsMenu(menu)
    }

    /**
     * 메뉴의 아이템을 선택했을 때
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_sign -> {
                if (isLogin) {
                    // 로그아웃 요청
                    item.title = "로그인"
                    isLogin = false
                    SharedPreferencesUtil.putIsLogin(this, false)
                    Toast.makeText(this, "로그아웃 됐습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    // 로그인 요청
                    var startLoginActivityIntent = Intent(this, Login2Activity::class.java)
                    startActivityForResult(startLoginActivityIntent, Login2Activity.REQUEST_CODE)
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * 채팅을 데이터베이스에 추가한다.
     */
    fun insertChatItemInDatabase(chatItem: ChatItem) {
        /*var db = ChatDatabaseHelper(this).writableDatabase
        db.execSQL("INSERT INTO ${ChatDatabaseHelper.TABLE_CHAT} (message) VALUES ('${chatItem.message}')")
        db.close()*/

        chatRef.push().setValue(chatItem)
    }

    /**
     * 채팅을 데이터베이스에서 가져온다.
     */
    fun getChatItemsInDatabase(): ArrayList<ChatItem> {
        /*var db = ChatDatabaseHelper(this).readableDatabase
        var cursor = db.rawQuery("SELECT * FROM ${ChatDatabaseHelper.TABLE_CHAT}", null)

        var chatItems = ArrayList<ChatItem>()

        while(cursor.moveToNext()){
            var message = cursor.getString(1)
            var chatItem = ChatItem(message)
            chatItems.add(chatItem)
        }

        cursor.close()
        db.close()*/

        var chatItems = ArrayList<ChatItem>()

        chatRef.addChildEventListener(object : ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildAdded(dataSnapshot: DataSnapshot, p1: String?) {
                // 메시지를 리스트에 추가한다.
                var chatItem = dataSnapshot.getValue(ChatItem::class.java)
                if (chatItem != null) adapter.addItem(chatItem)
                binding.rvChat.smoothScrollToPosition(chatItems.size - 1)
            }

            override fun onChildRemoved(p0: DataSnapshot) {

            }
        })

        return chatItems
    }


    /**
     * 푸시를 전송한다.
     */
    fun sendPush(title: String, body: String) {
        for (token in tokens) {
            var notification = NotificationModel.Notification(title, body)
            var body = Gson().toJson(
                NotificationModel(
                    notification,
                    token
                    //"fQb9Iw9tqJs:APA91bG9pVRRGq8c5Uk5BdhYxWI1Rxhpagp6MyzSxt3qDBJnhCKYejLYGqmoihkP1mBASW9jp6eBYAU4jcBnKer2UaY6F4QLos6V6RFuK8tcHpHgElrMaSh7COydopmhFJw89O_KGXNJ"
                )
            )

            var mediaType = "application/json; charset=utf-8".toMediaType()
            var requestBody = body.toRequestBody(mediaType)

            var request = Request.Builder()
                .header("Authorization", "key=AIzaSyCDls-t78ceororxwjgUcXpPKZLsaG5t3Q")
                .header("Content-Type", "Application-json")
                .url("https://fcm.googleapis.com/fcm/send")
                .post(requestBody)
                .build()

            var okHttpClient = OkHttpClient()
            okHttpClient.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {

                }

                override fun onResponse(call: Call, response: Response) {

                }
            })
        }
    }

    override fun onStart() {
        super.onStart()

        if (isLogin) {
            // 들어오기 푸시 전송
            var email = (application as ChatApplication).user!!.email
            sendPush("알림", "$email 접속 하셨습니다.")
        }
    }

    override fun onStop() {
        super.onStop()

        if (isLogin) {
            // 나가기 푸시 전송
            var email = (application as ChatApplication).user!!.email
            sendPush("알림", "$email 종료 하셨습니다.")
        }
    }

}
