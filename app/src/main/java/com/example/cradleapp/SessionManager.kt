package com.example.cradleapp

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private var pref: SharedPreferences = context.getSharedPreferences("CradleSession", Context.MODE_PRIVATE)
    private var editor: SharedPreferences.Editor = pref.edit()

    // Simpan data login
    fun saveLoginStatus(isLoggedIn: Boolean, username: String) {
        editor.putBoolean("isLoggedIn", isLoggedIn)
        editor.putString("username", username)
        editor.commit()
    }

    // Cek apakah sudah login
    fun isLoggedIn(): Boolean {
        return pref.getBoolean("isLoggedIn", false)
    }

    // Ambil nama user yang tersimpan
    fun getUsername(): String? {
        return pref.getString("username", "Admin")
    }

    // Hapus session saat logout
    fun logout() {
        editor.clear()
        editor.commit()
    }
}