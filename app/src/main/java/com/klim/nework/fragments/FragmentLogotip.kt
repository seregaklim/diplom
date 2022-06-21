package com.klim.nework.fragments

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import com.klim.nework.R
import com.klim.nework.databinding.ActivityMainBinding
import com.klim.nework.databinding.FragmentLogotipBinding
import com.klim.nework.viewModel.AuthViewModel
import com.klim.nework.viewModel.PageViewModel


import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentLogotip  : Fragment() {

    private val viewModel: AuthViewModel by viewModels()

    private lateinit var navController: NavController




    private lateinit var binding: FragmentLogotipBinding
    private var timer: CountDownTimer? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentLogotipBinding.inflate(
            inflater,
            container,
            false
        )

        startCountDownTimer(4000)

        //отключить
        fun onResumeActionBar() {
            super.onResume()
            (activity as AppCompatActivity).supportActionBar!!.hide()
        }
        onResumeActionBar()


        return binding.root
    }

    private fun startCountDownTimer(timeMillis: Long){
        timer?.cancel()
        timer = object : CountDownTimer(timeMillis, 1){
            override fun onTick(timeM: Long) {
                binding.logotipStart
            }

            override fun onFinish() {


                findNavController().navigate(R.id.action_fragmentLogotip_to_logInFragment)
            }

        }.start()
    }

}