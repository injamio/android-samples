package io.injam.injamsample

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnGoogle.setOnClickListener {
            startActivity(Intent(this, GoogleSampleActivity::class.java))
        }

        btnCedar.setOnClickListener {
            startActivity(Intent(this, CedarSampleActivity::class.java))
        }
    }
}
