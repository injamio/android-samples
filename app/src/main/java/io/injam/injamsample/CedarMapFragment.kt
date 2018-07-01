package io.injam.injamsample

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.mapbox.mapboxsdk.annotations.IconFactory
import com.mapbox.mapboxsdk.annotations.Marker
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ir.injam.injamsdk.Injam
import ir.injam.injamsdk.model.Credentials
import ir.injam.injamsdk.model.InjamNotification
import ir.injam.injamsdk.service.IService
import kotlinx.android.synthetic.main.fragment_cedar_map.*
import org.json.JSONObject

class CedarMapFragment: Fragment() {

    private val LOCATION = 1234
    var markers: MutableList<Marker> = arrayListOf()

    lateinit var injam: Injam

    private var lastLocation: Location? = null

    private var mFuseLocation: FusedLocationProviderClient? = null

    private var mMap: MapboxMap? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_cedar_map, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        map.onCreate(savedInstanceState)
        map.getMapAsync(object : OnMapReadyCallback {
            override fun onMapReady(mapboxMap: MapboxMap?) {
                mMap = mapboxMap
                mMap?.animateCamera(
                        CameraUpdateFactory.newCameraPosition(
                                CameraPosition.Builder().target(
                                        LatLng(35.699771, 51.370329)
                                ).zoom(15.0).build()
                        )
                )
                if (ContextCompat.checkSelfPermission(activity!!.applicationContext,
                                Manifest.permission.ACCESS_FINE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {
                    return
                }
                initGetLocation()
            }
        })
        val id = arguments?.getString("id")
        val credentials = Credentials(context, id,
                Constants.injam,
                mutableListOf())

        val no = InjamNotification("test", "test", "test",
                R.drawable.ic_launcher_foreground,
                "tes", "name", "desc",
                Color.RED, true, false, longArrayOf())
        injam = Injam(activity, credentials, no, object : IService {
            override fun joined(name: String?) {

            }

            override fun failed() {
                Log.d("injam", "Something went wrong!")
            }

            override fun connected() {
                Log.d("injam", "connected to injam")
                trackMyLocation()
            }

            override fun updateMessage(name: String?, data: Any?) {

            }

            override fun rawData(name: String?, data: JSONObject?) {

            }

            override fun tracking(name: String?, locationInfo: JSONObject?) {
                name?.let {  channelName ->
                    locationInfo?.let { location ->
                        updateLocationOnUI(channelName,
                                location.getJSONObject("location").getDouble("lat"),
                                location.getJSONObject("location").getDouble("lng"))
                    }
                }

            }
        })

        exitSession.setOnClickListener { _ ->
            injam.disconnect()
            activity?.finish()
        }
        token.setOnEditorActionListener {
            _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                subscribeToLocation(token.text.toString())
                true
            }

            false
        }

        mFuseLocation = LocationServices.getFusedLocationProviderClient(context!!)

        if (ContextCompat.checkSelfPermission(activity!!.applicationContext,
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity!!,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION)
            return
        }



        initGetLocation()
    }

    override fun onStart() {
        super.onStart()
        map.onStart()
    }

    override fun onResume() {
        super.onResume()
        map.onResume()
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
    }

    override fun onStop() {
        super.onStop()
        map.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        map.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        map.takeIf { it != null }?.onDestroy()
        injam.disconnect()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        map.onSaveInstanceState(outState)
    }

    private fun trackMyLocation() {
        injam.watchMyLocation()
    }

    private fun showToast(text: String) {
        Toast.makeText(context, text, Toast.LENGTH_LONG).show()
    }

    private fun subscribeToLocation(text: String) {
        injam.subscribe(text)
    }

    private fun subscribeToMessage(name: String) {
        injam.subscribeToMessage(name)
    }

    private fun subscribe(name: String) {
        injam.subscribe(name)
    }

    @SuppressLint("MissingPermission")
    fun initGetLocation() {
        if (mMap == null) {
            return
        }

//        mMap!!.isMyLocationEnabled = true
//        map.uiSettings.isMyLocationButtonEnabled = true

        if (mFuseLocation == null) {
            return
        }
        val locationTask = mFuseLocation!!.lastLocation
        locationTask.addOnCompleteListener { task ->
            if (task.isSuccessful && task.result != null) {
                lastLocation = task.result
                mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        LatLng(task.result.latitude, task.result.longitude)
                        , 16.0))
                injam.initSendLocations()

            }
        }.addOnFailureListener { exception -> exception.printStackTrace() }

        mMap!!.uiSettings.isZoomControlsEnabled = true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray) {
        injam.checkPermission(requestCode, permissions, grantResults)
        val result = permissions.filter { s -> s.equals(Manifest.permission.ACCESS_FINE_LOCATION) }
                .map { s -> permissions.indexOf(s) }
                .filter { index -> grantResults[index] == PackageManager.PERMISSION_GRANTED }
        if (result.size <= 0) {
            return
        }
        initGetLocation()

    }

    private fun updateLocationOnUI(name: String, lat: Double, lng: Double) {
        Log.d("injam", "$name $lat $lng")

        Observable.just(LocationModel(lat, lng, name))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe { t: LocationModel? ->

                    if (t != null) {
                        val m = markers.firstOrNull { marker -> marker.title == t.name }
                        if (m != null) {
                            Log.d("markerInjam", "set position ${t.name}")
                            m.position = LatLng(t.lat, t.lng)
                            return@subscribe
                        }
                        val markerOptions = MarkerOptions()
                                .position(LatLng(t.lat, t.lng)).title(t.name)
                                .icon(IconFactory.getInstance(context!!)
                                        .defaultMarker())
                        if (mMap != null) {
                            val marker = mMap!!.addMarker(markerOptions)
                            marker.title = t.name
                            Log.d("markerInjam", markerOptions.title)
                            markers.add(marker)
                        }
                    }


                }


    }
}