package com.project.kantongin.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.project.kantongin.R
import com.project.kantongin.databinding.AdapterCategoryBinding
import com.project.kantongin.databinding.AdapterTransactionBinding
import com.project.kantongin.model.Category
import com.project.kantongin.model.Transaction

class CategoryAdapter(val context: Context, var categories : ArrayList<Category>, var listener: AdapterListener?) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>(){

    private var listButton: ArrayList<MaterialButton> = arrayListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryAdapter.ViewHolder {
        return ViewHolder(
            AdapterCategoryBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: CategoryAdapter.ViewHolder, position: Int) {
        val category = categories[position]
        holder.binding.buttonCategory.text = category.name
        holder.binding.buttonCategory.setOnClickListener {
            listener?.onClick(category)
            setButton(it as MaterialButton)
        }
        listButton.add(holder.binding.buttonCategory)
    }

    override fun getItemCount() = categories.size

    class ViewHolder(val binding: AdapterCategoryBinding): RecyclerView.ViewHolder(binding.root)

    fun setData(data: List<Category>){
        categories.clear()
        categories.addAll(data)
        notifyDataSetChanged()
    }

    interface AdapterListener {
        fun onClick(category: Category)
    }

    private fun setButton(buttonSelected: MaterialButton){
        listButton.forEach{ button ->
            button.setBackgroundColor(ContextCompat.getColor(context, R.color.blue_baby))
        }
        buttonSelected.setBackgroundColor(ContextCompat.getColor(context, R.color.blue_navy))
    }

   fun setButton(category: String){
       listButton.forEach{ button ->
          if (button.text.toString().contains(category)){
               button.setBackgroundColor(ContextCompat.getColor(context, R.color.blue_navy))
           }
        }
    }
}