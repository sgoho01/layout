package me.ghsong.onechat.main

import android.app.Activity
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
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import me.ghsong.onechat.ChatDatabaseHelper
import me.ghsong.onechat.R
import me.ghsong.onechat.SharedPreferencesUtil
import me.ghsong.onechat.databinding.ActivityMainBinding
import me.ghsong.onechat.login.Login2Activity

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    private var isLogin: Boolean = false
    private var chatItems = ArrayList<ChatItem>()
    private lateinit var adapter: MainChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_main
        )

        isLogin = SharedPreferencesUtil.isLogin(this@MainActivity)

        chatItems = getChatItemsInDatabase()

        adapter = MainChatAdapter(chatItems)
        var linearLayoutManager = LinearLayoutManager(this)
        binding.rvChat.layoutManager = linearLayoutManager
        binding.rvChat.adapter = adapter

        // 최초 실행시 버튼 비활성화
        binding.btnSend.isEnabled = false

        // Edit의 텍스트 변화를 감지 체크 추가
        binding.etInput.addTextChangedListener(object: TextWatcher{
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.btnSend.isEnabled = ! s.isNullOrEmpty()
            }
        })

        binding.btnSend.setOnClickListener {
            if(!isLogin){
                // 게스트 상태로 로그인 요청
                Snackbar.make(binding.root, "로그인이 필요합니다.", Snackbar.LENGTH_INDEFINITE)
                    .setAction("로그인") {
                        // 로그인 버튼을 누른경우 동작한다.
                        var startLoginActivityIntent = Intent(this, Login2Activity::class.java)
                        startActivityForResult(startLoginActivityIntent, Login2Activity.REQUEST_CODE)
                    }
                    .show()

            }else {

                // 보내기를 눌렀을 경우 동작
                var message = binding.etInput.text.toString()

                var chatItem = ChatItem(message)
                adapter.addItem(chatItem)

                // db에 저장한다.
                insertChatItemInDatabase(chatItem)

                binding.etInput.setText("")
                binding.rvChat.smoothScrollToPosition(chatItems.size - 1)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            Login2Activity.REQUEST_CODE -> {
                // 로그인이 정상적으로 되었는지 판단한다.
                if(resultCode === Activity.RESULT_OK){
                    Toast.makeText(this, "로그인을 성공했습니다.", Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(this, "게스트로 실행합니다.", Toast.LENGTH_SHORT).show()
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
            .setNegativeButton("취소", object: DialogInterface.OnClickListener{
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    // 취소 버튼을 눌렀을 경우
                    dialog?.cancel()
                }
            })
            .setPositiveButton("종료"){
                dialogInterface, i ->
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
        var menuSign = menu?.findItem(R.id.menu_sign)
        menuSign?.title = if(isLogin) "로그아웃" else "로그인"
        //menu?.findItem(R.id.menu_sign)?.title = if (isLogin) "로그아웃" else "로그인"

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
     * 데이터 베이스에 chatItem을 추가한다.
     */
    fun insertChatItemInDatabase(chatItem: ChatItem) {
        var db = ChatDatabaseHelper(this).writableDatabase
        db.execSQL("INSERT INTO ${ChatDatabaseHelper.TABLE_CHAT} (message) VALUES ('${chatItem.message}')")
        db.close()
    }

    /**
     * 데이터 베이스에서 모든 ChatItem을 리스트로 가져온다.
     */
    fun getChatItemsInDatabase(): ArrayList<ChatItem>{
        var db =ChatDatabaseHelper(this).readableDatabase
        var cursor = db.rawQuery("SELECT * FROM ${ChatDatabaseHelper.TABLE_CHAT}", null)

        var chatItems = ArrayList<ChatItem>()

        while(cursor.moveToNext()){
            var message = cursor.getString(1)
            var chatItem = ChatItem(message)
            chatItems.add(chatItem)
        }

        cursor.close()
        db.close()

        return chatItems
    }

}
