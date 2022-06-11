package com.project.kantongin.scan

import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.project.kantongin.R
import com.project.kantongin.adapter.CategoryAdapter
import com.project.kantongin.base.BaseActivity
import com.project.kantongin.databinding.ActivityCreateBinding
import com.project.kantongin.databinding.ActivityScanBinding
import com.project.kantongin.model.Category

class ScanActivity : BaseActivity() {
    final val TAG: String = "ScanActivity"
    private val binding by lazy { ActivityScanBinding.inflate(layoutInflater) }
    private lateinit var categoryAdapter: CategoryAdapter
    private var type: String = "";
    private var category: String = "";
    private val db by lazy { Firebase.firestore }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupList()
        setupListener()

    }

    override fun onStart() {
        super.onStart()
        getCategory()
    }


    private fun setupList() {
        categoryAdapter =
            CategoryAdapter(this, arrayListOf(), object : CategoryAdapter.AdapterListener {
                override fun onClick(category: Category) {
                    this@ScanActivity.category = category.name!!

                }

            })
        binding.listCategory.adapter = categoryAdapter
    }

    private fun setupListener() {
        binding.buttonIn.setOnClickListener {
            type = "IN"
            setButton(it as MaterialButton)
        }
        binding.buttonOut.setOnClickListener {
            type = "EX"
            setButton(it as MaterialButton)
        }
        binding.buttonScan.setOnClickListener {
        }
    }

    private fun setButton(buttonSelected: MaterialButton) {
        Log.e(TAG, type)
        listOf<MaterialButton>(binding.buttonIn, binding.buttonOut).forEach {
            it.setBackgroundColor(ContextCompat.getColor(this, R.color.blue_baby))
        }
        buttonSelected.setBackgroundColor(ContextCompat.getColor(this, R.color.blue_navy))
    }

    private fun getCategory() {
        val categories: ArrayList<Category> = arrayListOf()
        db.collection("category")
            .get()
            .addOnSuccessListener { result ->
                result.forEach { document ->
                    categories.add(Category(document.data["name"].toString()))
                }
                Log.e("HomeActivity", "categories $categories")
                categoryAdapter.setData(categories)

            }

    }
}