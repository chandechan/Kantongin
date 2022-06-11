package com.project.kantongin.model

import com.google.firebase.Timestamp

data class User(
    var username : String,
    var email : String,
    var password : String,
    var created : Timestamp
)