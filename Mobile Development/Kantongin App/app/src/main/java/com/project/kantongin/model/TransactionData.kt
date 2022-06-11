package com.project.kantongin.model

import com.google.firebase.Timestamp
import com.google.gson.annotations.SerializedName

data class TransactionData (
    var note : String,
    var tags : String,
    var type : String,
    var amount : Int,
    var date : Timestamp? = Timestamp.now()
        )
