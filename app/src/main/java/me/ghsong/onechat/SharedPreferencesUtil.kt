package me.ghsong.onechat

import android.content.Context

object SharedPreferencesUtil {

    /**
     * 로그인 상태를 저장한다.
     */
    fun putIsLogin(context: Context, isLogin: Boolean) {
        var pref = context.getSharedPreferences(FILE_PREF, Context.MODE_PRIVATE)
        var editor = pref.edit()
        editor.putBoolean(KEY_IS_LOGIN, isLogin)
        editor.commit()
    }

    /**
     * 로그인 상태를 가져온다.
     */
    fun isLogin(context: Context): Boolean {
        var pref = context.getSharedPreferences(FILE_PREF, Context.MODE_PRIVATE)
        return pref.getBoolean(KEY_IS_LOGIN, false)
    }

    private const val FILE_PREF = "chat_pref"
    private const val KEY_IS_LOGIN = "is_login"

}