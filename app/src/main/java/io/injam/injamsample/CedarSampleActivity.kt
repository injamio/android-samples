package io.injam.injamsample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.cedarstudios.cedarmapssdk.CedarMaps
import com.cedarstudios.cedarmapssdk.listeners.OnTilesConfigured

class CedarSampleActivity: AppCompatActivity(), SelectPhysical {

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
        CedarMaps.getInstance().prepareTiles(object : OnTilesConfigured {
            override fun onSuccess() {
                supportFragmentManager.beginTransaction()
                        .replace(R.id.flHolder, CedarMapFragment().apply {
                            arguments = Bundle().apply {
                                putString("id", value)
                            }
                        }).commit()
            }

            override fun onFailure(errorMessage: String) {
            }
        })
    }
}