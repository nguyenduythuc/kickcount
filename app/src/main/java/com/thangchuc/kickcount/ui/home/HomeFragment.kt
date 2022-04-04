package com.thangchuc.kickcount.ui.home

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.thangchuc.kickcount.databinding.FragmentHomeBinding
import java.util.*

class HomeFragment : Fragment() {

    private var firstOpenApp = true
    private lateinit var homeViewModel: HomeViewModel
    private var _binding: FragmentHomeBinding? = null
    private var appPreferences: SharedPreferences? = null
    private val totalTime = 3600 // in second
    private var currentTime = 0
    private var kickCounter = 0
    private var progressbar: ProgressBar? = null
    private var timerLabel: TextView? = null
    private var kickCounted: TextView? = null
    private var kicking: ImageButton? = null
    private var cancelButton: ImageButton? = null
    private var finishSession: ImageButton? = null
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
        appPreferences = this.context?.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)

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

        mainHandler = Handler(Looper.getMainLooper())
        startButton?.setOnClickListener {
            mainHandler?.post(scheduleTask)
            startCounting()
        }
        kicking?.setOnClickListener {
            kickCounter += 1
            kickCounted?.text = kickCounter.toString()
        }
        cancelButton?.setOnClickListener {
            kickCounter -= 1
            kickCounted?.text = kickCounter.toString()
        }
        finishSession?.setOnClickListener {
            resetData()
        }
    }

    private fun startCounting() {
        startButton?.visibility = View.INVISIBLE
        componentVisibility(View.VISIBLE)
    }

    private fun resetData() {
        kickCounter = 0
        currentTime = 0
        timerLabel?.text = currentTime.toString()
        kickCounted?.text = kickCounter.toString()
        "00 : 00 : 00".also { timerLabel?.text = it }
        mainHandler?.removeCallbacks(scheduleTask)
        startButton?.visibility = View.VISIBLE
        componentVisibility(View.INVISIBLE)
        saveDataIntoSharedPreference(0,0);
    }

    private val scheduleTask = object : Runnable {
        override fun run() {
            updateProgressBar()
            mainHandler?.postDelayed(this, 1000)
        }
    }

    private fun updateProgressBar() {
        if (currentTime >= totalTime) {
            mainHandler?.removeCallbacks(scheduleTask)
            progressbar?.progress = 100
            return
        }
        progressbar?.progress = (currentTime*100)/totalTime
        setCurrentTime()
    }

    private fun setCurrentTime() {
        currentTime += 1
        val hours = currentTime / 3600;
        val minutes = "%02d".format ((currentTime % 3600) / 60)
        val seconds = "%02d".format ((currentTime % 3600) % 60)
        "0$hours : $minutes : $seconds".also { timerLabel?.text = it }
        saveDataIntoSharedPreference(currentTime, kickCounter)
    }

    private fun componentVisibility(visibility: Int) {
        cancelButton?.visibility = visibility
        kicking?.visibility = visibility
        finishSession?.visibility = visibility
        timerLabel?.visibility = visibility
        kickCounted?.visibility = visibility
    }

    private fun handleContinueTimer() {
        val savedCounter = appPreferences?.getInt("currentCounter", 0)!!
        if (savedCounter <= 0) return // do nothing if open App and no counting timer previous
        // get previous time and current time to comparing
        val currentLocalTime = Calendar.getInstance()
        val lastSavedTime = Calendar.getInstance()
        appPreferences?.getLong("lastTime", 0)?.let { lastSavedTime.timeInMillis = it }
        val lastTime = lastSavedTime.timeInMillis
        val offsetTime = currentLocalTime.timeInMillis - lastTime
        var newCurrentTime = (offsetTime / 1000 + savedCounter).toInt()
        // set saved time to counter and timer
        if (offsetTime <= 3600000) {
            // if saved time is in counting time
            currentTime = newCurrentTime
            kickCounter = appPreferences?.getInt("kickCounter", 0)!!
            kickCounted?.text = kickCounter.toString()
            mainHandler?.post(scheduleTask)
            if (firstOpenApp) startCounting()
            return
        }
        // if saved time is too far from current time
        resetData()
    }

    private fun saveDataIntoSharedPreference(currentTime: Int, kickCounter: Int) {
        var editor = appPreferences?.edit()
        editor?.putInt("currentCounter", currentTime)
        val c = Calendar.getInstance()
        editor?.putLong("lastTime", c.timeInMillis)
        editor?.putInt("kickCounter", kickCounter)
        editor?.commit()
    }

    override fun onPause() {
        super.onPause()
        mainHandler?.removeCallbacks(scheduleTask)
    }

    override fun onResume() {
        super.onResume()
        handleContinueTimer()
        if (firstOpenApp) firstOpenApp = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}