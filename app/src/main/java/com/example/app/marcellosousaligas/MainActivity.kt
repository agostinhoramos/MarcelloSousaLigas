package com.example.app.marcellosousaligas

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.app.marcellosousaligas.data.MovieListItem
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.jsoup.Jsoup
import java.io.IOException
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    private val CHANNEL_ID = "channel_id_example_01"
    private val notificationId = 101

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //retrieveWebInfo()
        //createNotificationChannel()
        perfectTime("20/10/10 08:22")
        buttonNotify.setOnClickListener{
            retrieveWebInfo()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun perfectTime(strDate: String) : Boolean{
        var playDate: LocalDateTime = LocalDateTime.parse(
            strDate,
            DateTimeFormatter.ofPattern("yy/MM/dd HH:mm")
        )
        var dateNow = LocalDateTime.now()
        if(playDate.compareTo(dateNow) >= 0){
            val minutesBtw: Long = Duration.between(dateNow, playDate).toMinutes()
            if( minutesBtw <= 8 ){
                return true
            }
        }else{
            return false
        }
        return false
    }

    private fun createNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val name = "League Marcello Sousa"
            val descText = "Alert League"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descText
            }
            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendNotification(title: String, desc: String){
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val PendingIntent = PendingIntent.getActivity(this, 0, intent, 0)
        val bitmapLargeIcon = BitmapFactory.decodeResource(applicationContext.resources, R.drawable.ic_launcher_foreground)
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setLargeIcon(bitmapLargeIcon)
            .setStyle(NotificationCompat.BigTextStyle().bigText(desc))
            .setContentIntent(PendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        with(NotificationManagerCompat.from(this)){
            notify(notificationId, builder.build())
        }
    }

    private fun getJsonDataFromAsset(context: Context, fileName: String): String? {
        val jsonString: String
        try {
            jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
        } catch (ioException: IOException) {
            ioException.printStackTrace()
            return null
        }
        return jsonString
    }

    fun parseJSON(string: String?): JSONObject {
        try {
            return JSONObject(string)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return JSONObject()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun retrieveWebInfo(){
        thread {

            try {

                // INITIALIZE
                var count = 0

                // network call, so run it in the background
                val html = Jsoup.connect("https://agostinhopinaramos.com/tests").get()
                val main = html.getElementsByClass("main")
                val next = main[0].getElementById("ongoing") //next
                val table = next.getElementsByTag("table")
                val tbody = table[0].getElementsByTag("tbody")
                val tr = tbody[0].getElementsByTag("tr")

                // JSON FILE
                val jsonFileString =
                    getJsonDataFromAsset(applicationContext, "portugal.json")
                val JSONObj: JSONObject = parseJSON(jsonFileString)

                for (item in tr) {
                    //-4 á 4 / 1.75 á 4.75 / 8.5 á 12.5
                    //8 para iniciar
                    var league = item.getElementsByTag("td")[0].text()
                    var playDate = item.getElementsByTag("td")[1].text()
                    var team = item.getElementsByTag("td")
                    var homeVSaway = team[2].text() + " VS " + team[4].text()

                    var line = item.getElementsByTag("td")[6].text()
                    val arr_line = line.split(" /").toTypedArray()
                    var h = (arr_line[0].trim()).toDouble()
                    var g = (arr_line[1].trim()).toDouble()
                    var c = (arr_line[2].trim()).toDouble()

                    // JSON FILE
                    for (objAttr in JSONObj.keys()) {
                        val data = JSONObj.getJSONArray(objAttr)
                        val len: Int = data.length() - 1;
                        for (x in 0..len) {
                            val a = data.getJSONArray(x)

                            var h_ = (a.get(0).toString().toDouble())
                            var g_ = (a.get(1).toString().toDouble())
                            var c_ = (a.get(2).toString().toDouble())
                            var message = a.get(3).toString()

                            if(
                                /*perfectTime(playDate) ||*/
                                (h==h_ && g==g_ && c==c_)
                            ){
                                println("$h==$h_ | $g==$g_ | $c==$c")
                                //sendNotification("---------------------------> Bet it now!!!", message)
                            }else{
                                //println("no event!")
                            }

                        }
                    }

                }

            }catch (e : IOException) {
                // handler
                println("DOM error!!!")
            }
        }
    }
}