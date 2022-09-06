package com.example.meeting

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_register.*

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        buttonLogin.setOnClickListener {
            performLogin()
        }

        backToRegistrationLogin.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity((intent))
        }
    }

    private fun performLogin(){
        val email = emailEditTextLogin.text.toString()
        val password = passwordEditTextLogin.text.toString()

        if(email.isEmpty() || password.isEmpty()){
            Toast.makeText(this,"Please insert username and password", Toast.LENGTH_LONG).show()
            return
        }

        Log.d("Login Activity", "Login with email: $email and password: $password")

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if(!it.isSuccessful) return@addOnCompleteListener

                Log.d("Login Activity", "Login effettuato con utente ${it.result?.user?.uid}")

                val intent = Intent(this, HomepageActivity::class.java)
                startActivity(intent)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Login failed ${it.message}", Toast.LENGTH_LONG).show()
            }
    }
}