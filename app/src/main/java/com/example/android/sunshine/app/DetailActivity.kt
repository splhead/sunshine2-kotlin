package com.example.android.sunshine.app

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.MenuItemCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.ShareActionProvider
import android.view.*
import android.widget.TextView

class DetailActivity : AppCompatActivity() {
    companion object {
        @JvmField
        val TAG: String = DetailActivity.javaClass.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_detail)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .add(R.id.container, DetailFragment())
                    .commit()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.detail, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val id = item?.itemId
        if (id == R.id.action_settings) {
            startActivity(Intent(this, SettingsActivity::class.java))
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    class DetailFragment : Fragment() {
        companion object {
            val FORECAST_SHARE_HASHTAG: String = " #SunshineApp"
        }

        var mForecastStr: String = ""

        override fun onCreate(savedInstanceState: Bundle?) {
            setHasOptionsMenu(true)
            super.onCreate(savedInstanceState)
        }

        override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            val rootView = inflater!!.inflate(R.layout.fragment_detail, container, false)

            if (activity.intent != null && activity.intent.hasExtra(Intent.EXTRA_TEXT)) {
                val forecast = rootView?.findViewById<TextView>(R.id.forecast)
                mForecastStr = activity.intent.getStringExtra(Intent.EXTRA_TEXT)
                forecast?.text = mForecastStr
            }
            return rootView
        }

        override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
            inflater?.inflate(R.menu.detailfragment, menu)
            val menuItem = menu?.findItem(R.id.action_share)
            val mShareActionProvider = MenuItemCompat.getActionProvider(menuItem) as ShareActionProvider

            mShareActionProvider.setShareIntent(createShareForecastIntent())

        }

        private fun createShareForecastIntent(): Intent {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_TEXT, mForecastStr + FORECAST_SHARE_HASHTAG)
            return shareIntent
        }
    }
}
