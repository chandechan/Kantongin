package com.project.kantongin.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.Timestamp
import com.project.kantongin.R
import com.project.kantongin.base.BaseActivity
import com.project.kantongin.databinding.ActivityLoginBinding
import com.project.kantongin.home.HomeActivity
import com.project.kantongin.model.*
import com.project.kantongin.prefrences.PreferenceManager
import com.project.kantongin.register.RegisterActivity
import com.project.kantongin.retrofit.Retro
import com.project.kantongin.util.timestampToString
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : BaseActivity() {

    private val binding by lazy { ActivityLoginBinding.inflate(layoutInflater) }
    private val pref by lazy { PreferenceManager(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupListener()
    }
    private fun setupListener(){
        binding.textRegister.setOnClickListener{
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }
        binding.buttonLogin.setOnClickListener{
            if (isRequired()) login()
            else Toast.makeText(applicationContext, "Isi data dengan lengkap", Toast.LENGTH_SHORT).show()
        }
    }

    private fun progress(progress: Boolean){
        binding.textAlert.visibility = View.GONE
        when (progress) {
            true -> {
                binding.buttonLogin.text = "Loading..."
                binding.buttonLogin.isEnabled = false
            }
            false -> {
                binding.buttonLogin.text = "Login"
                binding.buttonLogin.isEnabled = true
            }
        }
    }

    private fun login(){
        var login = DataLogin(
            email = binding.editEmail.text.toString(),
            password = binding.editPassword.text.toString()
        )
        progress(true)
        val retro = Retro().getApiService()
        retro.getUser(login).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(
                call: Call<LoginResponse>,
                response: Response<LoginResponse>
            ) { progress(false)
                if (response.body()!!.success) {
                        saveSession (
                            UserLogin(
                                username = response.body()!!.user.username,
                                email = response.body()!!.user.email,
                                createdAt = response.body()!!.user.createdAt as Timestamp
                            )
                        )
                    Toast.makeText(applicationContext, "Login success", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@LoginActivity,HomeActivity::class.java))
                    finish()
                } else {
                    binding.textAlert.visibility = View.VISIBLE
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Log.e("Gagal", t.message.toString())
            }

        })
//        db.collection("user")
//            .whereEqualTo("username", binding.editUsername.text.toString())
//            .whereEqualTo("password", binding.editPassword.text.toString())
//            .get()
//            .addOnSuccessListener { result ->
    }

    private fun isRequired(): Boolean{
        return (
                binding.editEmail.text.toString().isNotEmpty() &&
                binding.editPassword.text.toString().isNotEmpty()
                )
    }

    private fun saveSession (user: UserLogin){
        Log.e("LoginActivity", user.toString())
        pref.put("pref_is_login",1)
        pref.put("pref_username",user.username)
        pref.put("pref_email",user.email)
        pref.put("pref_date", timestampToString(user.createdAt)!!)
        if (pref.getInt("pref_avatar")== 0)pref.put("pref_avatar", R.drawable.avatar7)
    }
}