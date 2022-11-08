package com.android.studentdiary

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.android.studentdiary.databinding.ActivityLauncherBinding
/*import com.google.android.ads.mediation-testsuite.activities.HomeActivity*/
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LauncherActivity : AppCompatActivity() {
    lateinit var launcher: ActivityResultLauncher<Intent>
    private lateinit var auth: FirebaseAuth
    lateinit var binding: ActivityLauncherBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLauncherBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth

        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
            try{
                val account = task.getResult(ApiException::class.java)
                if(account!=null){
                    authWithGoogle(account.idToken!!)
                }
            } catch (e: ApiException){
                Log.d("MyLog","Api exception")
            }
        }
        binding.bSignIn.setOnClickListener {
            signInGoogle()
        }
        checkAuthState()
    }


    private fun getClient(): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(this,gso)
    }


    private fun signInGoogle(){
        val signInIntent = getClient()
        launcher.launch(signInIntent.signInIntent)
    }


    private fun authWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener {
            if (it.isSuccessful){
                checkAuthState()
                /*intent.putExtra("email",idToken)
                intent.putExtra("name",idToken)*/

            } else{
                Log.d("MyLog","Not successful")
            }
        }
    }

    private fun checkAuthState(){
        if(auth.currentUser!=null){
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}