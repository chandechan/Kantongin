package com.project.kantongin.transaction

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.project.kantongin.adapter.TransactionAdapter
import com.project.kantongin.base.BaseActivity
import com.project.kantongin.create.UpdateActivity
import com.project.kantongin.databinding.ActivityTransactionBinding
import com.project.kantongin.fragment.DateFragment
import com.project.kantongin.model.DeleteData
import com.project.kantongin.model.Transaction
import com.project.kantongin.model.TransactionResponse
import com.project.kantongin.prefrences.PreferenceManager
import com.project.kantongin.retrofit.Retro
import com.project.kantongin.util.PrefUtil
import com.project.kantongin.util.timestampToString
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class TransactionActivity : BaseActivity() {

    private val binding by lazy { ActivityTransactionBinding.inflate(layoutInflater) }
    private lateinit var transactionAdapter : TransactionAdapter

    private val db by lazy { Firebase.firestore }
    private val pref by lazy { PreferenceManager(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupList()
        setupListener()
    }

    override fun onStart() {
        super.onStart()
        getData()
    }

    private fun setupList(){
        transactionAdapter = TransactionAdapter(arrayListOf(), object : TransactionAdapter.AdapterListener{
            override fun onClick(transaction: Transaction) {
                startActivity(
                    Intent(this@TransactionActivity, UpdateActivity::class.java)
                        .putExtra("id", transaction.id)
                )
            }

            override fun onLongClick(transaction: Transaction) {
                val alertDialog = AlertDialog.Builder(this@TransactionActivity)
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

    private fun setupListener(){
        binding.swipe.setOnRefreshListener {
            binding.textTransaction.text = "Menampilkan 50 transaksi terakhir"
            getData()
        }
        binding.imageDate.setOnClickListener{
            DateFragment(object : DateFragment.DateListener {
                override fun onSuccsess(dateStart: String, dateEnd: String) {
                    Log.e("TransactionActivity", "$dateStart, $dateEnd")
                    binding.textTransaction.text = "$dateStart - $dateEnd"
                    setTransaction(dateStart, dateEnd)
//                    db.collection("transaction")
//                        .orderBy("created", Query.Direction.DESCENDING)
//                        .whereEqualTo("username", pref.getString(PrefUtil.pref_username))
//                        .whereGreaterThanOrEqualTo("created", stringToTimestamp("$dateStart 00:00")!!)
//                        .whereLessThanOrEqualTo("created", stringToTimestamp("$dateEnd 23:59")!!)
//                        .limit(50)
//                        .get()
//                        .addOnSuccessListener { result ->
//                            binding.swipe.isRefreshing = false

//                        }
                }

            }).apply {
                show(supportFragmentManager, "dateFragment" )
            }
        }
    }


    private fun getData() {
        val transactions: ArrayList<Transaction> = arrayListOf()
        val retro = Retro().getApiService()
        pref.getString(PrefUtil.pref_email)?.let {
            retro.getTransaction(it).enqueue(object : Callback<ArrayList<Transaction>> {
                override fun onResponse(
                    call: Call<ArrayList<Transaction>>,
                    response: Response<ArrayList<Transaction>>
                ) {
                    Log.d("Test", response.body()!!.size.toString())
                    binding.swipe.isRefreshing = false
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
                    transactionAdapter.setData(transactions)
                }

                override fun onFailure(call: Call<ArrayList<Transaction>>, t: Throwable) {
                    Log.e("Gagal", t.message.toString())
                }

            })
        }
    }

//    private fun getData(){
//        binding.swipe.isRefreshing = true
//        db.collection("transaction")
//            .orderBy("created", Query.Direction.DESCENDING)
//            .whereEqualTo("username", pref.getString(PrefUtil.pref_username))
//            .limit(50)
//            .get()
//            .addOnSuccessListener { result ->
//                binding.swipe.isRefreshing = false
//                setTransaction(result)
//            }
//    }

    fun setTransaction(dateStart: String, dateEnd: String){
        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val dateStartStamp: Date = formatter.parse(dateStart)
        val dateEndStamp : Date = formatter.parse(dateEnd)
        val transactions: ArrayList<Transaction> = arrayListOf()
        val retro = Retro().getApiService()
        pref.getString(PrefUtil.pref_email)?.let {
            retro.getTransaction(it).enqueue(object : Callback<ArrayList<Transaction>> {
                override fun onResponse(
                    call: Call<ArrayList<Transaction>>,
                    response: Response<ArrayList<Transaction>>
                ) {
                    Log.d("Test", response.body()!!.size.toString())
                    binding.swipe.isRefreshing = false
                    response.body()!!.forEach { doc ->
                        val createdDate = formatter.parse(doc.created?.let { it1 ->
                            timestampToString(
                                it1
                            )
                        })
                        if (createdDate.before(dateStartStamp)&&createdDate.after(dateEndStamp)) {
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
                    }
                    transactionAdapter.setData(transactions)
                }

                override fun onFailure(call: Call<ArrayList<Transaction>>, t: Throwable) {
                    Log.e("Gagal", t.message.toString())
                }

            })
        }
//        val transactions: ArrayList<Transaction> = arrayListOf()
//        result.forEach { doc ->
//            transactions.add(
//                Transaction(
//                    id = doc.reference.id,
//                    category = doc.data["category"].toString(),
//                    type = doc.data["type"].toString(),
//                    amount = doc.data["amount"].toString().toInt(),
//                    note = doc.data["note"].toString(),
//                    created = doc.data["created"] as Timestamp
//
//                )
//            )
//        }
//        transactionAdapter.setData(transactions)
    }

    private fun deleteTransaction(id: String) {
        val delete = DeleteData(
            noteId = id
        )
        val retro = Retro().getApiService()
        pref.getString(PrefUtil.pref_email)?.let { it1 ->
            retro.deleteTransaction(delete,it1).enqueue(object : Callback<TransactionResponse> {
                override fun onResponse(
                    call: Call<TransactionResponse>,
                    response: Response<TransactionResponse>
                ) {
                    getData()
                }

                override fun onFailure(call: Call<TransactionResponse>, t: Throwable) {
                    TODO("Not yet implemented")
                }
            })
        }
    }
}