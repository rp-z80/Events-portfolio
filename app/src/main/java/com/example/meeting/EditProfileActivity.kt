package com.example.meeting

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_create_event.*
import kotlinx.android.synthetic.main.activity_editprofile.*
import java.util.*


class EditProfileActivity : AppCompatActivity() {

    private var selectedPhotoUri : Uri? = null
    private var imageFileLocation = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editprofile)

        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        fetchDataFromDB(ref)

        buttonSelectPhotoEditProfile.setOnClickListener {
            Log.d("ProfileActivity", "Try to show photo selector")
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"

            startActivityForResult(intent, 0)
        }

        buttonUpdateProfile.setOnClickListener {

            if(imageFileLocation == ""){
                val listener = object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot){
                        val imageUrl = dataSnapshot.child("profileImageUrl").value
                        updateProfileToDatabase(imageUrl.toString())
                    }

                    override fun onCancelled(p0: DatabaseError) {
                        Log.d("EditProfile Activity", "load image Url cancelled")
                    }
                }
                ref.addValueEventListener(listener)
            }

            else
                updateProfileToDatabase(imageFileLocation)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null){
            Log.d("Profile Activity", "photo has been selected")
            selectedPhotoUri = data.data // location di dove l'immagine è stata memorizzata

            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)

            imageViewEditProfile.setImageBitmap(bitmap)
            buttonSelectPhotoEditProfile.alpha = 0f

            uploadImageToFireBase()
        }
    }

    private fun uploadImageToFireBase() {

        progressCircleEditProfile.visibility = View.VISIBLE
        buttonUpdateProfile.isClickable = false

        if(selectedPhotoUri == null) return
        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("profilePictures/$filename")
        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                Log.d("EditProfile Activity", "profile picture loaded successfully")
                ref.downloadUrl.addOnSuccessListener {
                    Log.d("EditProfile Activity", "File location $it")

                    imageFileLocation = it.toString()
                    //val uid = FirebaseAuth.getInstance().uid ?: ""
                    //val imageDbRef = FirebaseDatabase.getInstance().getReference("/users/$uid/profileImageUrl")
                    //imageDbRef.setValue(it.toString())
                    progressCircleEditProfile.visibility = View.INVISIBLE
                    buttonUpdateProfile.isClickable = true
                }
            }
            .addOnFailureListener{
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show()
            }
    }

    private fun updateProfileToDatabase(imageFileLocation:String){

        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        val user = User(uid, usernameEditTextEditProfile.text.toString(), emailEditTextEditProfile.text.toString(), imageFileLocation, bioEditTextEditProfile.text.toString())
        ref.setValue(user)
            .addOnSuccessListener {
                Log.d("EditProfile Activity", "Data updated on FireBase DB")
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to update data on FireBase DB", Toast.LENGTH_LONG)
                    .show()
            }
    }

    private fun fetchDataFromDB(ref: DatabaseReference){

        val listener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot){

                val username = dataSnapshot.child("username").value
                val email = dataSnapshot.child("email").value
                val bio = dataSnapshot.child("bio").value
                val imageUrl = dataSnapshot.child("profileImageUrl").value
                usernameEditTextEditProfile.setText(username.toString())
                emailEditTextEditProfile.setText(email.toString())

                if (bio != ""){
                    bioEditTextEditProfile.setText(bio.toString())
                }

                if (imageUrl != "https://firebasestorage.googleapis.com/v0/b/meeting-db-7f54a.appspot.com/o/images%2FblankProfilePicture.png?alt=media&token=929cc654-e40f-4355-8a2b-6a16ff808ea5"){
                    Picasso.get().load(imageUrl.toString()).into(imageViewEditProfile)
                    buttonSelectPhotoEditProfile.alpha = 0f
                }
            }

            override fun onCancelled(p0: DatabaseError) {
                Log.d("Profile Fragment", "load username cancelled")
            }
        }
        ref.addValueEventListener(listener)
    }
}