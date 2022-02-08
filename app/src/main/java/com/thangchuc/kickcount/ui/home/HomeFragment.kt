package com.thangchuc.kickcount.ui.home

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.thangchuc.kickcount.R
import com.thangchuc.kickcount.databinding.FragmentHomeBinding
import java.util.*
import kotlin.concurrent.schedule

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    private var _binding: FragmentHomeBinding? = null
    private var progr = 0.0
    private var currentTime = 0
    private var kickCounter = 0
    private var progressbar: ProgressBar? = null
    private var timerLabel: TextView? = null
    private var kickCounted: TextView? = null
    private var kicking: Button? = null
    private var cancelButton: Button? = null
    private var finishSession: Button? = null
    private var startButton: Button? = null
    private var mainHandler: Handler? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
                ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        progressbar = binding.progressBar
        timerLabel = binding.timerLabel
        kickCounted = binding.kickCounted
        kicking = binding.kicking
        cancelButton = binding.cancelButton
        finishSession = binding.finishSession
        startButton = binding.startButton
//        homeViewModel.text.observe(viewLifecycleOwner, Observer {
//            textView.text = it
//        })
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startButton?.setOnClickListener {
            startButton?.visibility = View.INVISIBLE
            progr = 1.0
            mainHandler = Handler(Looper.getMainLooper())
            mainHandler?.post(scheduleTask)
            componentVisibility(View.VISIBLE)
        }
        kicking?.setOnClickListener {
            kickCounter += 1
            kickCounted?.setText(kickCounter.toString())
        }
        cancelButton?.setOnClickListener {
            resetData()
        }
        finishSession?.setOnClickListener {
            resetData()
            componentVisibility(View.INVISIBLE)
        }
//        mainHandler.post(scheduleTask)
    }

    private fun resetData() {
        kickCounter = 0
        currentTime = 0
        timerLabel?.setText(currentTime.toString())
        kickCounted?.setText(kickCounter.toString())
        setCurrentTime()
    }

    private val scheduleTask = object : Runnable {
        override fun run() {
            updateProgressBar()
            mainHandler?.postDelayed(this, 1000)
        }
    }

    private fun updateProgressBar() {
        val percent = 1.0/72
        progr += percent
        progressbar?.progress = progr.toInt()

        setCurrentTime()
    }

    private fun setCurrentTime() {
        currentTime += 1
        val hours = currentTime / 3600;
        val minutes = "%02d".format ((currentTime % 3600) / 60)
        val seconds = "%02d".format ((currentTime % 3600) % 60)
        "0$hours : $minutes : $seconds".also { timerLabel?.text = it }
    }

    private fun componentVisibility(visibility: Int) {
        cancelButton?.visibility = visibility
        kicking?.visibility = visibility
        finishSession?.visibility = visibility
        timerLabel?.visibility = visibility
        kickCounted?.visibility = visibility
    }

    override fun onPause() {
        super.onPause()
        mainHandler?.removeCallbacks(scheduleTask)
    }

    override fun onResume() {
        super.onResume()
        mainHandler?.post(scheduleTask)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}