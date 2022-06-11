package com.project.kantongin.model

import com.google.firebase.Timestamp
import com.google.gson.annotations.SerializedName

data class Transaction(
    var id : String?,
    var note : String,
    @SerializedName("tags")
    var category : String,
    var type : String,
    var amount : Int,
    @SerializedName("date")
    var created : Timestamp? = Timestamp.now()
)