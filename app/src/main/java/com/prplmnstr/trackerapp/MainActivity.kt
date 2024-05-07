package com.prplmnstr.trackerapp

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.prplmnstr.trackerapp.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var googleMap: GoogleMap
    private lateinit var databaseReference: DatabaseReference
    private var currentMarker: Marker? = null
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val firebaseDatabase = FirebaseDatabase.getInstance()
        databaseReference = firebaseDatabase.getReference("locations")



    if (ActivityCompat.checkSelfPermission(this,
    android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(this,
            arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
            1)
    }
        startLocationUpdates()
}

override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        startLocationUpdates()
    }
}

@SuppressLint("MissingPermission")
private fun startLocationUpdates() {
    val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
    val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            val locationData = HashMap<String, Any>()
            locationData["latitude"] = location.latitude
            locationData["longitude"] = location.longitude

            databaseReference.setValue(locationData)

        }
        override fun onProviderDisabled(provider: String) {}
    }
    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0f, locationListener)
}

@SuppressLint("MissingPermission")
override fun onMapReady(gMap: GoogleMap) {
    googleMap = gMap
    googleMap.isMyLocationEnabled = true

    databaseReference.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val latitude = snapshot.child("latitude").getValue(Double::class.java)
            val longitude = snapshot.child("longitude").getValue(Double::class.java)

            latitude?.let { lat ->
                longitude?.let { long ->
                    val latLng = LatLng(lat, long)
                    if (currentMarker == null) {
                        // If marker is null, create a new marker
                        currentMarker = googleMap.addMarker(MarkerOptions().position(latLng).title("Current Location"))
                    } else {
                        // Update existing marker position
                        currentMarker?.position = latLng
                    }

                    val cameraPosition = CameraPosition.Builder()
                        .target(latLng)
                        .zoom(15f) // Zoom level, adjust as needed
                        .build()
                    googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))

                }
            }
        }

        override fun onCancelled(error: DatabaseError) {
            // Handle database error
        }
    })
}

override fun onResume() {
    super.onResume()
  // binding.mapView.onResume()
}

override fun onPause() {
    super.onPause()
   // binding.mapView.onPause()
}

override fun onDestroy() {
    super.onDestroy()
   // binding.mapView.onDestroy()
}

override fun onLowMemory() {
    super.onLowMemory()
   // binding.mapView.onLowMemory()
}
}