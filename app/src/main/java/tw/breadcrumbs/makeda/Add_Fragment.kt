package tw.breadcrumbs.makeda

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.util.*
import android.content.ContentValues.TAG
import android.graphics.Color
import android.location.Geocoder
import android.location.Location
import android.widget.*
import kotlinx.android.synthetic.main.add_fragment.*
import tw.breadcrumbs.makeda.DBModules.ItemDAO
import tw.breadcrumbs.makeda.dataModel.CountriesNCities
import tw.breadcrumbs.makeda.dataModel.PPModel
import java.io.IOException


class Add_Fragment : androidx.fragment.app.Fragment() {
    private val debugmode = false
    var country_spinner:Spinner? = null
    var city_spinner:Spinner? = null
    val globalCountries = CountriesNCities()
    private var sel_country = globalCountries.country_str[0]
    private var sel_city = globalCountries.TW_areas_str[0]
    private var item_dao : ItemDAO? = null
    private var pp_model_seted : PPModel? = null
    private var init = 0
    private var locate_switch = 0
    var save_button:ImageButton? = null
    var pp_name:EditText? = null
    var pp_phone:EditText? = null
    var pp_addr:EditText? = null
    var pp_fb:EditText? = null
    var pp_web:EditText? = null
    var pp_bloginfo:EditText? = null
    var pp_opentime:EditText? = null
    var pp_tag_note:EditText? = null
    var pp_descrip:EditText? = null



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.add_fragment, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        item_dao = ItemDAO(this@Add_Fragment.context!!)
        SetupView()
        locateFunSetup()
        (activity as MainActivity).setNavigationID(1)
    }

    private fun disableEditText(editText: EditText) {
        editText.isFocusable = false
        editText.isEnabled = false
        editText.isCursorVisible = false
        editText.keyListener = null
        editText.setBackgroundColor(Color.TRANSPARENT)
    }

    fun spin_data_setup() {
        // update city and country select item
        var idx = 0
        for (i in CountriesNCities().country_str) {
            idx += 1
            if (sel_country == i) {
                break
            }
        }
        var cities: Array<String> = arrayOf()
        pp_country_in.setSelection(idx - 1)

        // setup City
        idx = 0
        when (sel_country) {
            CountriesNCities().country_str[0] ->
                cities = CountriesNCities().TW_areas_str
            CountriesNCities().country_str[1] ->
                cities = CountriesNCities().JP_areas_ZH_str
            CountriesNCities().country_str[2] ->
                cities = CountriesNCities().CN_areas_str
        }
        for (i in cities) {
            idx += 1
            if (i == sel_city) {
                break
            }
        }
        //setup_city_spin(R.id.pp_city_view, cities)
        if (debugmode)
            Log.i(TAG, "Review_Fragment : spin_data_setup : city select index : " + idx)
        pp_city_in.setSelection(idx - 1)
    }

    @SuppressLint("SetTextI18n")
    fun locateFunSetup() {
        locateNowBtn.setOnClickListener {
            var country: String = ""
            var city: String = " - "
            var street: String = " "
            var town: String = " "
            val currLoc: Location? = (activity as MainActivity).getLastLocation()

            try {
                val geoCoder = Geocoder(this@Add_Fragment.context)
                val placemark = geoCoder.getFromLocation(currLoc!!.latitude, currLoc.longitude, 1)
                if (debugmode)
                    Log.i("MainActivity", "placemark:$placemark")
                if (placemark[0].subAdminArea == null) {
                    city = placemark[0].adminArea.toString()
                } else if (placemark[0].adminArea == null) {
                    city = placemark[0].subAdminArea.toString()
                } else if (placemark[0].adminArea == null && placemark[0].subAdminArea == null) {

                }
                if (placemark[0].thoroughfare == null && placemark[0].subThoroughfare != null) {
                    if (placemark[0].subThoroughfare.isNotEmpty()) street = placemark[0].subThoroughfare
                } else {
                    if (placemark[0].thoroughfare.isNotEmpty()) street = placemark[0].thoroughfare
                }

                if (placemark[0].locality != null) {
                    town = placemark[0].locality.toString()
                }
                country = placemark[0].countryName.toString()

                sel_country = country
                sel_city = city

                pp_addr!!.run {
                    if (locate_switch == 1) {
                        setText(" ${currLoc.latitude} ${currLoc.longitude}")
                        locate_switch = 0
                    } else {
                        locate_switch = 1
                        setText("$country $city $town $street")
                    }
                }


                spin_data_setup()
            } catch (e: IOException) {
                if (debugmode)
                    Log.i("Notif_Fragment", " error : e")
            }
        }
    }

    fun pp_setup(){
        if (pp_model_seted == null) return
        pp_model_seted.let {
            pp_name!!.setText(pp_model_seted!!.name)
            disableEditText(pp_name!!)
            pp_phone!!.setText(pp_model_seted!!.phone)
            disableEditText(pp_phone!!)
            pp_addr!!.setText(pp_model_seted!!.addr)
            disableEditText(pp_addr!!)
            pp_fb!!.setText(pp_model_seted!!.fb)
            disableEditText(pp_fb!!)
            pp_web!!.setText(pp_model_seted!!.web)
            disableEditText(pp_web!!)
            pp_bloginfo!!.setText(pp_model_seted!!.blogInfo)
            disableEditText(pp_bloginfo!!)
            pp_opentime!!.setText(pp_model_seted!!.opentime)
            disableEditText(pp_opentime!!)
            pp_tag_note!!.setText(pp_model_seted!!.tag_note)
            disableEditText(pp_tag_note!!)
            pp_descrip!!.setText(pp_model_seted!!.descrip)
            disableEditText(pp_descrip!!)
            pp_saveButton.visibility = View.GONE
        }
    }

    fun pp_in_cleanup() {
        pp_name!!.setText("")
        pp_phone!!.setText("")
        pp_addr!!.setText("")
        pp_fb!!.setText("")
        pp_web!!.setText("")
        pp_bloginfo!!.setText("")
        pp_opentime!!.setText("")
        pp_tag_note!!.setText("")
        pp_descrip!!.setText("")
    }

    private fun SetupView() {
        save_button = view!!.findViewById(R.id.pp_saveButton) as ImageButton
        pp_name = view!!.findViewById(R.id.pp_name_in) as EditText
        pp_phone = view!!.findViewById(R.id.pp_phone_in) as EditText
        pp_addr = view!!.findViewById(R.id.pp_addr_in) as EditText
        pp_fb = view!!.findViewById(R.id.pp_fb_in) as EditText
        pp_web = view!!.findViewById(R.id.pp_web_in) as EditText
        pp_bloginfo = view!!.findViewById(R.id.pp_bloginfo_in) as EditText
        pp_tag_note = view!!.findViewById(R.id.pp_tag_note_in) as EditText
        pp_opentime = view!!.findViewById(R.id.pp_opentime_in) as EditText
        pp_descrip = view!!.findViewById(R.id.pp_descrip_in) as EditText

        setup_country_spin(R.id.pp_country_in)
        sel_country.let {
            setup_city_spin(R.id.pp_city_in)
        }
        setup_spinner_listener()
        setup_button_listener()
    }

    fun setup_button_listener() {
        save_button!!.setOnClickListener {
            var country_city_spin_str = pp_country_in.selectedItem.toString() + " | " + pp_city_in.selectedItem.toString()
            if (debugmode)
                Log.i(TAG,"clicked")
            if (pp_name!!.text.toString().length > 0 && pp_addr!!.text.toString().length > 0) {
                val item = PPModel(
                    0, 0,pp_name!!.text.toString(), pp_phone!!.text.toString(),
                    country_city_spin_str,
                    pp_addr!!.text.toString(), pp_fb!!.text.toString(),
                    pp_web!!.text.toString(), pp_bloginfo!!.text.toString(),
                    pp_opentime!!.text.toString(), pp_tag_note!!.text.toString(), pp_descrip!!.text.toString(),
                    " ", 0, 0
                )
                //ppmodel.name =
                item_dao.let {
                    if (item_dao!!.insert(item) == null) {
                        Toast.makeText(this@Add_Fragment.context, "資料存入錯誤", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this@Add_Fragment.context, pp_name!!.text.toString() +
                                "存入完成", Toast.LENGTH_LONG).show()
                        pp_in_cleanup()
                    }
                }
            } else {
                Toast.makeText(this@Add_Fragment.context, pp_name!!.text.toString() +
                        "請輸入完整資料", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun setup_spinner_listener() {
        if (country_spinner!!.onItemSelectedListener == null) {
            country_spinner!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(arg0: AdapterView<*>, arg1: View?, position: Int, id: Long) {
                    //if (debugmode) Toast.makeText(this@Add_Fragment.context, arg0.selectedItem.toString(), Toast.LENGTH_LONG).show()
                    if (sel_country != arg0.selectedItem.toString()) {
                        sel_country = arg0.selectedItem.toString()
                        if (arg0.selectedItemId.toInt() == 0) {
                            setup_city_spin(R.id.pp_city_in)//, globalCountries.TW_areas_str)
                        } else if (arg0.selectedItemId.toInt() == 1) {
                            setup_city_spin(R.id.pp_city_in)//, globalCountries!!.JP_areas_str)
                        } else if (arg0.selectedItemId.toInt() == 2) {
                            setup_city_spin(R.id.pp_city_in)//, globalCountries.CN_areas_str)
                        }
                    }
                }

                override fun onNothingSelected(arg0: AdapterView<*>) {
                    //if (debugmode) Toast.makeText(this@Add_Fragment.context, "您沒有選擇任何項目", Toast.LENGTH_LONG).show()
                }
            }
        }

        if (city_spinner!!.onItemSelectedListener == null) {
            city_spinner!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(arg0: AdapterView<*>, arg1: View?, position: Int, id: Long) {
                    //if (debugmode) Toast.makeText(this@Add_Fragment.context, arg0.selectedItem.toString(), Toast.LENGTH_LONG).show()
                    if (sel_city != arg0.selectedItem.toString()) {
                        sel_city = arg0.selectedItem.toString()
                    }
                }

                override fun onNothingSelected(arg0: AdapterView<*>) {
                    //if (debugmode) Toast.makeText(this@Add_Fragment.context, "您沒有選擇任何項目", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun setup_country_spin(ViewID: Int) {
        //對應控件
        country_spinner = view!!.findViewById(ViewID) as Spinner
        val adapter = ArrayAdapter(this@Add_Fragment.context!!, R.layout.spinner_item, globalCountries.country_str)
        //設置下拉列表的風格
        //adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        //將adapter 添加到spinner中
        country_spinner!!.setAdapter(adapter)

    }

    private fun setup_city_spin(ViewID: Int) {
        var city_array:Array<String>? = null
        sel_country.let {
            when(sel_country) {
                globalCountries.country_str[0] ->
                    city_array = globalCountries.TW_areas_str
                globalCountries.country_str[1] ->
                    city_array = globalCountries.JP_areas_ZH_str
                globalCountries.country_str[2] ->
                    city_array = globalCountries.CN_areas_str
            }
        }
        city_spinner = view!!.findViewById(ViewID) as Spinner
        val adapter = ArrayAdapter(this@Add_Fragment.context!!, R.layout.spinner_item, city_array!!)

        //設置下拉列表的風格
        //adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        //將adapter 添加到spinner中
        city_spinner!!.setAdapter(adapter)
    }
}