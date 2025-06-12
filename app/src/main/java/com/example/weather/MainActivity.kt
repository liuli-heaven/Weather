package com.example.weather

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.weather.adapter.WeatherAdapter
import com.example.weather.data.DatabaseHelper
import com.example.weather.data.EnvironmentPram
import com.example.weather.data.SunData
import com.example.weather.data.Temperature
import com.example.weather.data.Tip
import com.example.weather.data.WeatherCurrentData
import com.example.weather.data.WeatherDataCallback
import com.example.weather.data.WeatherDayData
import com.example.weather.data.WeatherDayItem
import com.example.weather.data.WeatherHourData
import com.example.weather.data.WeatherHourItem
import com.example.weather.data.WeatherListData

class MainActivity : ComponentActivity(), WeatherDataCallback {
    lateinit var recyclerView: RecyclerView
    val db = DatabaseHelper(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        db.setCallback(this)

        val dayItems: MutableList<WeatherDayData> = mutableListOf(
            WeatherDayData(Temperature(10, 20), "3月20日", "晴", "星期一"),
            WeatherDayData(Temperature(15, 22), "3月21日", "晴", "星期二"),
        )

        val hourItems: MutableList<WeatherHourData> = mutableListOf(
            WeatherHourData("晴", 14, "2:00", "哈尔滨"),
        )

        val items = mutableListOf(
            WeatherCurrentData("哈尔滨", Temperature(14, 25), 20, "晴"),
            WeatherHourItem(hourItems),
            Tip("近日天气"),
            WeatherDayItem(dayItems),
            Tip("环境质量"),
            EnvironmentPram(20, 20, 0, "30", "四级", "东南风"),
            Tip("日出日落"),
            SunData("6:00", "18:00", "渐盈凸月")
        )
        val adapter = WeatherAdapter(items)
        recyclerView.adapter = adapter

        db.queryWeather("5d4091c3910057d608e1feda5e55168e")

    }

    @SuppressLint("NotifyDataSetChanged")
    override fun drawDayData(weatherListData: WeatherListData){
        val adapter = recyclerView.adapter as WeatherAdapter
        val dayList =  adapter.items[3] as WeatherDayItem
        dayList.dataList.clear()
        for (data in weatherListData.dataList){
            val date = "${data.fc_time.substring(4, 6)}月${data.fc_time.substring(6)}日"
            dayList.dataList.add(WeatherDayData(Temperature(data.tem_min, data.tem_max), date, data.wp, data.week))
        }

        val firstData = weatherListData.dataList[0]
        adapter.items[5] = EnvironmentPram(firstData.rh, firstData.pre_pro, firstData.uv_level, firstData.cloud_cover,
            firstData.ws_desc, firstData.wd_desc)
        adapter.items[7] = SunData(firstData.sunrise, firstData.sunset, firstData.moonphase)
        adapter.notifyDataSetChanged()

    }
    override fun drawHourData(weatherHourItem: WeatherHourItem, dateTemperature: Temperature?){
        val adapter = recyclerView.adapter as WeatherAdapter
        val firstData = weatherHourItem.dataList[0]
        if (dateTemperature == null){
            adapter.items[0] = WeatherCurrentData(firstData.city, Temperature(0, 0), firstData.tem, firstData.wp)
        } else {
            adapter.items[0] = WeatherCurrentData(firstData.city, dateTemperature, firstData.tem, firstData.wp)
        }
        adapter.items[1] = weatherHourItem
        adapter.notifyItemChanged(0)
        adapter.notifyItemChanged(1)
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = name,
        modifier = modifier
    )
}
