package xyz.yakdmt.prefetcherapp

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import xyz.yakdmt.prefetcher.perfomance.DroppedFrameCounter
import xyz.yakdmt.prefetcher.perfomance.DroppedFramesListener

class MainActivity : AppCompatActivity() {

    lateinit var droppedFrameCounter: DroppedFrameCounter
    private lateinit var droppedFramesText: TextView
    private lateinit var clearDroppedFrames: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportFragmentManager.beginTransaction().apply {
            val fragment = MainFragment()
            add(R.id.fragment_container, fragment)
            commit()
        }

        droppedFramesText = findViewById(R.id.dropped_frames_count)
        clearDroppedFrames = findViewById(R.id.clear_dropped_frames)
        clearDroppedFrames.setOnClickListener {
            droppedFrameCounter.reset()
            droppedFramesText.text = "Dropped frames: 0"
        }

        droppedFrameCounter = DroppedFrameCounter(this)
        droppedFrameCounter.framesListener = object : DroppedFramesListener {
            override fun onFramesCounterChanged(count: Int) {
                droppedFramesText.text = "Dropped frames: $count"
            }
        }

    }
}
