package com.example.app.marcellosousaligas

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.app.marcellosousaligas.data.MovieListItem
import com.google.gson.Gson
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
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        retrieveWebInfo()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun perfectTime(strDate: String) : Boolean{
        var playDate = LocalDateTime.parse(
            strDate,
            DateTimeFormatter.ofPattern("yy/MM/dd HH:mm", Locale.ENGLISH)
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
            // network call, so run it in the background
            val html = Jsoup.connect("https://www.scorebing.com/league/35").get()
            val main = html.getElementsByClass("main")
            val next = main[0].getElementById("next")
            val table = next.getElementsByTag("table")
            val tbody = table[0].getElementsByTag("tbody")
            val tr = tbody[0].getElementsByTag("tr")

            val movieList = ArrayList<MovieListItem>()

            val jsonFileString = getJsonDataFromAsset(applicationContext, "portugal.json")
            val JSONObj : JSONObject = parseJSON(jsonFileString)

            for (objAttr in JSONObj.keys()){
                val data = JSONObj.getJSONArray(objAttr)
                val len: Int = data.length()-1;
                for (x in 0..len){
                    val a =  data.getJSONArray(x)

                    for(item in tr){
                        //-4 á 4 / 1.75 á 4.75 / 8.5 á 12.5
                        //8 para iniciar
                        var league = item.getElementsByTag("td")[0].text()
                        var playDate = item.getElementsByTag("td")[1].text()
                        var team = item.getElementsByTag("td")
                        var homeVSaway = team[2].text() +" VS "+ team[4].text()

                        var line = item.getElementsByTag("td")
                        var h = (line[9].text().trim()).toDouble()
                        var g = (line[10].text().trim()).toDouble()
                        var c = (line[11].text().trim()).toDouble()
                        var h_ = (a.get(0).toString().toDouble())
                        var g_ = (a.get(1).toString().toDouble())
                        var c_ = (a.get(2).toString().toDouble())
                        var message = a.get(3).toString()

                        if(
                            perfectTime(playDate) &&
                            (h==h_ && g==g_ && c==c_)
                        ){
                            println( "The message is ------> " + message )
                        }else{
                            println("No sorry!!!")
                        }

                        //val movieName = movieItem.text()
                        //val movieImageUrl = movieItem.getElementsByTag("img")[0].absUrl("data-original").toString()
                        //movieList.add(MovieListItem(movieName, movieImageUrl))
                    }
                }
            }

            // can't access UI elements from the background thread
            /*this.runOnUiThread{
                val recyclerViewAdapter = RecyclerViewAdapter(movieList)
                val linearLayoutManager = LinearLayoutManager(this)
                recyclerView.layoutManager = linearLayoutManager
                recyclerView.adapter = recyclerViewAdapter
            }*/
        }
    }
}