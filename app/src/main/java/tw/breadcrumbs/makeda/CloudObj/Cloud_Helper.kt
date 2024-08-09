package tw.breadcrumbs.makeda.CloudObj

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Context
import android.os.AsyncTask
import android.util.Log
import java.io.*
import java.net.HttpURLConnection
import java.net.URL


class Cloud_Helper(@field:SuppressLint("StaticFieldLeak") val context: Context, val callback: (String?) -> Unit)
    : AsyncTask<String, Unit, String>() {
    private val debugmode = false
    private var country = ""
    private var city = ""

    override fun doInBackground(vararg params: String): String? {
        //TODO
        return null
    }

    fun readStream(inputStream: BufferedInputStream): String {
        val bufferedReader = BufferedReader(InputStreamReader(inputStream))
        val stringBuilder = StringBuilder()
        bufferedReader.forEachLine { stringBuilder.append(it) }
        return stringBuilder.toString()
    }


    fun Get_Cloud_PPS_InCity(in_country:String, in_city:String)//:List<PPModel>
    {
        //TODO
    }

    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)
        callback(result)
    }

    fun getJsonFromURL(wantedURL: String) : String {
        return URL(wantedURL).readText()
    }

}