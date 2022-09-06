package com.example.meeting

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.fragment_profile.*

class ProfileActivity : AppCompatActivity() {

    private lateinit var eventsRecyclerView : RecyclerView
    private var eventsList = ArrayList<Event>() //lista degli oggetti evento per l'adapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        //bind the recyclerView to the id of the layout recyclerView object
        eventsRecyclerView = findViewById(R.id.recyclerViewProfileActivity)
        eventsRecyclerView.layoutManager = LinearLayoutManager(this)

        eventsList = arrayListOf() //initialize eventsList

        Log.d("Profile Activity", "entered profile ativity")
        val userId  = intent.getStringExtra("USER ID") //Get the userId

        Log.d("Profile Activity","userId: $userId")

        fetchUserData(userId)

    }

    private fun fetchUserData(userId : String?) {

        Log.d("Profile Activity", "entered fetchUserData")

        val ref = FirebaseDatabase.getInstance().getReference("/users/$userId")

        ref.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot){

                val username = dataSnapshot.child("username").value
                val bio = dataSnapshot.child("bio").value
                val imageUrl = dataSnapshot.child("profileImageUrl").value

                usernameTextViewProfileActivity.text = username.toString()
                bioTextViewProfileActivity.text = bio.toString()
                if(imageUrl != "")
                    Picasso.get().load(imageUrl.toString()).into(imageViewProfileActivity)
            }

            override fun onCancelled(p0: DatabaseError) {
                Log.d("Profile Fragment", "load username cancelled")
            }
        })

        getUserEvents(userId)
    }

    private fun getUserEvents(uid: String?)
    {
        val eventsRef = FirebaseDatabase.getInstance().getReference("events/")

        eventsRef.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {

                for (eventSnapshot in dataSnapshot.children){
                    if(uid == eventSnapshot.child("userId").value.toString() ){
                        //create object Event to add in eventList
                        val event = eventSnapshot.getValue(Event::class.java)
                        eventsList.add(event!!)
                    }
                }

                //we create the RecyclerAdapter object with the list we just fetched
                //to visualize all the cardView on the homepage
                recyclerViewProfileActivity.adapter = RecyclerAdapter(eventsList)
            }

            override fun onCancelled(p0: DatabaseError) {
                Log.d("Profile Fragment", "load event cancelled")
            }

        })
    }
}