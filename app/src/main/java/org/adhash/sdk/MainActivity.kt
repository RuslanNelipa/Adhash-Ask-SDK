package org.adhash.sdk

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setPropertyBtn.setOnClickListener { adView.adOrder = 2 }
        showBtn.setOnClickListener { adView.requestNewAd() }
        adView.setLoadingCallback { isLoading ->
            progressBar.visibility = if(isLoading) View.VISIBLE else View.GONE
            adView.visibility = if(isLoading) View.GONE else View.VISIBLE
        }
    }
}
