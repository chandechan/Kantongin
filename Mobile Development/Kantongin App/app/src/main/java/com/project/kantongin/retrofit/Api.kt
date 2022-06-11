package com.project.kantongin.retrofit

import com.project.kantongin.model.*
import com.squareup.okhttp.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface Api {
    @GET("kantongku/{userEmail}")
    fun getTransaction(@Path("userEmail") userEmail: String): Call<ArrayList<Transaction>>

    @POST("login")
    fun getUser(@Body requestBody: DataLogin):Call<LoginResponse>

    @POST("register")
    fun postRegister(@Body requestBody: DataRegister):Call<LoginResponse>

    @POST("kantongku/{userEmail}")
    fun postTransaction(@Body requestBody: TransactionData, @Path("userEmail") userEmail: String):Call<TransactionResponse>

    @PUT("kantongku/{userEmail}")
    fun putTransaction(@Body requestBody: UpdateData, @Path("userEmail") userEmail: String):Call<TransactionResponse>

//    @DELETE("kantongku/{userEmail}")
//    fun deleteTransaction(@Body requestBody: DeleteData, @Path("userEmail") userEmail: String): Call<TransactionResponse>

    @HTTP(method = "DELETE", path = "kantongku/{userEmail}", hasBody = true)
    fun deleteTransaction(@Body requestBody: DeleteData,@Path("userEmail") userEmail: String): Call<TransactionResponse>




}