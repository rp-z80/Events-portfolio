package com.example.meeting

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {

    private var blankProfilePictureLocation = ""

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        buttonRegister.setOnClickListener {
            performRegistration()
        }

        alreadyHaveAccountRegister.setOnClickListener {
            Log.d("Main Activity", "Try to show Login Activity")

            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        if(FirebaseAuth.getInstance().currentUser!=null) {
            val intent = Intent(this, HomepageActivity::class.java)
            startActivity(intent)
        }
    }

    private fun performRegistration(){
        val username = usernameEditTextRegister.text.toString().trim()
        val email = emailEditTextRegister.text.toString().trim()
        val password = passwordEditTextRegister.text.toString()

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()){
            Toast.makeText(this, "Please enter text in fields", Toast.LENGTH_LONG).show()
            return
        }

        Log.d("Main Activity", "username is $username")
        Log.d("Main Activity", "Email is $email")
        Log.d("Main Activity","Password is $password")

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { it ->
                if(!it.isSuccessful){
                    return@addOnCompleteListener
                }
                // it worked
                Log.d("Main Activity", "utente creato con successo ${it.result?.user?.uid}")

                val blankProfilePictureRef = FirebaseStorage.getInstance().getReference("images/blankProfilePicture.png")

                blankProfilePictureRef.downloadUrl.addOnSuccessListener {
                    Log.d("Register Activity","URI!!! : $it")

                    saveUserToFirebaseDatabase(it.toString())
                }
                    .addOnFailureListener {
                        Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show()
                    }



                val intent = Intent(this, HomepageActivity::class.java)
                startActivity(intent)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to create user : ${it.message}", Toast.LENGTH_LONG).show()
                Log.d("Main Activity", "Failed to create user : ${it.message}")
            }
    }

    private fun saveUserToFirebaseDatabase(blankProfilePictureLocation:String){

        val uid = FirebaseAuth.getInstance().uid ?: ""

        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        val user = User(uid, usernameEditTextRegister.text.toString(), emailEditTextRegister.text.toString(),
            blankProfilePictureLocation, "")
        ref.setValue(user)
            .addOnSuccessListener {
                Log.d("Register Activity", "User saved on FireBase DB")
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to save user on FireBase DB", Toast.LENGTH_LONG).show()
            }
    }
}
