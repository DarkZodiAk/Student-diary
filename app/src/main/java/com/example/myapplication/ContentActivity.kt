package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.myapplication.databinding.ActivityContentBinding

class ContentActivity : AppCompatActivity() {
    lateinit var binding: ActivityContentBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val item = intent.getSerializableExtra("item") as Task
        binding.apply {
            /* для plainText */
            edTitle.setText(item.title)
            edDesc.setText(item.description)

        }

        binding.btChange.setOnClickListener {
            val task = Task(binding.edTitle.text.toString(), binding.edDesc.text.toString())

            val editIntent = Intent().apply {
                putExtra("task", task) /* Только после получения имени task запустится adapter.addTask */
            }
            setResult(RESULT_OK, editIntent)
            finish()
        }
    }
}