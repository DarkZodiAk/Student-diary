package com.android.studentdiary

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import com.android.studentdiary.databinding.ActivityLauncherBinding
import com.android.studentdiary.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    lateinit var binding: ActivityMainBinding

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        val email = auth.currentUser?.email   //Эти три строки выведут вам почту и имя аккаунта
        val displayName = auth.currentUser?.displayName

        binding.textView.text = displayName + "\n" + email

        binding.bSignOut.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LauncherActivity::class.java))
        }
    }

}