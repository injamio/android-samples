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
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ir.injam.injamsdk.Injam
import ir.injam.injamsdk.model.Credentials
import ir.injam.injamsdk.model.InjamNotification
import ir.injam.injamsdk.service.IService
import kotlinx.android.synthetic.main.fragment_google_map.*
import org.json.JSONObject

class GoogleMapFragment: Fragment(), OnMapReadyCallback {

    private var mMap: GoogleMap? = null
    private val LOCATION = 1234
    var markers: MutableList<Marker> = arrayListOf()

    override fun onMapReady(p0: GoogleMap?) {
        mMap = p0
        if (ContextCompat.checkSelfPermission(context!!,
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        initGetLocation()
    }

    lateinit var injam: Injam

    private var lastLocation: Location? = null

    private var id: String? = null

    private var mFuseLocation: FusedLocationProviderClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        id = arguments?.getString("id")
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_google_map, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val credentials = Credentials(context!!, id!!,
                Constants.injam, mutableListOf())

        val no = InjamNotification("test", "test", "test",
                R.drawable.ic_launcher_background,
                "tes", "name", "desc", Color.RED,
                true, false, longArrayOf())
        injam = Injam(activity, credentials, no, object : IService {
            override fun updateMessage(p0: String?, p1: Any?) {
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

            override fun rawData(p0: String?, p1: JSONObject?) {
            }

            override fun connected() {
                trackMyLocation()
            }

            override fun failed() {
            }

            override fun joined(p0: String?) {
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

        getMap()


        mFuseLocation = LocationServices.getFusedLocationProviderClient(context!!)

        if (ContextCompat.checkSelfPermission(context!!,
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity!!,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION)
            return
        }



        initGetLocation()

    }

    private fun trackMyLocation() {
        injam.watchMyLocation()
//        injam.subscribeToMyLocation(channelName)
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

        mMap!!.isMyLocationEnabled = true
        mMap!!.uiSettings.isMyLocationButtonEnabled = true

        if (mFuseLocation == null) {
            return
        }
        val locationTask = mFuseLocation!!.lastLocation
        locationTask.addOnCompleteListener { task ->
            if (task.isSuccessful && task.result != null) {
                lastLocation = task.result
                mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        LatLng(task.result.latitude, task.result.longitude)
                        , 16f))
                injam.initSendLocations()

            }
        }.addOnFailureListener { exception -> exception.printStackTrace() }

        mMap!!.uiSettings.isZoomControlsEnabled = true
    }

    private fun updateLocationOnUI(name: String, lat: Double, lng: Double) {
        Log.d("injam", "$name $lat $lng")

        Observable.just(LocationModel(lat, lng, name))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe { t: LocationModel? ->

                    if (t != null) {
                        val m = markers.firstOrNull { marker -> marker.tag!! == t.name }
                        if (m != null) {
                            Log.d("markerInjam", "set position ${t.name}")
                            m.position = LatLng(t.lat, t.lng)
                            return@subscribe
                        }
                        val markerOptions = MarkerOptions()
                                .position(LatLng(t.lat, t.lng)).title(t.name)
                                .icon(BitmapDescriptorFactory.defaultMarker())
                        if (mMap != null) {
                            val marker = mMap!!.addMarker(markerOptions)
                            marker.tag = markerOptions.title
                            Log.d("markerInjam", markerOptions.title)
                            markers.add(marker)
                        }
                    }


                }


    }

    fun checkPermission(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        injam.checkPermission(requestCode, permissions, grantResults)
        val result = permissions.filter { s -> s.equals(Manifest.permission.ACCESS_FINE_LOCATION) }
                .map { s -> permissions.indexOf(s) }
                .filter { index -> grantResults[index] == PackageManager.PERMISSION_GRANTED }
        if (result.size <= 0) {
            return
        }
        initGetLocation()
    }

    private fun getMap() {
        (childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment).getMapAsync(this)
    }
}