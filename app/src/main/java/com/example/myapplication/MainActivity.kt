package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.myapplication.databinding.ActivityMainBinding

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.bottomButtons.selectedItemId = R.id.folder

        binding.bottomButtons.setOnNavigationItemSelectedListener {
            when(it.itemId) {
                R.id.messages -> {
                    Toast.makeText(this,"messages", Toast.LENGTH_LONG).show()

                }
                R.id.settings -> {
                    Toast.makeText(this,"settings", Toast.LENGTH_LONG).show()
                }
                R.id.folder -> {
                    Toast.makeText(this,"folder", Toast.LENGTH_LONG).show()
                }
                R.id.calendar -> {
                    Toast.makeText(this,"calendar", Toast.LENGTH_LONG).show()
                }
            }
            true
        }

        /* Добавление фрагмента в place_holder */
        supportFragmentManager.beginTransaction().replace(R.id.place_holder, FragmentAddTask()).commit()
        /* ?Добавление фрагмента в place_holder */



    }
}