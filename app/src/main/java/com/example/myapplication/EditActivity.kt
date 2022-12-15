package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import com.example.myapplication.databinding.ActivityEditBinding

class EditActivity : AppCompatActivity() {

    /*1*/lateinit var binding: ActivityEditBinding

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        /*2*/ binding = ActivityEditBinding.inflate(layoutInflater)
        setContentView(/*3 R.layout.activity_edit*/ binding.root)


        binding.btDone.setOnClickListener {
            val task = Task(binding.edTitle.text.toString(), binding.edDesc.text.toString())
            val editIntent = Intent().apply {
                putExtra("task", task) /* Только после получения имени task запустится adapter.addTask */
            }
            setResult(RESULT_OK, editIntent)
            finish()
        }
    }
}