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
import com.project.kantongin.model.Transaction
import com.project.kantongin.prefrences.PreferenceManager
import com.project.kantongin.util.PrefUtil
import com.project.kantongin.util.stringToTimestamp

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
                    db.collection("transaction")
                        .orderBy("created", Query.Direction.DESCENDING)
                        .whereEqualTo("username", pref.getString(PrefUtil.pref_username))
                        .whereGreaterThanOrEqualTo("created", stringToTimestamp("$dateStart 00:00")!!)
                        .whereLessThanOrEqualTo("created", stringToTimestamp("$dateEnd 23:59")!!)
                        .limit(50)
                        .get()
                        .addOnSuccessListener { result ->
                            binding.swipe.isRefreshing = false
                            setTransaction(result)
                        }
                }

            }).apply {
                show(supportFragmentManager, "dateFragment" )
            }
        }
    }
    private fun getData(){
        binding.swipe.isRefreshing = true
        db.collection("transaction")
            .orderBy("created", Query.Direction.DESCENDING)
            .whereEqualTo("username", pref.getString(PrefUtil.pref_username))
            .limit(50)
            .get()
            .addOnSuccessListener { result ->
                binding.swipe.isRefreshing = false
                setTransaction(result)
            }
    }

    private fun setTransaction(result: QuerySnapshot){
        val transactions: ArrayList<Transaction> = arrayListOf()
        result.forEach { doc ->
            transactions.add(
                Transaction(
                    id = doc.reference.id,
                    category = doc.data["category"].toString(),
                    type = doc.data["type"].toString(),
                    amount = doc.data["amount"].toString().toInt(),
                    note = doc.data["note"].toString(),
                    created = doc.data["created"] as Timestamp

                )
            )
        }
        transactionAdapter.setData(transactions)
    }

    private fun deleteTransaction(id: String){
        db.collection("transaction")
            .document(id)
            .delete()
            .addOnSuccessListener {
                getData()
            }
    }
}