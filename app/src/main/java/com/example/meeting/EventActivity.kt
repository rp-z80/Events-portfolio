package com.example.meeting

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.Gravity
import android.view.Gravity.CENTER
import android.view.Gravity.CENTER_VERTICAL
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.LocationCallback
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_event.*
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.event_cardview_layout.*
import kotlinx.android.synthetic.main.event_cardview_layout.view.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_profile.*
import java.text.SimpleDateFormat


class EventActivity : AppCompatActivity() {

    lateinit var mapFragment: SupportMapFragment
    lateinit var googleMap : GoogleMap

    private var event : Event ?= null

    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event)

        event = intent.getSerializableExtra("EVENT") as? Event //Get the Event object the click on event cardview passed

        populateView()

        checkParticipation()

        userImageViewEventActivity.setOnClickListener {

            /* SE L'USER CHE HA CREATO L'EVENTO CLICCA SU SE STESSO
                REDIRECT AL PROFILEFRAGMENT

            if(event?.userId == FirebaseAuth.getInstance().uid){
                val intent = Intent(this, HomepageActivity::class.java)
                intent.putExtra("REDIRECT FLAG", 1)
                startActivity(intent)
            }
             */

            //redirect a profileActivity passando l'userId dell'utente che ha creato l'evento
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("USER ID", event?.userId.toString())
            this.startActivity(intent)
        }

        usernameTextViewEventActivity.setOnClickListener {
            //redirect a profileActivity passando l'userId dell'utente che ha creato l'evento
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("USER ID", event?.userId.toString())
            this.startActivity(intent)
        }


        participateButtonEventActivity.setOnClickListener {
            val uid = FirebaseAuth.getInstance().uid ?: ""
            addUserToParticipants(uid)
        }

        dontParticpateButtonEventActivity.setOnClickListener {
            val uid = FirebaseAuth.getInstance().uid ?: ""
            removeUserFromParticipants(uid)
        }

        deleteEventButtonEventActivity.setOnClickListener {
            deleteEventFromDB()
        }

    }



    private fun checkParticipation() {

        val uid = FirebaseAuth.getInstance().uid ?: ""

        if(uid == event?.userId.toString()){
            participateButtonEventActivity.visibility = View.INVISIBLE
            participateButtonEventActivity.isClickable = false
            dontParticpateButtonEventActivity.visibility = View.INVISIBLE
            dontParticpateButtonEventActivity.isClickable = false
            deleteEventButtonEventActivity.visibility = View.VISIBLE
            deleteEventButtonEventActivity.isClickable = true
            return
        }

        else{
            for (userId in event?.participants!!){
                if(uid == userId){

                    deleteEventButtonEventActivity.visibility = View.INVISIBLE
                    deleteEventButtonEventActivity.isClickable = false
                    participateButtonEventActivity.visibility = View.INVISIBLE
                    participateButtonEventActivity.isClickable = false
                    dontParticpateButtonEventActivity.visibility = View.VISIBLE
                    dontParticpateButtonEventActivity.isClickable = true
                    return
                }
            }
        }

        deleteEventButtonEventActivity.visibility = View.INVISIBLE
        deleteEventButtonEventActivity.isClickable = false
        dontParticpateButtonEventActivity.visibility = View.INVISIBLE
        dontParticpateButtonEventActivity.isClickable = false
        participateButtonEventActivity.visibility = View.VISIBLE
        participateButtonEventActivity.isClickable = true
    }


    //populate the view with event info
    @SuppressLint("SetTextI18n")
    private fun populateView(){

        //retrieve user image and username from DB by userId stored in Event object
        val userRef = FirebaseDatabase.getInstance().getReference("users/${event?.userId}")
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Picasso.get().load(dataSnapshot.child("profileImageUrl").value.toString()).into(userImageViewEventActivity)
                usernameTextViewEventActivity.text = dataSnapshot.child("username").value.toString()
            }

            override fun onCancelled(p0: DatabaseError) {
                Log.d("Profile Fragment", "load event cancelled")
            }

        })

        Picasso.get().load(event?.imageLocation.toString()).into(eventImageViewEventActivity)
        titleTextViewEventActivity.text = event?.title.toString()
        descriptionTextViewEventActivity.text = event?.description.toString()

        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm")
        val eventDateString = "${event?.date} ${event?.time}"
        val eventDate = sdf.parse(eventDateString)
        dateTimeTextViewEventActivity.text = "" +
                "${DateFormat.format("EEEE", eventDate)} " +
                "${DateFormat.format("dd", eventDate)} " +
                "${DateFormat.format("MMM", eventDate)} " +
                "${event?.time}"

        addressTextViewEventActivity.text = event?.address.toString()

        Log.d("Event Activity", "event to string: ${event?.eventId}")

        val geoFireRef = FirebaseDatabase.getInstance().getReference("eventsGeoFireLocations/")
        val geoFire = GeoFire(geoFireRef)

        geoFire.getLocation("${event?.eventId}", object : LocationCallback {
            override fun onLocationResult(key: String?, location: GeoLocation?) {
                if (location != null) {
                    Log.d("Event Activity","The location for the event is ${location.latitude} and ${location.longitude}")

                    mapFragment = supportFragmentManager.findFragmentById(R.id.mapViewEventActivity) as SupportMapFragment

                    mapFragment.getMapAsync(OnMapReadyCallback {
                        googleMap = it
                        val location = LatLng(location.latitude, location.longitude)
                        googleMap.addMarker(MarkerOptions().position(location).title(event?.title))
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
                    })
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                System.err.println("There was an error getting the GeoFire location: $databaseError")
            }
        })


        //add the imageViews of the participants
        var i = -100 //int da incrementare per aumentare il margine sinistro delle imageview ad ogni ciclo
        for (participant in event?.participants!!){



            Log.d("Event Activity", "participant: $participant")

            val userRef = FirebaseDatabase.getInstance().getReference("users/$participant")

            userRef.addValueEventListener(object: ValueEventListener{
                override fun onDataChange(dataSnapshot: DataSnapshot) {

                    if (i==348) return

                    i += 112
                    val imageUrl = dataSnapshot.child("profileImageUrl").value

                    val newView: ImageView = de.hdodenhof.circleimageview.CircleImageView(this@EventActivity)
                    newView.id = View.generateViewId()

                    eventActivity.addView(newView)

                    newView.layoutParams.width = 100
                    newView.layoutParams.height = 100
                    newView.setBackgroundResource(R.drawable.profileimage_border)
                    Picasso.get().load(imageUrl.toString()).into(newView)

                    val params = newView.layoutParams as ConstraintLayout.LayoutParams
                    params.leftToRight = participantTextViewEventActivity.id
                    params.topToTop = participantTextViewEventActivity.id
                    params.bottomToBottom = participantTextViewEventActivity.id
                    params.setMargins(i, 0, 0, 0)
                }

                override fun onCancelled(p0: DatabaseError) {
                }
            })
        }

        val participantsButton : TextView = TextView(this)

        participantsButton.text = "+ ${event?.participants?.size.toString()}"

        eventActivity.addView(participantsButton)

        participantsButton.setBackgroundResource(R.drawable.profileimage_border)

        participantsButton.layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
        participantsButton.layoutParams.height = 100
        participantsButton.setPadding(24,0,24,0)

        participantsButton.gravity = Gravity.CENTER


        val buttonParams = participantsButton.layoutParams as ConstraintLayout.LayoutParams
        buttonParams.leftToRight = participantTextViewEventActivity.id
        buttonParams.topToTop = participantTextViewEventActivity.id
        buttonParams.bottomToBottom = participantTextViewEventActivity.id
        buttonParams.setMargins(460,0,0,0)

        participantsButton.setOnClickListener {
            showParticipantsDialog()
        }

    }


    private fun addUserToParticipants(userId : String)
    {
        event?.participants?.add(userId)

        val participantsRef = FirebaseDatabase.getInstance().getReference("events/${event?.eventId}").child("participants")

        participantsRef.setValue(event?.participants)

        finish()
        startActivity(intent)

    }

    private fun removeUserFromParticipants(uid: String) {
        event?.participants?.remove(uid)

        val participantsRef = FirebaseDatabase.getInstance().getReference("events/${event?.eventId}").child("participants")

        participantsRef.setValue(event?.participants)

        finish()
        startActivity(intent)
    }


    private fun showParticipantsDialog() {

        val builder = AlertDialog.Builder(this)

        builder.setTitle("Participants List")

        val verticalLayout = LinearLayout(this)

        verticalLayout.orientation  = LinearLayout.VERTICAL
        verticalLayout.setPadding(50,40,50,40)

        //populate dialog with participants
        for (participant in event?.participants!!){

            Log.d("Event Activity", "participant: $participant")

            val userRef = FirebaseDatabase.getInstance().getReference("users/$participant")

            userRef.addValueEventListener(object: ValueEventListener{
                override fun onDataChange(dataSnapshot: DataSnapshot) {

                    val horizontalLayout = LinearLayout(verticalLayout.context)
                    horizontalLayout.orientation = LinearLayout.HORIZONTAL
                    horizontalLayout.setPadding(0,24,12,0)


                    val imageUrl = dataSnapshot.child("profileImageUrl").value
                    val username = dataSnapshot.child("username").value

                    val newView: ImageView = de.hdodenhof.circleimageview.CircleImageView(horizontalLayout.context)
                    val usernameTextView : TextView = TextView(horizontalLayout.context)

                    horizontalLayout.addView(newView)
                    horizontalLayout.addView(usernameTextView)

                    usernameTextView.text = username.toString()
                    usernameTextView.setTextColor(Color.BLACK)
                    usernameTextView.gravity = CENTER_VERTICAL
                    usernameTextView.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                    usernameTextView.layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT




                    newView.layoutParams.width = 150
                    newView.layoutParams.height = 150
                    newView.setBackgroundResource(R.drawable.profileimage_border)
                    Picasso.get().load(imageUrl.toString()).into(newView)
                    val params = newView.layoutParams as LinearLayout.LayoutParams
                    params.setMargins(0,0,24,0)

                    verticalLayout.addView(horizontalLayout)

                    horizontalLayout.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT

                    newView.setOnClickListener {
                        //redirect a profileActivity passando l'userId dell'utente
                        val intent = Intent(horizontalLayout.context, ProfileActivity::class.java)
                        intent.putExtra("USER ID", dataSnapshot.key.toString())
                        horizontalLayout.context.startActivity(intent)
                    }

                    usernameTextView.setOnClickListener {
                        //redirect a profileActivity passando l'userId dell'utente
                        val intent = Intent(horizontalLayout.context, ProfileActivity::class.java)
                        intent.putExtra("USER ID", dataSnapshot.key.toString())
                        horizontalLayout.context.startActivity(intent)
                    }

                }

                override fun onCancelled(p0: DatabaseError) {
                }
            })
        }

        builder.setView(verticalLayout)
        builder.create().show()

    }


    private fun deleteEventFromDB(){

        val eventsRef = FirebaseDatabase.getInstance().getReference("/events")
        val geoFireLocationsRef = FirebaseDatabase.getInstance().getReference("eventsGeoFireLocations/")

        eventsRef.child(event?.eventId.toString()).removeValue()
        geoFireLocationsRef.child(event?.eventId.toString()).removeValue()

        finish()
        val intent = Intent(this, HomepageActivity::class.java)
        startActivity(intent)
    }
}

