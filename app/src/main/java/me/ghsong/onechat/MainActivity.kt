package me.ghsong.onechat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import me.ghsong.onechat.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        var chatItems = ArrayList<ChatItem>()
        /*for (idx in 1..100) {
            var chatitem = ChatItem("메시지 입니다. $idx")
            chatItems.add(chatitem)
        }*/

        var adapter = MainChatAdapter(chatItems)
        var linearLayoutManager = LinearLayoutManager(this)
        binding.rvChat.layoutManager = linearLayoutManager
        binding.rvChat.adapter = adapter


    }
}
