package com.example.meeting

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQueryEventListener
import com.google.android.gms.location.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_home.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class HomeFragment : Fragment(), GeoQueryEventListener {

    //permission id is just an int that must be unique so it can be any number
    private var PERMISSION_ID = 42

    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    lateinit var locationRequest: LocationRequest

    private var currentLocationLatitude: Double = 0.0
    private var currentLocationLongitude: Double = 0.0

    private lateinit var eventsRecyclerView : RecyclerView
    private var eventsKeysList: ArrayList<String> = arrayListOf() //lista delle chiavi degli eventi vicini alla posizione
    private var eventsList = ArrayList<Event>() //lista degli oggetti evento per l'adapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        //bind the recyclerView to the id of the layout recyclerView object
        eventsRecyclerView = view.findViewById(R.id.recyclerViewHomeFragment)
        eventsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        eventsList = arrayListOf() //initialize eventsList

        //fusedLocationProvider used for retrieve the device location
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())

        //getLastLocation retrieve device location and when it gets results calls getNearbyEvents
        //inside the listener, otherwise getNearbyEvents would be called before getLastLocation receives
        //the results for the device location
        getLastLocation()

        createANewEventButton.setOnClickListener {
            val intent = Intent(activity, CreateEventActivity::class.java)
            startActivity(intent)
        }
    }


    private fun getNearbyEvents() {

        Log.d("Home Fragment", "entered on getNearbyEvents function")

        //clear eventList so homepage doesn't stack new events with old ones that could be the same on every refresh
        eventsList.clear()

        // creates a new query around current Location with a radius of 10 kilometers
        val geoFireRef = FirebaseDatabase.getInstance().getReference("eventsGeoFireLocations/")
        val geoFire = GeoFire(geoFireRef)
        val geoQuery = geoFire.queryAtLocation(GeoLocation(currentLocationLatitude, currentLocationLongitude), 10.0)

        //listener for query results implemented in onKeyEntered etc.
        geoQuery.addGeoQueryEventListener(this)
    }

    override fun onKeyEntered(key: String?, location: GeoLocation?) {
        Log.d("Home Fragment", "entered on onKeyEntered function")

        //for every event found that satisfy the geoQuery we add his id to te list of events ids
        eventsKeysList.add(key.toString())
    }

    override fun onKeyExited(key: String?) {}

    override fun onKeyMoved(key: String?, location: GeoLocation?) {}

    override fun onGeoQueryReady() {
        Log.d("Home Fragment", "All data from locations has been retrieved")
        Log.d("onGeoQueryReady", "eventsKeysList size: ${eventsKeysList.size}")

        //here we are sure that all results from geoQuery have been retrieved and that
        //eventsKeysList is filled with every nearby events, so we can fetch evry event data
        //and populate our homepage
        populateView()
    }

    override fun onGeoQueryError(error: DatabaseError?) {}


    //function to get last location
    private fun getLastLocation(){

        //check permission
        if(checkPermission()){

            //check if location service is enabled
            if(isLocationEnabled()){
                Log.d("Home Fragment", "Location is enabled")

                //Get the location
                fusedLocationProviderClient.lastLocation.addOnCompleteListener { task ->
                    val location = task.result
                    if(location == null){
                        //if there is no last location saved on device we have to get a new location
                        getNewLocation()
                    }
                    else {
                        currentLocationLatitude = location.latitude
                        currentLocationLongitude = location.longitude
                        Log.d("Home Fragment", "getLastLocation location.latitude: ${location.latitude} and location.longitude: ${location.longitude}")

                        //Here we are sure we have the device location so we can get the nearest events to show on homepage
                        getNearbyEvents()
                    }
                }

            }
            else Toast.makeText(view?.context, "Location Services are disabled", Toast.LENGTH_LONG).show()
        }
        else requestPermission()
    }

    private fun getNewLocation(){

        Log.d("Home Fragment", "entered getNewLocation")
        locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 0
        locationRequest.fastestInterval = 0
        locationRequest.numUpdates = 2

        if(checkPermission()) {
            fusedLocationProviderClient.requestLocationUpdates(
                    locationRequest, locationCallback, Looper.myLooper())
        }
    }

    private val locationCallback = object : LocationCallback(){
        override fun onLocationResult(p0: LocationResult) {
            val lastLocation = p0.lastLocation
            //Now we will set the new location
            currentLocationLatitude = lastLocation.latitude
            currentLocationLongitude = lastLocation.longitude

            Log.d("Home Fragment", "LocationCallback assigned: LATITUDE: $currentLocationLatitude and LONGITUDE: $currentLocationLongitude")

            //same as getLastLocation here we are sure we have the device location
            // so we can get the nearest events to show on homepage
            getNearbyEvents()
        }
    }

    //function to check uses permission to access location
    private fun checkPermission():Boolean{

        Log.d("Home Fragment", "entered checkPermission")

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("Home Fragment","checkPermission FALSE")
            return false

        }
        Log.d("Home Fragment","checkPermission TRUE")

        return true
    }

    //function to get user permission to access location
    private fun requestPermission(){

        Log.d("Home Fragment", "entered requestPermission")

        ActivityCompat.requestPermissions(
            this.requireActivity(),
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            PERMISSION_ID)

    }

    //function to check if location service of device is enabled
    private fun  isLocationEnabled():Boolean{
        val locationManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        Log.d("Home Fragment", "entered onRequestPermissionResult")
        //built in function that checks the permission result
        //used just for debugging
        if(requestCode==PERMISSION_ID){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Log.d("Home Fragment", "onRequestPermissionResult: You Have the Permission")
            }
            else Log.d("Home Fragment", "onRequestPermissionResult: You DON'T have the Permission")
        }
    }


    //function to populate the recyclerView with the retrieved events from getNearbyEvents
    private  fun populateView(){
        Log.d("Home Fragment", "entered populateView function")

        val c = Calendar.getInstance()

        val eventsRef = FirebaseDatabase.getInstance().getReference("events/")

        eventsRef.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {

                //for every key retrieved by getNearbyEvents we
                //search for the corresponding event in DB
                for (key in eventsKeysList){
                    for (eventSnapshot in dataSnapshot.children){
                        if(key == eventSnapshot.key ){

                            val eventDate = eventSnapshot.child("date").value.toString()
                            val eventTime = eventSnapshot.child("time").value.toString()
                            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm")
                            val currentDateTime = c.time
                            val eventDateTimeString = "$eventDate $eventTime"

                            val eventDateTime = sdf.parse(eventDateTimeString)

                            if (currentDateTime <= eventDateTime){
                                //create object Event to add in eventList
                                val event = eventSnapshot.getValue(Event::class.java)
                                eventsList.add(event!!)
                           }

                        }
                    }
                }

                //we create the RecyclerAdapter object with the list we just fetched
                //to visualize all the cardView on the homepage
                recyclerViewHomeFragment.adapter = RecyclerAdapter(eventsList)
            }

            override fun onCancelled(p0: DatabaseError) {
                Log.d("Profile Fragment", "load event cancelled")
            }

        })
    }

}