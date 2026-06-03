package com.nvtrong.doanchilltea

import android.content.Context
import android.content.Intent
import com.nvtrong.doanchilltea.model.CartManager

object SessionManager {
    private const val PREF_NAME = "user_session"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USER_NAME = "user_name"
    private const val KEY_USER_ROLE = "user_role"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"
    private const val KEY_LOGIN_TIME = "login_time"
    private const val SESSION_DURATION_MS = 7 * 24 * 60 * 60 * 1000L

    data class UserSession(
        val id: Int,
        val name: String,
        val role: Int
    )

    fun saveUser(context: Context, id: Int, name: String, role: Int) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putInt(KEY_USER_ID, id)
            .putString(KEY_USER_NAME, name)
            .putInt(KEY_USER_ROLE, role)
            .putLong(KEY_LOGIN_TIME, System.currentTimeMillis())
            .apply()
        CartManager.setCurrentUser(id)
    }

    fun updateUserName(context: Context, name: String) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_USER_NAME, name)
            .apply()
    }

    fun getUser(context: Context): UserSession? {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        if (!prefs.getBoolean(KEY_IS_LOGGED_IN, false)) return null
        val loginTime = prefs.getLong(KEY_LOGIN_TIME, 0L)
        if (loginTime <= 0L || System.currentTimeMillis() - loginTime > SESSION_DURATION_MS) {
            clear(context)
            return null
        }

        val id = prefs.getInt(KEY_USER_ID, -1)
        if (id == -1) return null

        return UserSession(
            id = id,
            name = prefs.getString(KEY_USER_NAME, null) ?: "Nguoi dung",
            role = prefs.getInt(KEY_USER_ROLE, 0)
        )
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
        CartManager.setCurrentUser(-1)
    }

    fun createHomeIntent(context: Context, session: UserSession): Intent {
        val target = if (session.role == 1) AdminActivity::class.java else HomeActivity::class.java
        return Intent(context, target).apply {
            putExtra("USER_ID", session.id)
            putExtra("USER_NAME", session.name)
            putExtra("USER_ROLE", session.role)
        }
    }
}
