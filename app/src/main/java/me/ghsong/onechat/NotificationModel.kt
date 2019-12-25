package me.ghsong.onechat


/**
 * 푸시 전송을 위한 데이터
 */
data class NotificationModel(val notification: Notification, var to: String) {
    data class Notification(val title: String, val body: String)

}