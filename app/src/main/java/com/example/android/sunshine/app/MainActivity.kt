package com.example.android.sunshine.app

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.*
import android.widget.ArrayAdapter
import android.widget.ListView


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .add(R.id.container, PlaceholderFragment())
                    .commit()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId


        return if (id == R.id.action_settings) {
            true
        } else super.onOptionsItemSelected(item)

    }

    /**
     * A placeholder fragment containing a simple view.
     */
    class PlaceholderFragment : Fragment() {

        override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                                  savedInstanceState: Bundle?): View? {
            val rootView = inflater!!.inflate(R.layout.fragment_main, container, false)

            val weekForecasts = listOf(
                    "Today - Sunny - 88/63"
                    , "Tomorrow - Foggy - 70/46"
                    , "Weds - Cloudy - 72/63"
                    , "Thurs - Rainy - 64/61"
                    , "Fri - Foggy - 70/46"
                    , "Sat - Sunny - 76/68"
                    , "Sun - Sunny - 80/69"
            )
            val mForecastAdapter = ArrayAdapter<String>(
                    activity
                    , R.layout.list_item_forecast
                    , R.id.list_item_forecast_textView
                    , weekForecasts
            )

            val listviewForecast = rootView.findViewById<ListView>(R.id.listview_forecast)
            listviewForecast.adapter = mForecastAdapter

            return rootView
        }
    }
}
