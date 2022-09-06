package com.example.meeting

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
//import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_home.*
//import kotlinx.android.synthetic.main.activity_editprofile.*
import kotlinx.android.synthetic.main.fragment_profile.*


class ProfileFragment : Fragment() {

    private lateinit var eventsRecyclerView : RecyclerView
    private var eventsList = ArrayList<Event>() //lista degli oggetti evento per l'adapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_profile, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        //bind the recyclerView to the id of the layout recyclerView object
        eventsRecyclerView = view.findViewById(R.id.recyclerViewProfileFragment)
        eventsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        eventsList = arrayListOf() //initialize eventsList

        fetchUserData()

        editProfileButton.setOnClickListener {
            val intent = Intent(activity, EditProfileActivity::class.java)
            startActivity(intent)
        }

        logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(activity, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun fetchUserData() {

        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        val listener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot){

                val username = dataSnapshot.child("username").value
                val bio = dataSnapshot.child("bio").value
                val imageUrl = dataSnapshot.child("profileImageUrl").value

                usernameTextViewProfileFragment.text = username.toString()
                bioTextViewProfileFragment.text = bio.toString()
                if(imageUrl != "")
                    Picasso.get().load(imageUrl.toString()).into(imageViewProfileFragment)
            }

            override fun onCancelled(p0: DatabaseError) {
                Log.d("Profile Fragment", "load username cancelled")
            }
        }
        ref.addValueEventListener(listener)

        getUserEvents(uid)

    }

    private fun getUserEvents(uid: String)
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
                recyclerViewProfileFragment.adapter = RecyclerAdapter(eventsList)
            }

            override fun onCancelled(p0: DatabaseError) {
                Log.d("Profile Fragment", "load event cancelled")
            }

        })
    }
}
