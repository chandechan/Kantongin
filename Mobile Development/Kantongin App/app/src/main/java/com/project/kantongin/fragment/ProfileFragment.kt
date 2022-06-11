package com.project.kantongin.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.findNavController
import com.project.kantongin.R
import com.project.kantongin.databinding.FragmentProfileBinding
import com.project.kantongin.login.LoginActivity
import com.project.kantongin.prefrences.PreferenceManager
import com.project.kantongin.util.PrefUtil

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private val pref by lazy { PreferenceManager(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater,container,false )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListener()
        binding.textBalance.text = requireActivity().intent.getStringExtra("balance")
    }

    override fun onStart() {
        super.onStart()
        getAvatar()
    }

    private fun getAvatar(){
        binding.imageAvatar.setImageResource(pref.getInt(PrefUtil.pref_avatar)!!)
        binding.textUsername.text = pref.getString(PrefUtil.pref_username)
        binding.textEmail.text = pref.getString(PrefUtil.pref_email)
        binding.textDate.text = pref.getString(PrefUtil.pref_date)
    }

    private fun setupListener(){
        binding.imageAvatar.setOnClickListener{
            findNavController().navigate(R.id.action_profileFragment_to_avatarFragment)
        }
        binding.cardLogout.setOnClickListener {
            pref.clear()
            Toast.makeText(requireContext(), "Logout", Toast.LENGTH_SHORT).show()
            startActivity(
                Intent(requireActivity(), LoginActivity::class.java)
                    .addFlags(
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                Intent.FLAG_ACTIVITY_CLEAR_TASK or
                                Intent.FLAG_ACTIVITY_NEW_TASK
                    )
            )
            requireActivity().finish()
        }
    }
}