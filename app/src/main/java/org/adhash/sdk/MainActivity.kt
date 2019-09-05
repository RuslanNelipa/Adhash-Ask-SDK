package org.adhash.sdk

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onStart() {
        adView.prepareBanner("0x36016ae83df47035679f2e5d2c490c804a45ca9b")
        adView.init("https://bidder.adhash.org/", "protocol.php?action=rtb&version=1.0")
        super.onStart()
    }
}
