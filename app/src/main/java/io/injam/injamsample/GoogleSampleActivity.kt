package io.injam.injamsample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

class GoogleSampleActivity: AppCompatActivity(), SelectPhysical {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_holder)

        addSelectPhysicalId()
    }

    private fun addSelectPhysicalId() {
        supportFragmentManager.beginTransaction()
                .add(R.id.flHolder, SelectPhysicalIdFragment()).commit()
    }

    override var id: String = ""
        set(value) {
            loadMap(value)
        }

    private fun loadMap(value: String) {
        supportFragmentManager.beginTransaction()
                .replace(R.id.flHolder, GoogleMapFragment().apply {
            arguments = Bundle().apply {
                putString("id", value)
            }
        }, "map").commit()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        (supportFragmentManager.findFragmentByTag("map") as GoogleMapFragment)
                .checkPermission(requestCode, permissions, grantResults)
    }

}