package com.gps

import android.Manifest
import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.base.BaseActivity
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Task

class Location : BaseActivity(),OnMapReadyCallback {
    //..2
    lateinit var fusedLocationClient:FusedLocationProviderClient
    val REQUEST_LOCATION_CODE =120

    // 5
    var googleMap:GoogleMap?=null
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        if (isGPSPermissionAllowed()){
            getUserLocation()
        }else{
            requestPermission()
        }
        // find
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    val requestGPSPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){
        isGranted:Boolean->
        if (isGranted){
            //.. user ok
            getUserLocation()
        }
        else{
        //requestPermission()
            showDialog("we can't get the nearest drivers to you" + "to use this feature allow location permission")
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun requestPermission(){
        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
             //show explanation to the uer and show dialog
            showDialog("please enable location permission to get you the  nearest drivers", posActionName = "allow",
            posAction = DialogInterface.OnClickListener{ dialogInterface, i ->
                dialogInterface.dismiss()
                requestGPSPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            },
            negActionName = "No", negAction = DialogInterface.OnClickListener { dialogInterface, i ->
                    dialogInterface.dismiss()
                })

        }else{
            requestGPSPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }
    // get location umedattly
    val locationRequest =LocationRequest.create().apply {
        interval =10000
        fastestInterval = 5000
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }
    @SuppressLint("MissingPermission")
    private fun getUserLocation() {
        // check gps is open
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        val client :SettingsClient = LocationServices.getSettingsClient(this)
        val task : Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build() )


        //.. get location
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        task.addOnSuccessListener { locationSettingResponse->
            fusedLocationClient.requestLocationUpdates(locationRequest ,
                locationCallBack,Looper.getMainLooper())
        }
        task.addOnFailureListener { exception->
            if (exception is ResolvableApiException){
                try {
                    // LOCATION settings are not satisfied, but this can be
                    exception.startResolutionForResult(this@Location,
                        REQUEST_LOCATION_CODE)
                } catch (sendEx : IntentSender.SendIntentException){
                    //.. ignore error
                }
            }
        }
        Toast.makeText(this,"we can access user location ",Toast.LENGTH_LONG).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode== REQUEST_LOCATION_CODE){
            if (resultCode == RESULT_OK){
                getUserLocation()
            }

        }
    }
    //..1
    fun isGPSPermissionAllowed():Boolean{
       return  ContextCompat.checkSelfPermission(this,
        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }
    // 3
    // to update location
    val locationCallBack : LocationCallback = object :LocationCallback(){
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            for (location in result.locations){
                Log.e("location update",""+location.latitude+""+location.longitude)
                userLocation = location
                drawUserMarkerOnMap()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallBack)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        drawUserMarkerOnMap()
    }

    var userLocation : Location?=null
    var userMarker :Marker?=null
    private fun drawUserMarkerOnMap() {
        val ltlng = LatLng(userLocation?.latitude?:0.0,userLocation?.longitude?:0.0)
        if (userLocation == null)return
        if (googleMap == null) return
        val markerOptions=MarkerOptions() // get this form user location
        markerOptions.position(ltlng)
        markerOptions.title ( "current location")
        if (userMarker==null){
            userMarker= googleMap?.addMarker(markerOptions)
        }
        else{
            userMarker?.position = ltlng
        }

        googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(ltlng,16.0f))
    }
}