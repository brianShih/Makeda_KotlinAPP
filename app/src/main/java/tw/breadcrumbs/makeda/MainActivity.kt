package tw.breadcrumbs.makeda

import android.Manifest.permission.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Point
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.os.Bundle
import android.os.Looper
import androidx.fragment.app.Fragment
//import android.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.util.jar.Manifest
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.location.*
import java.io.IOException


class MainActivity : AppCompatActivity() {
    private val debugmode = false
    var setting_frag : Setting_Fragment? = null
    var add_frag : Add_Fragment? = null
    var dash_frag : Dash_Fragment? = null
    //var opentripplan_frag : Fragment? = null
    var notif_frag : Notif_Fragment? = null

    private val CALL_REQUIRE_CODE = 100
    private val FINE_LOCATION_REQUIRE_CODE = 101
    private val COARSE_LOCATION_REQUIRE_CODE = 102
    private val ALERT_WINDOWS_REQUIRE_CODE = 103
    private var select_index = -1
    private var Init_Done = false

    private lateinit var mLocationRequest : LocationRequest
    private lateinit var mFusedLocationProviderClient : FusedLocationProviderClient
    private var mLastLocation : Location? = null
    private val FASTEST_INTERVAL: Long = 2000 /* 2 sec */
    private val INTERVAL: Long = (2 * 1000).toLong() // 10 sec
    private var phoneNum = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val uri = intent.data

        MobileAds.initialize(this, "ca-app-pub-3903928830427305~6842437575")
        setting_frag = Setting_Fragment()
        add_frag = Add_Fragment()
        dash_frag = Dash_Fragment()
        notif_frag = Notif_Fragment()
        //opentripplan_frag = OpenTripplan_Fragment()

        navigation.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)
        BottomNavigationViewHelper.removeShiftMode(navigation)
        navigation.menu.getItem(2).isChecked = true

        //Manually displaying the first fragment - one time only
        setting_frag.let {
            //if (!setting_frag.isAdded) {
            val transaction = supportFragmentManager.beginTransaction()
            transaction.add(setting_frag!!, "setting_fragment")
            transaction.replace(R.id.frame_layout, setting_frag!!)
            transaction.addToBackStack(null)
            transaction.commit()
            //}
        }
        add_frag.let{
            //if (!add_frag.isAdded) {
            val transaction = supportFragmentManager.beginTransaction()
            transaction.add(add_frag!!, "add_fragment")
            transaction.replace(R.id.frame_layout, add_frag!!)
            transaction.addToBackStack(null)
            transaction.commit()
            //}
        }
        /*
        opentripplan_frag.let{
            val transaction = supportFragmentManager.beginTransaction()
            //if (!dash_frag.isAdded) {
            transaction.add(opentripplan_frag, "opentripplan_fragment")
            //}
            transaction.replace(R.id.frame_layout, opentripplan_frag)
            transaction.addToBackStack(null)
            transaction.commit()
        }*/
        notif_frag.let{
            //if (!notif_frag.isAdded) {
            val transaction = supportFragmentManager.beginTransaction()
            transaction.add(notif_frag!!, "notif_fragment")
            transaction.replace(R.id.frame_layout, notif_frag!!)
            transaction.addToBackStack(null)
            transaction.commit()
            //}
        }
        dash_frag.let{
            val transaction = supportFragmentManager.beginTransaction()
            //if (!dash_frag.isAdded) {
            transaction.add(dash_frag!!, "dash_fragment")
            //}
            transaction.replace(R.id.frame_layout, dash_frag!!)
            transaction.addToBackStack(null)
            transaction.commit()
        }

        locationPermissionRequire()
        startLocationUpdates()

        Init_Done = true
        val adRequest = AdRequest.Builder().build()
        adView?.loadAd(adRequest)
    }

    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        var selectedFragment: androidx.fragment.app.Fragment?= null
        when (item.itemId) {
            R.id.navigation_setting -> {
                if (Init_Done) {
                    select_index = 0
                }
                selectedFragment = setting_frag//Setting_Fragment()
            }

            R.id.navigation_add -> {
                if (Init_Done) {
                    select_index = 1
                }
                selectedFragment = add_frag//Add_Fragment()
            }

            R.id.navigation_dashboard -> {
                if (Init_Done) {
                    select_index = 2
                }
                selectedFragment = dash_frag//Dash_Fragment()
            }
/*
            R.id.navigation_opentripplan -> {
                if (Init_Done) {
                    select_index = 3
                }
                selectedFragment = opentripplan_frag
            }
*/
            R.id.navigation_notifications -> {
                if (Init_Done) {
                    select_index = 3
                }
                selectedFragment = notif_frag//Notif_Fragment()
            }
        }
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frame_layout, selectedFragment!!)
        transaction.addToBackStack(null)
        transaction.commit()

        return@OnNavigationItemSelectedListener true
    }

    override fun onBackPressed() {

        val count = fragmentManager.backStackEntryCount

        if (count == 0) {
            super.onBackPressed()
        } else {
            fragmentManager.popBackStackImmediate()
        }
    }

    open fun currFragmentID() : Int {
        //return navigation.menu.ge
        if (debugmode)
            Log.i("MainActivity","setting:${R.id.setting_layout} , add:${R.id.add_layout}" +
        " dash:${R.id.dash_layout} , notif:${R.id.notif_layout} // current : ${navigation.selectedItemId}")
        var index = 0
        val sel = navigation.selectedItemId
        when (sel) {
            R.id.setting_layout -> index = 0
            R.id.add_layout -> index = 1
            R.id.dash_layout -> index = 2
            //R.id.opentripplan_layout -> index = 3
            R.id.notif_layout -> index = 3
            //setting_frag!!.id -> index = 0
            //add_frag!!.id -> index = 1
            //dash_frag!!.id -> index = 2
            //notif_frag!!.id -> index = 3
        }
        return select_index
    }

    fun setNavigationID(index : Int) {
        if (navigation != null)
            navigation.menu.getItem(index).isChecked = true
    }

    open fun switchFragment(from: androidx.fragment.app.Fragment, to : androidx.fragment.app.Fragment, selectIndex : Int) {
        navigation.menu.getItem(selectIndex).isChecked = true
        //Manually displaying the first fragment - one time only
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frame_layout, to)
        transaction.addToBackStack(from.javaClass.name)
        transaction.commit()
    }

    fun floatingWindowPermissionRequire() {
        if (ContextCompat.checkSelfPermission(this, SYSTEM_ALERT_WINDOW) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, SYSTEM_ALERT_WINDOW)) {
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this, arrayOf(SYSTEM_ALERT_WINDOW), ALERT_WINDOWS_REQUIRE_CODE)
            }
        }
    }

    fun callPhonePermissionRequire() {
        if (ContextCompat.checkSelfPermission(this, CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, CALL_PHONE)) {
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this, arrayOf(CALL_PHONE), CALL_REQUIRE_CODE)
            }
        }
    }

    fun callPhone(phone:String) {
        if (phone == " " || phone == "-" || phone == "" || phone == "0" || phone == "待補充") {
            Toast.makeText(this@MainActivity, "需要正確的電話號碼", Toast.LENGTH_LONG).show()
            return
        }
        phoneNum = phone
        if (ContextCompat.checkSelfPermission(this, CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            callPhonePermissionRequire()
        } else {
            // Permission has already been granted
            val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phone))
            startActivity(intent)
        }
    }

    fun mapNavi(dir:String) {
        val gmmIntentUri = Uri.parse("google.navigation:q=" + dir)
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")
        startActivity(mapIntent)
    }

    fun openWeb(url : String) {
        if (url == " " || url == "-" || url == "" || url == "待補充") {
            Toast.makeText(this@MainActivity, "找不到網址", Toast.LENGTH_LONG).show()
            return
        }
        val openURL = Intent(Intent.ACTION_VIEW)
        openURL.data = Uri.parse(url)
        startActivity(openURL)
    }

    fun openFB(url : String) {
        if (url == " " || url == "-" || url == "" || url == "待補充") {
            Toast.makeText(this@MainActivity, "請更新粉絲團資料", Toast.LENGTH_LONG).show()
            return
        }
        val fbref = "fb://facewebmodal/f?href=" + url
        val openURL = Intent(Intent.ACTION_VIEW)
        openURL.data = Uri.parse(fbref)
        startActivity(openURL)
    }


    override fun onRequestPermissionsResult(requestCode: Int,
                permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            CALL_REQUIRE_CODE ->
            // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, yay!
                    callPhone(phoneNum)
                } else {
                    // permission denied, boo! Disable the
                    // functionality
                }
            COARSE_LOCATION_REQUIRE_CODE ->
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, yay!
                    startLocationUpdates()
                } else {
                    // permission denied, boo! Disable the
                    // functionality
                }
            FINE_LOCATION_REQUIRE_CODE ->
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, yay!
                    startLocationUpdates()
                } else {
                    // permission denied, boo! Disable the
                    // functionality
                }
            ALERT_WINDOWS_REQUIRE_CODE ->
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, yay!
                    //startLocationUpdates()
                } else {
                    // permission denied, boo! Disable the
                    // functionality
                }
        }
    }

    companion object {
        //var selectedItem: Item = Item()
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            // do work here
            //locationResult.lastLocation
            onLocationChanged(locationResult.lastLocation)
        }
    }

    fun getLastAddress(context: Context) : String? {
        var city : String = " "
        var country : String = " "
        var town : String = " "
        var street : String = " "
        var addr: String? = null
        try {
            val geoCoder = Geocoder(context)
            val placemark = geoCoder.getFromLocation(mLastLocation!!.latitude, mLastLocation!!.longitude, 1)
            if (debugmode)
                Log.i("MainActivity", "placemark:$placemark")
            if (placemark[0].subAdminArea == null) {
                city = placemark[0].adminArea.toString()
            } else if (placemark[0].adminArea == null) {
                city = placemark[0].subAdminArea.toString()
            } else if (placemark[0].adminArea == null && placemark[0].subAdminArea == null) {

            }
            if (placemark[0].locality != null) {
                town = placemark[0].locality.toString()
            }
            country = placemark[0].countryName.toString()

        } catch (e: IOException) {
            if (debugmode)
                Log.i("MainActivity", " error : $e")
        }
        addr = country + city + town
        return addr
    }

    fun getLastLocation() : Location? {
        if (Init_Done == false) return null
        
        return mLastLocation
    }

    fun onLocationChanged(location: Location) {
        mLastLocation = location
    }

    private fun locationPermissionRequire() {
        if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, ACCESS_FINE_LOCATION)) {

            } else {
                ActivityCompat.requestPermissions(this, arrayOf(ACCESS_FINE_LOCATION), FINE_LOCATION_REQUIRE_CODE)
                ActivityCompat.requestPermissions(this, arrayOf(ACCESS_COARSE_LOCATION), COARSE_LOCATION_REQUIRE_CODE)
            }
        }
    }

    protected fun startLocationUpdates() {

        // Create the location request to start receiving updates
        mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.setInterval(INTERVAL)
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL)

        // Create LocationSettingsRequest object using location request
        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(mLocationRequest)
        val locationSettingsRequest = builder.build()

        val settingsClient = LocationServices.getSettingsClient(this)
        settingsClient.checkLocationSettings(locationSettingsRequest)

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (debugmode)
                Log.i("MainActivity", " check location permission is fail")
            return
        }
        mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper())

    }
}



