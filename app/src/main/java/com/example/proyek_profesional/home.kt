package com.example.proyek_profesional

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.Volley
import com.android.volley.toolbox.JsonObjectRequest
import org.json.JSONObject
import android.os.Handler
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.example.proyek_profesional.databinding.ActivityHomeBinding
import com.example.proyek_profesional.databinding.ActivityMainBinding
import com.google.firebase.database.FirebaseDatabase

class home : AppCompatActivity() {

    lateinit var binding: ActivityHomeBinding
    private lateinit var notificationManager: NotificationManagerCompat
    private lateinit var tv1: TextView
    private lateinit var tv2: TextView
    private lateinit var tv3: TextView
    private var imageClick = false
    private var image2Click = false
    private var lastNotificationField1: Int = -1
    private var lastNotificationField2: Int = -1
    private var lastNotificationField3: Int = -1


    private val handler = Handler()
    private val delay = 10000L // 10 detik

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)


        tv1 = findViewById(R.id.tv_1)
        tv2 = findViewById(R.id.tv_2)
        tv3 = findViewById(R.id.tv_3)

        notificationManager = NotificationManagerCompat.from(this)


        // Memanggil fungsi getLatestData untuk pertama kali
        getLatestData()

        // Mengulang permintaan setiap delay
        handler.postDelayed(object : Runnable {
            override fun run() {
                getLatestData()
                handler.postDelayed(this, delay)
            }
        }, delay)

        binding.btnKeluar.setOnClickListener {
            val intent = Intent(this, Logout::class.java)
            startActivity(intent)
        }


    }

    private fun getLatestData() {

        val url = "https://api.thingspeak.com/channels/1933360/feeds.json?results=1"
        val queue = Volley.newRequestQueue(this)

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            Response.Listener { response ->
                val feedArray = response.getJSONArray("feeds")
                if (feedArray.length() > 0) {
                    val latestFeed = feedArray.getJSONObject(0)
                    val field1 = latestFeed.getString("field1")
                    val field2 = latestFeed.getString("field2")
                    val field3 = latestFeed.getString("field3")
                    tv1.text = field1
                    tv2.text = field2
                    tv3.text = field3
                    val field1Value = field1.toIntOrNull()
                    val field2Value = field2.toIntOrNull()
                    val field3Value = field3.toIntOrNull()

                    if (field1Value != null && field1Value < 1 && field1Value != lastNotificationField1){
                        showNotification("PH Tidak Sesuai", "Nilai PH: $field1Value")
                        lastNotificationField1 = field1Value
                    }
                    if (field1Value != null && field1Value > 6.5  && field1Value != lastNotificationField1) {
                        showNotification("PH Tidak Sesuai", "Nilai PH: $field1Value")
                        lastNotificationField1 = field1Value
                    }
                    if (field2Value != null && field2Value < 25 && field2Value != lastNotificationField2) {
                        showNotification("SUHU Tidak Sesuai", "Nilai SUHU: $field2Value")
                        lastNotificationField2 = field2Value
                    }
                    if (field2Value != null && field2Value > 30  && field2Value != lastNotificationField2) {
                        showNotification("SUHU Tidak Sesuai", "Nilai SUHU: $field2Value")
                        lastNotificationField2 = field2Value
                    }

                    if (field3Value != null && field3Value <105 && field3Value != lastNotificationField3) {
                        showNotification("TDS Tidak Sesuai", "Nilai TDS: $field3Value")
                        lastNotificationField3 = field3Value
                    }
                    if (field3Value != null && field3Value > 140  && field3Value != lastNotificationField3) {
                        showNotification("TDS Tidak Sesuai", "Nilai TDS: $field3Value")
                        lastNotificationField3 = field3Value
                    }
                }
            },
            Response.ErrorListener { error ->
                Log.e("TAG", "Error: ${error.message}")
            }
        )
        queue.add(jsonObjectRequest)
    }

    fun gantiGambar(view: View){
        when(view.id){
            R.id.btn -> {
                imageClick = !imageClick
                if (imageClick){
                    (view as ImageButton).setBackgroundResource(R.drawable.on)
                    kirimData("Pompa", 1)
                } else{
                    (view as  ImageButton).setBackgroundResource(R.drawable.off)
                    kirimData("Pompa", 0)
                }
            }
            R.id.btn1 -> {
                image2Click = !image2Click
                if (image2Click) {
                    (view as ImageButton).setBackgroundResource(R.drawable.on)
                    kirimData("Lampu",1)
                } else {
                    (view as ImageButton).setBackgroundResource(R.drawable.off)
                    kirimData("Lampu" ,0)
                }
            }
        }
    }

    private fun kirimData(field: String, value: Int) {
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference(field)
        myRef.setValue(value)
            .addOnSuccessListener {
                Log.d("TAG", "Data successfully sent to Firebase")
            }
            .addOnFailureListener {
                Log.d("TAG", "Error sending data to Firebase: ${it.message}")
            }
    }

    private fun showNotification(title: String, message: String) {
        val intent = Intent(this,home::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent,0)
        val builder = NotificationCompat.Builder(this, notify.CHANNEL_1_ID)
            .setSmallIcon(R.drawable.icon_home)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notification = builder.build()
        notificationManager.notify(1, notification)
        }


    private fun keluarAplikasi() {
        AlertDialog.Builder(this)
            .setMessage("Apakah yakin keluar?")
            .setPositiveButton("Ya") { _, _ ->
                finishAffinity() // mengakhiri semua aktivitas yang terbuka
                System.exit(0) // keluar dari aplikasi
            }
            .setNegativeButton("Tidak", null)
            .show()
    }

    override fun onBackPressed() {
        // panggil fungsi keluarAplikasi()
        keluarAplikasi()
    }

}