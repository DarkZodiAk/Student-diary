package com.android.studentdiary

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.android.studentdiary.databinding.ActivityLauncherBinding
import com.android.studentdiary.models.Folder
import com.android.studentdiary.models.User
/*import com.google.android.ads.mediation-testsuite.activities.HomeActivity*/
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
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


    private fun createFirstFolder(db: FirebaseFirestore, usrinf: FirebaseUser?): String {
        val folder = Folder(usrinf?.uid, name = "Входящие")
        val ref = db.collection("folders").document()
        ref.set(folder)
            .addOnSuccessListener { Log.d("Firestore_Log", "Id of added first folder: ${ref.id}") }
            .addOnFailureListener { Log.d("Firestore_Log", "Couldn't add first folder") }
        return ref.id
    }

    private fun createUser(db: FirebaseFirestore, usrinf: FirebaseUser?){
        db.collection("users").document("${usrinf?.uid}").get()
            .addOnSuccessListener {
                if(!it.exists()){
                    val firstRef = createFirstFolder(db, usrinf)
                    val user = User(usrinf?.displayName!!, usrinf.email)
                    user.folder_ids.add(firstRef)
                    db.collection("users").document(usrinf.uid).set(user)

                    Log.d("Firestore_Log","Created missing data")
                }
            }
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
            } else{
                Log.d("MyLog","Not successful")
            }
        }
    }

    private fun checkAuthState(){
        if(auth.currentUser!=null){
            val usrinf = auth.currentUser
            val db = Firebase.firestore
            createUser(db,usrinf)

            val intent = Intent(this, MainActivity::class.java). apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            startActivity(intent)
        }
    }
}



/**/