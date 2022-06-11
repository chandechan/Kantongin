package com.project.kantongin.model

import com.google.firebase.Timestamp

data class LoginResponse (
    var success : Boolean,
    var message : String,
    var user : UserLogin
)

data class UserLogin(
    var username : String,
    var email : String,
    var createdAt : Timestamp
)