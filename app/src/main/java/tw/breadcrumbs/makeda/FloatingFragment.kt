package tw.breadcrumbs.makeda

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import tw.breadcrumbs.makeda.R

//import sun.jvm.hotspot.utilities.IntArray


//import sun.jvm.hotspot.utilities.IntArray


class FloatingFragment : Service() {
    private var mFloatingView: View? = null
    private var mWindowManager : WindowManager? = null

    public fun FloatingFragment() {

    }

    override fun onBind(intent: Intent?): IBinder? {
        val ibinder : IBinder? = null
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        return ibinder
    }

    override fun onCreate() {
        super.onCreate()
        mFloatingView = LayoutInflater.from(this).inflate(R.layout.speed_view, null);
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        //Specify the view position
        //Specify the view position
        params.gravity =
            Gravity.TOP or Gravity.START //Initially view will be added to top-left corner

        params.x = 0
        params.y = 100

        //Add the view to the window
        //Add the view to the window
        mWindowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        mWindowManager!!.addView(mFloatingView, params)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}