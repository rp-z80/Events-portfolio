package com.example.meeting

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_create_event.*
import kotlinx.android.synthetic.main.activity_editprofile.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.Calendar.*


class CreateEventActivity : AppCompatActivity() {

    //CALENDAR
    private val calendar: Calendar = getInstance()
    private var day = calendar.get(DAY_OF_MONTH)
    private var month = calendar.get(MONTH)
    private var year = calendar.get(YEAR)
    private var hour = calendar.get(HOUR_OF_DAY)
    private var minute = calendar.get(MINUTE)

    private var selectedPhotoUri: Uri?= null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_event)

        ArrayAdapter.createFromResource(
            this,
            R.array.tag_list,
            android.R.layout.simple_spinner_item
        ).also {
            adapter -> adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            selectTagSpinner.adapter = adapter
        }

        //button click to show calendar datePickerDialog
        showCalendarButton.setOnClickListener {
            val dpd = DatePickerDialog(this, R.style.datepicker_style, { view, mYear, mMonth, mDayOfMonth ->
                //set date on button
                val mmMonth = mMonth + 1
                showCalendarButton.text = "$mDayOfMonth/$mmMonth/$mYear"

                //initialize data
                day = mDayOfMonth
                month = mmMonth
                year = mYear

            }, year, month, day)
            dpd.show()
        }

        //button click to show clock timePickerDialog
        showTimePickerButton.setOnClickListener {
            val tpd = TimePickerDialog(this, R.style.timepicker_style, { view, hourOfDay, mMinute ->
                //set time on button
                showTimePickerButton.text = String.format("%02d:%02d", hourOfDay, mMinute) //format the pattern of time to show 09:05 instead of 9:5

                //initialize time
                hour = hourOfDay
                minute = mMinute

            }, hour, minute, true)
            tpd.show()
        }

        selectPhotoCreateEventButton.setOnClickListener {
                Log.d("Create Event Activity", "Try to show photo selector")
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = "image/*"

                startActivityForResult(intent, 0)
        }


        createEventButton.setOnClickListener {
            progressCircleCreateEvent.visibility = View.VISIBLE
            createEventButton.isClickable = false
            uploadImageToFireBase()
        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null){
            Log.d("Create Event Activity", "photo has been selected")
            selectedPhotoUri = data.data // location di dove l'immagine è stata memorizzata

            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)

            eventImageViewCreateEvent.setImageBitmap(bitmap)
            selectPhotoCreateEventButton.alpha = 0f
            selectPhotoTextViewCreateEvent.text = "Selected photo $selectedPhotoUri"
        }
    }


    private fun uploadImageToFireBase() {
        if(selectedPhotoUri == null) return
        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("eventsImages/$filename")
        ref.putFile(selectedPhotoUri!!)
                .addOnSuccessListener {
                    Log.d("Create Event Activity", "event image loaded successfully")
                    ref.downloadUrl.addOnSuccessListener {
                        Log.d("Create Event Activity", "File location $it")

                        saveEventToFirebaseDB(it.toString())
                    }
                }
                .addOnFailureListener{
                    Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show()
                }



    }



    private fun saveEventToFirebaseDB(imageLocation : String){

        val eventTitle = eventTitleEditTextCreateEvent.text.toString().trim()
        val eventDescription = descriptionMultilineTextCreateEvent.text.toString()
        val eventAddress = addressEditTextCreateEvent.text.toString()
        val eventDate = "$day/$month/$year"
        val eventTime = String.format("%02d:%02d", hour, minute)
        val participants = ArrayList<String>()
        val eventCategory = selectTagSpinner.selectedItem.toString()


        if (eventTitle.isEmpty() || eventDescription.isEmpty() || eventCategory.isEmpty() || eventAddress.isEmpty() || eventDate.isEmpty() || eventTime.isEmpty()){
            Toast.makeText(this, "Please enter text in fields", Toast.LENGTH_LONG).show()
            return
        }

        val eventId = UUID.randomUUID().toString()
        val uid = FirebaseAuth.getInstance().uid ?: ""
        participants.add(uid) //add event creator to list of participants
        val ref = FirebaseDatabase.getInstance().getReference("events/$eventId")
        val event = Event(eventId, eventTitle, eventDescription, eventCategory, eventAddress, uid, eventDate, eventTime, imageLocation, participants)
        ref.setValue(event)
            .addOnSuccessListener {
                Toast.makeText(this, "Event successfully created", Toast.LENGTH_LONG).show()
                finish()
                val intent = Intent(this, HomepageActivity::class.java)
                startActivity(intent)
            }
            .addOnFailureListener {
                Log.d("Create Event Activity", "Something went wrong")
            }

        //initialize location to bind to event
        val geocoder = Geocoder(this, Locale.getDefault())
        val address = geocoder.getFromLocationName(eventAddress, 1)
        val longitude: Double = address[0].longitude
        val latitude: Double = address[0].latitude

        //set event location on database
        val geoFireRef = FirebaseDatabase.getInstance().getReference("eventsGeoFireLocations/")
        val geoFire = GeoFire(geoFireRef)
        geoFire.setLocation(eventId, GeoLocation(latitude, longitude))

    }



}
