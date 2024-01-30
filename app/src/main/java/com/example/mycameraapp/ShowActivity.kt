package com.example.mycameraapp

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.mycameraapp.databinding.ActivityMainBinding
import com.example.mycameraapp.databinding.ActivityShowBinding

class ShowActivity : AppCompatActivity() {
    private val binding: ActivityShowBinding by lazy {
        ActivityShowBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.imgCapture.setImageURI(Uri.parse(intent.getStringExtra("uri")))
    }
}