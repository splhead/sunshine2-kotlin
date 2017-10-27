package com.example.android.sunshine.app

import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.util.Log
import android.view.*
import android.widget.ArrayAdapter
import android.widget.ListView
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ForecastFragment : Fragment() {
    companion object {
        @JvmField
        val TAG: String = ForecastFragment().javaClass.simpleName
        const val OWM_LIST = "list"
        const val OWM_WEATHER = "weather"
        const val OWM_TEMPERATURE = "temp"
        const val OWM_MAX = "max"
        const val OWM_MIN = "min"
        const val OWM_DESCRIPTION = "main"
        const val FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily"
        const val QUERY_PARAM = "q"
        const val FORMAT_PARAM = "mode"
        const val UNITS_PARAM = "units"
        const val DAYS_PARAM = "cnt"
        const val APPID_PARAM = "appid"
    }

    var mForecastAdapter: ArrayAdapter<String>? = null


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater!!.inflate(R.layout.fragment_main, container, false)


        setHasOptionsMenu(true)

        /*val data: Array<String> = arrayOf(
                "Today - Sunny - 88/63"
                , "Tomorrow - Foggy - 70/46"
                , "Weds - Cloudy - 72/63"
                , "Thurs - Rainy - 64/61"
                , "Fri - Foggy - 70/46"
                , "Sat - Sunny - 76/68"
                , "Sun - Sunny - 80/69"
        )

        val weekForecasts: ArrayList<String> = ArrayList()
        weekForecasts.addAll(data)*/

        mForecastAdapter = ArrayAdapter(
                activity
                , R.layout.list_item_forecast
                , R.id.list_item_forecast_textView
                , ArrayList<String>()
        )

        val listviewForecast = rootView.findViewById<ListView>(R.id.listview_forecast)
        listviewForecast.adapter = mForecastAdapter
        listviewForecast.setOnItemClickListener { parent, view, position, id ->
            val forecast = mForecastAdapter!!.getItem(position)
            val intent = Intent(activity, DetailActivity::class.java)
            intent.putExtra(Intent.EXTRA_TEXT, forecast)
            Log.d(TAG, "starting activity")
            startActivity(intent)
        }

        return rootView
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater!!.inflate(R.menu.forecastfragment, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val id = item!!.itemId


        return if (id == R.id.action_refresh) {
            updateWeather()
            true
        } else super.onOptionsItemSelected(item)
    }

    override fun onStart() {
        super.onStart()
        updateWeather()
    }

    private fun updateWeather() {
        val sharedPreference = PreferenceManager.getDefaultSharedPreferences(activity)
        val location = sharedPreference.getString(getString(R.string.pref_location_key),
                getString(R.string.pref_location_default))
        FetchWeatherTask().execute(location)
    }

    inner class FetchWeatherTask : AsyncTask<String, Void, ArrayList<String>>() {

        override fun doInBackground(vararg params: String?): ArrayList<String> {
            val urlConnection: HttpURLConnection
            val reader: BufferedReader
            var forecastJsonStr = ""
            val format = "json"
            val units = "metric"
            val numDays = 7

            try {

                val uri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendQueryParameter(QUERY_PARAM, params[0])
                        .appendQueryParameter(FORMAT_PARAM, format)
                        .appendQueryParameter(UNITS_PARAM, units)
                        .appendQueryParameter(DAYS_PARAM, numDays.toString())
                        .appendQueryParameter(APPID_PARAM, BuildConfig.OPEN_WEATHER_MAP_API_KEY)
                        .build()

                val url = URL(uri.toString())
                Log.d(TAG, "Built URI " + uri.toString())

                // create a request
                urlConnection = url.openConnection() as HttpURLConnection
                urlConnection.requestMethod = "GET"
                urlConnection.connect()

                val inputStream = urlConnection.inputStream
                inputStream ?: return ArrayList()
                reader = BufferedReader(InputStreamReader(inputStream))

                forecastJsonStr = reader.use { it.readText() }
                Log.d(TAG, forecastJsonStr)

            } catch (e: IOException) {
                Log.w(TAG, e)
            }

            try {
                return getWeatherDataFromJson(forecastJsonStr)
            } catch (e: JSONException) {
                Log.e(TAG, e.message)
                e.printStackTrace()
            }
            return ArrayList()
        }

        override fun onPostExecute(result: ArrayList<String>) {
            if (result.size > 0) {
                mForecastAdapter?.clear()
                mForecastAdapter?.addAll(result)
            }
        }

        private fun getReadableDateString(time: Long): String {
            val shortenedDateFormat = SimpleDateFormat("EEE MMM dd", Locale.US)
            return shortenedDateFormat.format(time)
        }

        private fun formatHighLows(_high: Double, _low: Double, unitType: String): String {
            var high = _high
            var low = _low

            if (unitType == getString(R.string.pref_units_imperial)) {
                high = (high * 1.8) + 32
                low = (low * 1.8) + 32
            } else if (unitType != getString(R.string.pref_units_metric)) {
                Log.d(TAG, "Unit type not found: " + unitType)
            }

            val roundedHigh = Math.round(high)
            val roundedLow = Math.round(low)
            return roundedHigh.toString().plus("/").plus(roundedLow)
        }

        private fun getWeatherDataFromJson(forecastJsonstr: String): ArrayList<String> {
            val forecastJson = JSONObject(forecastJsonstr)
            val weatherArray = forecastJson.getJSONArray(OWM_LIST)

            val dayTime = Calendar.getInstance()

            val resultStrs = ArrayList<String>()

            val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(activity)
            val unitType = sharedPrefs.getString(
                    getString(R.string.pref_units_key),
                    getString(R.string.pref_units_metric)
            )

            for (i in 0..(weatherArray.length() - 1)) {
                var day: String
                var description: String
                var highAndLow: String

                val dayForecast: JSONObject = weatherArray.getJSONObject(i)

                val dateTime = dayTime.timeInMillis + (1000 * 60 * 60 * 24 * i)

                day = getReadableDateString(dateTime)

                val weatherObject: JSONObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0)

                description = weatherObject.getString(OWM_DESCRIPTION)

                val temperatureObject: JSONObject = dayForecast.getJSONObject(OWM_TEMPERATURE)
                val high: Double = temperatureObject.getDouble(OWM_MAX)
                val low: Double = temperatureObject.getDouble(OWM_MIN)

                highAndLow = formatHighLows(high, low, unitType)

                resultStrs.add(i, "$day - $description - $highAndLow")
            }

            for (l in resultStrs) {
                Log.d(ForecastFragment.TAG, "forecast entry $l")
            }

            return resultStrs
        }
    }


}