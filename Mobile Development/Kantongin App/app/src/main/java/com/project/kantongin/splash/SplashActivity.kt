package com.project.kantongin.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.project.kantongin.R
import com.project.kantongin.base.BaseActivity
import com.project.kantongin.home.HomeActivity
import com.project.kantongin.login.LoginActivity
import com.project.kantongin.prefrences.PreferenceManager

class SplashActivity : BaseActivity() {
    private val pref by lazy { PreferenceManager(this) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        Handler(Looper.getMainLooper()).postDelayed( {
            if (pref.getInt("pref_is_login") == 0){
                startActivity(Intent(this, LoginActivity::class.java))
            } else startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }, 2000)
    }
}