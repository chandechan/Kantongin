package com.project.kantongin.home

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.project.kantongin.R
import com.project.kantongin.adapter.TransactionAdapter
import com.project.kantongin.base.BaseActivity
import com.project.kantongin.create.CreateActivity
import com.project.kantongin.create.UpdateActivity
import com.project.kantongin.databinding.ActivityHomeBinding
import com.project.kantongin.databinding.HomeAvatarBinding
import com.project.kantongin.databinding.HomeDashboardBinding
import com.project.kantongin.model.Category
import com.project.kantongin.model.DeleteData
import com.project.kantongin.model.Transaction
import com.project.kantongin.model.TransactionResponse
import com.project.kantongin.prefrences.PreferenceManager
import com.project.kantongin.profile.ProfileActivity
import com.project.kantongin.retrofit.Api
import com.project.kantongin.retrofit.Retro
import com.project.kantongin.scan.ScanActivity
import com.project.kantongin.transaction.TransactionActivity
import com.project.kantongin.util.PrefUtil
import com.project.kantongin.util.amountFormat
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeActivity : BaseActivity() {

    private val rotateOpen: Animation by lazy {
        AnimationUtils.loadAnimation(
            this,
            R.anim.rotate_open_anim
        )
    }
    private val rotateClose: Animation by lazy {
        AnimationUtils.loadAnimation(
            this,
            R.anim.rotate_close_anim
        )
    }
    private val fromBottom: Animation by lazy {
        AnimationUtils.loadAnimation(
            this,
            R.anim.from_bottom_anim
        )
    }
    private val toBottom: Animation by lazy {
        AnimationUtils.loadAnimation(
            this,
            R.anim.to_bottom_anim
        )
    }

    private val binding by lazy { ActivityHomeBinding.inflate(layoutInflater) }
    private lateinit var bindingAvatar: HomeAvatarBinding
    private lateinit var bindingDashboard: HomeDashboardBinding
    private lateinit var transactionAdapter: TransactionAdapter

    private val pref by lazy { PreferenceManager(this) }

    private var clicked = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupBinding()
        setupListerner()
        setupList()
    }

    override fun onStart() {
        super.onStart()
        getAvatar()
        getBalance()
        getData()
    }

    private fun getAvatar() {
        bindingAvatar.textAvatar.text = pref.getString(PrefUtil.pref_username)
        bindingAvatar.imageAvatar.setImageResource(pref.getInt(PrefUtil.pref_avatar)!!)
    }

    private fun getBalance() {
        var totalIn = 0
        var totalOut = 0
        val retro = Retro().getApiService()
        pref.getString(PrefUtil.pref_email)?.let {
            retro.getTransaction(it).enqueue(object : Callback<ArrayList<Transaction>> {
                override fun onResponse(
                    call: Call<ArrayList<Transaction>>,
                    response: Response<ArrayList<Transaction>>
                ) {
                    response.body()!!.forEach { doc ->
                        when (doc.type) {
                            "IN" -> totalIn += doc.amount
                            "EX" -> totalOut += doc.amount
                        }
                    }
                    bindingDashboard.textBalance.text = amountFormat(totalIn - totalOut)
                    bindingDashboard.textIn.text = amountFormat(totalIn)
                    bindingDashboard.textOut.text = amountFormat(totalOut)
                }

                override fun onFailure(call: Call<ArrayList<Transaction>>, t: Throwable) {
                    Log.e("Gagal", t.message.toString())
                }

            })
        }
    }

//    private fun getData(){
//        binding.progress.visibility = View.VISIBLE
//        val transactions: ArrayList<Transaction> = arrayListOf()
//        db.collection("transaction")
//            .orderBy("created", Query.Direction.DESCENDING)
//            .whereEqualTo("username", pref.getString(PrefUtil.pref_username))
//            .limit(4)
//            .get()
//            .addOnSuccessListener { result ->
//                binding.progress.visibility = View.GONE
//                result.forEach { doc ->
//                    transactions.add(
//                        Transaction(
//                            id = doc.reference.id,
//                            username = doc.data["username"].toString(),
//                            category = doc.data["category"].toString(),
//                            type = doc.data["type"].toString(),
//                            amount = doc.data["amount"].toString().toInt(),
//                            note = doc.data["note"].toString(),
//                            created = doc.data["created"] as Timestamp
//
//                        )
//                    )
//                }
//                transactionAdapter.setData(transactions)
//            }
//    }

    private fun getData() {
        val transactions: ArrayList<Transaction> = arrayListOf()
        binding.progress.visibility = View.VISIBLE
        val retro = Retro().getApiService()
        pref.getString(PrefUtil.pref_email)?.let {
            retro.getTransaction(it).enqueue(object : Callback<ArrayList<Transaction>> {
                override fun onResponse(
                    call: Call<ArrayList<Transaction>>,
                    response: Response<ArrayList<Transaction>>
                ) {
                    binding.progress.visibility = View.GONE
                    Log.d("Test", response.body()!!.size.toString())
                    if (response.body()!!.size <= 4) {
                        response.body()!!.forEach { doc ->
                            transactions.add(
                                Transaction(
                                    id = doc.id,
                                    category = doc.category,
                                    type = doc.type,
                                    amount = doc.amount,
                                    note = doc.note,
                                    created = doc.created

                                )
                            )
                        }
                    }else {
                        for (i in 0..3) {
                            transactions.add(
                                Transaction(
                                    id = response.body()?.get(i)?.id,
                                    category = response.body()?.get(i)!!.category,
                                    type = response.body()?.get(i)!!.type,
                                    amount = response.body()?.get(i)!!.amount,
                                    note = response.body()?.get(i)!!.note,
                                    created = response.body()?.get(i)!!.created

                                )
                            )
                        }
                    }
                    transactionAdapter.setData(transactions)
                }

                override fun onFailure(call: Call<ArrayList<Transaction>>, t: Throwable) {
                    Log.e("Gagal", t.message.toString())
                }

            })
        }
    }


    private fun setupBinding() {
        setContentView(binding.root)
        bindingAvatar = binding.includeAvatar
        bindingDashboard = binding.includeDashboard
    }

    private fun setupList() {
        transactionAdapter =
            TransactionAdapter(arrayListOf(), object : TransactionAdapter.AdapterListener {
                override fun onClick(transaction: Transaction) {
                    startActivity(
                        Intent(this@HomeActivity, UpdateActivity::class.java)
                            .putExtra("noteId", transaction.id)
                    )
                }

                override fun onLongClick(transaction: Transaction) {
                    val alertDialog = AlertDialog.Builder(this@HomeActivity)
                    alertDialog.apply {
                        setTitle("Hapus")
                        setMessage("Hapus ${transaction.note} dari riwayat transaksi?")
                        setNegativeButton("Batal") { dialogInterface, _ ->
                            dialogInterface.dismiss()
                        }
                        setPositiveButton("Hapus") { dialogInterface, _ ->
                            deleteTransaction(transaction.id!!)
                            dialogInterface.dismiss()
                        }
                    }
                    alertDialog.show()
                }

            })
        binding.listTransaction.adapter = transactionAdapter
    }

    private fun setupListerner() {
        bindingAvatar.imageAvatar.setOnClickListener {
            startActivity(
                Intent(this, ProfileActivity::class.java)
                    .putExtra("balance", bindingDashboard.textBalance.text.toString())
            )
        }
        binding.textTransaction.setOnClickListener {
            startActivity(Intent(this, TransactionActivity::class.java))
        }
        binding.addBtn.setOnClickListener {
            onAddButtonClicked()
        }
        binding.createBtn.setOnClickListener {
            startActivity(Intent(this, CreateActivity::class.java))
        }
        binding.imgBtn.setOnClickListener {
            startActivity(Intent(this, ScanActivity::class.java))
        }
    }

    private fun onAddButtonClicked() {
        setVisibility(clicked)
        setAnimation(clicked)
        setClickable(clicked)
        clicked = !clicked
    }

    private fun setVisibility(clicked: Boolean) {
        if (!clicked) {
            binding.createBtn.visibility = View.VISIBLE
            binding.imgBtn.visibility = View.VISIBLE
        } else {
            binding.createBtn.visibility = View.INVISIBLE
            binding.imgBtn.visibility = View.INVISIBLE
        }
    }

    private fun setAnimation(clicked: Boolean) {
        if (!clicked) {
            binding.createBtn.startAnimation(fromBottom)
            binding.imgBtn.startAnimation(fromBottom)
            binding.addBtn.startAnimation(rotateOpen)
        } else {
            binding.createBtn.startAnimation(toBottom)
            binding.imgBtn.startAnimation(toBottom)
            binding.addBtn.startAnimation(rotateClose)
        }
    }

    private fun setClickable(clicked: Boolean) {
        if (!clicked) {
            binding.createBtn.isClickable = true
            binding.imgBtn.isClickable = true
        } else {
            binding.createBtn.isClickable = false
            binding.imgBtn.isClickable = false
        }
    }

    //        db.collection("transaction")
//            .document(id)
//            .delete()
//            .addOnSuccessListener {
//                getData()
//                getBalance()
//            }


    private fun deleteTransaction(id: String) {
        val delete = DeleteData(
            noteId = id
        )
        val retro = Retro().getApiService()
        pref.getString(PrefUtil.pref_email)?.let { it1 ->
            retro.deleteTransaction(delete, it1).enqueue(object : Callback<TransactionResponse> {
                override fun onResponse(
                    call: Call<TransactionResponse>,
                    response: Response<TransactionResponse>
                ) {
                    getData()
                    getBalance()
                }

                override fun onFailure(call: Call<TransactionResponse>, t: Throwable) {
                    TODO("Not yet implemented")
                }
            })
        }
    }
}