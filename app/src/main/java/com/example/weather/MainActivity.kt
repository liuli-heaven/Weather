package com.example.weather

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.PreviewActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.weather.adapter.DayDataAdapter
import com.example.weather.adapter.HourDataAdapter
import com.example.weather.adapter.WeatherAdapter
import com.example.weather.data.EnvironmentPram
import com.example.weather.data.SunData
import com.example.weather.data.Temperature
import com.example.weather.data.Tip
import com.example.weather.data.WeatherCurrentData
import com.example.weather.data.WeatherDayData
import com.example.weather.data.WeatherDayItem
import com.example.weather.data.WeatherHourData
import com.example.weather.data.WeatherHourItem
import com.example.weather.net.generateJWT
import com.example.weather.ui.theme.WeatherTheme
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val dayItems: MutableList<WeatherDayData> = mutableListOf(
            WeatherDayData(Temperature(10, 20), "3月20日", "晴", "星期一"),
            WeatherDayData(Temperature(15, 22), "3月21日", "晴", "星期二"),
            WeatherDayData(Temperature(11, 18), "3月22日", "中雨", "星期三"),
            WeatherDayData(Temperature(15, 21), "3月23日", "晴", "星期四"),
            WeatherDayData(Temperature(12, 17), "3月24日", "阴", "星期五"),
            WeatherDayData(Temperature(18, 25), "3月25日", "晴", "星期六"),
            WeatherDayData(Temperature(14, 20), "3月26日", "小雨", "星期日"),
        )

        val hourItems: MutableList<WeatherHourData> = mutableListOf(
            WeatherHourData("晴", 14, "2:00"),
            WeatherHourData("晴", 16, "4:00"),
            WeatherHourData("晴", 18, "6:00"),
            WeatherHourData("晴", 20, "8:00"),
            WeatherHourData("晴", 22, "10:00"),
            WeatherHourData("晴", 24, "12:00"),
            WeatherHourData("晴", 25, "14:00"),
        )

        val items = mutableListOf(
            WeatherCurrentData("哈尔滨", Temperature(14, 25), 20, "晴"),
            Tip("每小时"),
            WeatherHourItem(hourItems),
            Tip("近日天气"),
            WeatherDayItem(dayItems),
            EnvironmentPram(20, 20, "0级", "30", "四级", "东南风"),
            SunData("6:00", "18:00", "渐盈凸月")

        )
        val adapter = WeatherAdapter(items)
        recyclerView.adapter = adapter

    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = name,
        modifier = modifier
    )
}
