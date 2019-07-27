package xyz.yakdmt.prefetcherapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportFragmentManager.beginTransaction().apply {
            val fragment = MainFragment()
            add(R.id.fragment_container, fragment)
            commit()
        }

        Timber.d("onCreate")
    }
}
