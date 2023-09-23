package com.example.foodrunner.activity

import com.example.foodrunner.R
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout


class OrderPlacedActivity : AppCompatActivity() {

    lateinit var btnOkay: Button
    lateinit var orderPlaced: RelativeLayout
    lateinit var image: ImageView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_placed)

        orderPlaced = findViewById(R.id.orderPlaced)
        btnOkay = findViewById(R.id.btnOkay)

        image=findViewById(R.id.imgOrderPlaced) as ImageView
        val animation:Animation= AnimationUtils.loadAnimation(this, R.anim.blink)
        image.startAnimation(animation)

        btnOkay.setOnClickListener {

            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finishAffinity()
        }
    }

    override fun onBackPressed() {
        //user can't go back
    }
}
