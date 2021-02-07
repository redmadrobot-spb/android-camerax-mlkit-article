package com.redmadrobot.numberrecognizer.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.redmadrobot.numberrecognizer.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, RecognitionFragment.newInstance())
                .commitNow()
        }
    }
}