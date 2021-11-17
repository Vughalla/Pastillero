package com.example.pastillero.activities

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat.getSystemService
import androidx.navigation.NavDeepLinkBuilder
import androidx.navigation.Navigation.findNavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.pastillero.R
import com.example.pastillero.fragments.FormFragment
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.android.synthetic.main.fragment_settings.*
import java.util.*

class MainActivity : AppCompatActivity() {

    lateinit var context : Context
    lateinit var alarmManager: AlarmManager
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment_activity_bottom_nav)

        val appBarConfiguration = AppBarConfiguration(setOf(
                R.id.navigation_home, R.id.navigation_settings, R.id.navigation_profile
        ))
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        context = this
        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        createNotificationChannel()

 
    }

    private fun createNotificationChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val name : CharSequence = "Canal de Farmacia Central Oeste"
            val description = "Recordatorios de tratamientos médicos"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("Farmacia Central Oeste", name, importance)

            channel.description = description
            val notificationManager = getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    class AlarmReceiver: BroadcastReceiver() {


        lateinit var datosRecibidos: Bundle
        override fun onReceive(context: Context?, intent: Intent?) {

            val db = FirebaseFirestore.getInstance()
            datosRecibidos = intent?.getExtras()!!
            //val tratActual = datosRecibidos.getSerializable("idMed") as String
            val idAlarm = datosRecibidos.getSerializable("idAl") as Int
            val nameMed = datosRecibidos.getSerializable("nameMed") as String
            val mensaje = datosRecibidos.getSerializable("mensaje") as String

            val i = Intent(context, MainActivity::class.java)
            intent!!.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            val pendingIntent = PendingIntent.getActivity(context, idAlarm, intent, 0)

            val bitmap = BitmapFactory.decodeResource(context?.resources, R.drawable.ic_launcher_foreground)
            val bitmapLargeIcon = BitmapFactory.decodeResource(context?.resources, R.drawable.ic_launcher_foreground)

            val builder = NotificationCompat.Builder(context!!, "Farmacia Central Oeste")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("Farmacia Central Oeste")
                .setContentText(""+mensaje +" - "+ nameMed +"")
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setLargeIcon(bitmapLargeIcon)
                .setColorized(true)
                .setColor(Color.GREEN)
            //.setStyle(NotificationCompat.BigPictureStyle().bigPicture(bitmap))
            //.setStyle(NotificationCompat.InboxStyle)
                .setStyle(NotificationCompat.BigTextStyle().bigText("Debería tomar la dosis del medicamento " + nameMed))


            with(NotificationManagerCompat.from(context)) {
                notify(123, builder.build())
            }

            if(mensaje == "ALARMA"){
                db.collection("treatments")
                    .get()
                    .addOnSuccessListener { tratamientos ->
                        for (tratamiento in tratamientos){
                            val alarmas = tratamiento.get("idAlarmas")
                            if (alarmas.toString().contains(idAlarm.toString())){
                                updateStock(tratamiento.id,tratamiento.get("stock").toString().toInt(),idAlarm )
                            }
                        }
                    }
            }

            //Toast.makeText(context, ""+mensaje +" - "+ nameMed +"", Toast.LENGTH_SHORT).show()
        }

        private fun updateStock(idTratamiento: String, stock: Int, idAlarm: Int) {
            val db = FirebaseFirestore.getInstance()
            db.collection("treatments").document(idTratamiento)
                .set(
                    hashMapOf(
                        "stock" to stock-1
                    ), SetOptions.merge()
                ).addOnSuccessListener {
                    
                }
        }
    }

    override fun onResume() {
        super.onResume()
        val docRef = db.collection("treatments").document()
        docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("___________", "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                Log.d("________", "Current data: ${snapshot.data}")
            } else {
                Log.d("________", "Current data: null")
            }
        }
    }
    private fun alarmCancel(idAlarma : Int) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, idAlarma, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        alarmManager.cancel(pendingIntent)
    }
    //----------  MÉTODO PARA ACTUALIZACIÓN DE ALARMAS ------------------//
    /*private fun alarmUpdate(tratamientoActual: Tratamiento) {
        val seconds = tratamientoActual.frecuenciaHoras * 60 * 60 * 1000 //edt_timer.text.toString().toInt() * 1000
        val intent = Intent(context, AlarmReceiver::class.java)
        intent.putExtra("trat",tratamientoActual)
        val pendingIntent = PendingIntent.getBroadcast(context, tratamientoActual.idAlarma, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        Toast.makeText(context, "La alarma del tratamiento " + tratamientoActual.nombre + " fue actualizada a las " + Date().toString(), Toast.LENGTH_LONG).show()
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + /seconds/ 10000, pendingIntent)
    }*/



}


