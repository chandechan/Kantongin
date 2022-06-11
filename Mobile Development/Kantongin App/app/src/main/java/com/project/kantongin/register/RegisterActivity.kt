package com.project.kantongin.register

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.project.kantongin.base.BaseActivity
import com.project.kantongin.databinding.ActivityRegisterBinding
import com.project.kantongin.login.LoginActivity
import com.project.kantongin.model.DataRegister
import com.project.kantongin.model.LoginResponse
import com.project.kantongin.model.User
import com.project.kantongin.retrofit.Retro
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : BaseActivity() {

    private val binding by lazy { ActivityRegisterBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupListener()
    }

    private fun setupListener(){
        binding.textLogin.setOnClickListener{
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
        binding.buttonRegister.setOnClickListener{
            if (isRequired()) checkUsername()
            else Toast.makeText(applicationContext, "Isi data dengan lengkap", Toast.LENGTH_SHORT).show()
        }
    }

    private fun progress(progress: Boolean){
        binding.textAlert.visibility = View.GONE
        when (progress) {
            true -> {
                binding.buttonRegister.text = "Loading..."
                binding.buttonRegister.isEnabled = false
            }
            false -> {
                binding.buttonRegister.text = "Register"
                binding.buttonRegister.isEnabled = true
            }
        }
    }

    private fun isRequired(): Boolean{
        return (
                binding.editUsername.text.toString().isNotEmpty() &&
                binding.editEmail.text.toString().isNotEmpty() &&
                binding.editPassword.text.toString().isNotEmpty()
                )
    }

    private fun checkUsername(){
        val register = DataRegister(
            username = binding.editUsername.text.toString(),
            email = binding.editEmail.text.toString(),
            password = binding.editPassword.text.toString()
        )
        progress(true)
        val retro = Retro().getApiService()
        retro.postRegister(register).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                progress(false)
                if (response.body()!!.success) addUser()
                else binding.textAlert.visibility = View.VISIBLE
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Log.e("Gagal", t.message.toString())
            }
        })
    }
//        db.collection("user")
//            .whereEqualTo("username", binding.editUsername.text.toString())
//            .get()
//            .addOnSuccessListener { result ->
//                progress(false)
//                if (result.isEmpty) addUser()
//                else binding.textAlert.visibility = View.VISIBLE
//            }

    private fun addUser() {
        val register = DataRegister(
            username = binding.editUsername.text.toString(),
            email = binding.editEmail.text.toString(),
            password = binding.editPassword.text.toString()
        )
        progress(true)
        val retro = Retro().getApiService()
        retro.postRegister(register).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                progress(false)
                Toast.makeText(applicationContext, "User Ditambahkan", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@RegisterActivity,LoginActivity::class.java))
                finish()
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Log.e("Gagal", t.message.toString())
            }
        })
    }
//        val user = User(
//            username = binding.editUsername.text.toString(),
//            email = binding.editEmail.text.toString(),
//            password = binding.editPassword.text.toString(),
//            created = Timestamp.now()
//        )
//        db.collection("user")
//            .add(user)
//            .addOnSuccessListener {
//                progress(false)
//                Toast.makeText(applicationContext, "User Ditambahkan", Toast.LENGTH_SHORT).show()
//                startActivity(Intent(this,LoginActivity::class.java))
//                finish()
//            }
//    }
}