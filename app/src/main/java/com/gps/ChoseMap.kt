package com.gps

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.compass.CompassActivity

class ChoseMap : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chose_map)
        val compass = findViewById<Button>(R.id.compass)
        val location = findViewById<Button>(R.id.location)
        compass.setOnClickListener {
            startActivity(Intent(this, CompassActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
        location.setOnClickListener {
            startActivity(Intent(this, Location::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }
}