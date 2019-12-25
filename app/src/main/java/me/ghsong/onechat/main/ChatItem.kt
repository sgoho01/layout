package me.ghsong.onechat.main

/**
 * 채팅 메시지 데이터 클래스
 */
data class ChatItem (
    // 메시지
    var message: String = "",
    // 보낸이
    var email: String = ""
)
