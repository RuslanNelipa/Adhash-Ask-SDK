package org.adhash.sdk

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

        //set property
        adView.publisherId = "0x9e0fa4b9a910d25d3a92102dfe4ca0079031a6d4"
        setPropertyBtn.setOnClickListener { adView.adOrder = 2 }

        //request ad
        showBtn.setOnClickListener {
            adView.requestNewAd()
            errorTv.text = null
        }

        //loading callback
        adView.setLoadingCallback { isLoading ->
            //progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            //adView.visibility = if (isLoading) View.GONE else View.VISIBLE
        }

        //error callback
        adView.setErrorCallback { error ->
            //errorTv.text = "Error: $error"
        }
    }
}
