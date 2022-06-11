package com.project.kantongin.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.project.kantongin.R
import com.project.kantongin.databinding.FragmentDateBinding
import java.util.*


class DateFragment (var listener: DateListener) : BottomSheetDialogFragment() {

    private lateinit var binding : FragmentDateBinding
    private var clickDateStart: Boolean = false
    private var dateTemp : String = ""
    private var dateStart : String = ""
    private var dateEnd : String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {
        binding = FragmentDateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setView("Tanggal mulai", "Pilih")
        setupListener()
    }

    private fun setupListener(){
        binding.calenderView.setOnDateChangeListener { _, year, month, day ->
            dateTemp = "$day/${month + 1}/$year"
        }
        binding.textApply.setOnClickListener {
            when(clickDateStart){
                false -> {
                    clickDateStart = true
                    dateStart = dateTemp
                    binding.calenderView.date = Date().time
                    setView("Tanggal akhir", "Terapkan")
                }
                true -> {
                    dateEnd = dateTemp
                    listener.onSuccsess(dateStart,dateEnd)
                    this.dismiss()
                }
            }
        }
    }

    private fun setView(title: String, apply: String){
        binding.textTitle.text = title
        binding.textApply.text = apply
    }

    interface DateListener {
        fun onSuccsess(dateStart: String, dateEnd: String)
    }

}