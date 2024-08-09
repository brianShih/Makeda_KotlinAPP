package tw.breadcrumbs.makeda

import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.AsyncTask
import android.util.Log
import tw.breadcrumbs.makeda.dataModel.CountriesNCities
import tw.breadcrumbs.makeda.dataModel.PPModel
import tw.breadcrumbs.makeda.dataModel.TripPlan_PPModel


class calcuteDistanceHelper(@field:SuppressLint("StaticFieldLeak") val activity:MainActivity,
                            @field:SuppressLint("StaticFieldLeak") val context: Context,
                            val callback: (clist:MutableList<TripPlan_PPModel>?) -> Unit)
    : AsyncTask<MutableList<TripPlan_PPModel>, Unit, MutableList<TripPlan_PPModel>>() {
    private val debugmode = false
    var calList :MutableList<TripPlan_PPModel>? = mutableListOf()

    override fun doInBackground(vararg params: MutableList<TripPlan_PPModel>): MutableList<TripPlan_PPModel>? {
        try {
            calList = params[0]
            if (calList!!.count() > 0) {
                calList!!.forEach {
                    it.distance = updateDistance(it)
                    if (isCancelled) return null
                    it.calDistanceDone = true
                    if (debugmode) Log.i("calcuteDistanceHelper", " name = ${it.name}, distance = ${it.distance}")
                }
                return calList
            }
        } catch ( e: RuntimeException) {
            if (debugmode)
                Log.i("calcuteDistanceHelper", " RuntimeException : $e")
        }
        return null
    }

    override fun onPostExecute(result: MutableList<TripPlan_PPModel>?) {
        if (result == null) return

        super.onPostExecute(result)
        callback(result)
    }

    private fun getDistance(dLoc : Location) : Float {
        val currLoc: Location = (activity as MainActivity).getLastLocation() ?: return 0f
        return currLoc.distanceTo(dLoc)
    }

    fun cleanupList() {
        calList = mutableListOf()
    }

    fun getLocationFromAddress(strAddress:String) : Location? {

        val coder : Geocoder = Geocoder(context)
        var address : List<Address>? = null
        val p1 : Location = Location(LocationManager.PASSIVE_PROVIDER)

        try {

            address = coder.getFromLocationName(strAddress,1)
            //Log.i("Dash_Fragment", " address: $address")

            if (address == null) {
                return null
            }
            val addrLoc = address[0]
            p1.latitude = addrLoc.latitude
            p1.longitude = addrLoc.longitude

        } catch (e : Exception) {
            if (debugmode)
                Log.i("Dash_Fragment", "getLocationFromAddress : Error: $e")
        }
        return p1
    }

    fun updateDistance(item: TripPlan_PPModel) :Float {
        val addr = item.addr
        val lang = CountriesNCities().isCJK(addr)
        if (lang) {
            //if (addr)
            val loc: Location? = getLocationFromAddress(addr)
            var dist = 0
            if (loc != null) {
                val dsFloat = getDistance(loc)
                if (dsFloat > 0f) {
                    dist = dsFloat.toInt()
                    //dist = dsFloat / 1000f
                } else return 0f
            } else return 0f

            return dist.toFloat() / 1000f
        } else {
            val pa1_sp = addr.split(" ")
            val loc : MutableList<String>? = mutableListOf()
            pa1_sp.forEach {
                if (it.isNotEmpty()) {
                    loc!!.add(it)
                }
            }
            if (debugmode)
                Log.i("calcuteDistanceHelper"," loc split = $loc / loc split count = ${loc!!.count()}")

            val ppLoc: Location = Location(LocationManager.PASSIVE_PROVIDER)
            if (loc!!.get(0).contains(",") || loc.get(1).contains(",")) {
                val temp = addr.split(",")
                ppLoc.latitude = temp[0].toDouble()
                if (temp[1] == " " || temp[1].isEmpty() || temp[1] == ",") ppLoc.longitude = temp[2].toDouble()
                else ppLoc.longitude = temp[1].toDouble()
            }
            else {
                ppLoc.latitude = loc[0].toDouble()
                if (loc[1] == " " || loc[1].isEmpty() || loc[1] == ",") ppLoc.longitude = loc[2].toDouble()
                else ppLoc.longitude = loc[1].toDouble()
            }

            val currLoc = (activity as MainActivity).getLastLocation()
            if (currLoc != null) {
                val disValue = currLoc.distanceTo(ppLoc).toInt()

                return disValue/ 1000f
            }
            return 0f
        }
    }
}