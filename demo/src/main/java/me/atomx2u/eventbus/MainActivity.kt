package me.atomx2u.eventbus

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        LiveEventBus.with(TextChangedEvent::class).observe(this, sticky = true) { changed ->
            tv.text = changed.text
        }
        btn.setOnClickListener {
            LiveEventBus.with(TextChangedEvent::class).emit(
                TextChangedEvent(et.text.toString())
            )
        }
    }

    class TextChangedEvent(val text: String) : LiveEventBus.Event
}
