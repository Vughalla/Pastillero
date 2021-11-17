package com.example.pastillero.fragments

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.pastillero.R
import com.example.pastillero.activities.MainActivity
import com.example.pastillero.databinding.FragmentEditarTtoBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.android.synthetic.main.fragment_editar_tto.*

class EditarTto : Fragment() {

    private var _binding: FragmentEditarTtoBinding? = null
    private val binding get() = _binding!!
    private lateinit var medName: String
    private lateinit var idTto: String
    private lateinit var stock: String
    private lateinit var frecuencia: String
    private lateinit var diasRecompra: String
    private lateinit var diasReceta: String
    private lateinit var idAlarmas: String
    private lateinit var idMed: String
    lateinit var alarmManager: AlarmManager

    val db = FirebaseFirestore.getInstance()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditarTtoBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val btn_actualizar_tto = root.findViewById<View>(R.id.btn_actualizar_tto)
        val btn_cancelar_actualizar_tto = root.findViewById<View>(R.id.btn_cancelar_actualizar_tto)
        alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager

        idTto = activity?.intent?.getStringExtra("idTto").toString()
        medName = activity?.intent?.getStringExtra("medName").toString()
        stock = activity?.intent?.getStringExtra("stock").toString()
        frecuencia = activity?.intent?.getStringExtra("frecuencia").toString()
        diasRecompra = activity?.intent?.getStringExtra("diasRecompra").toString()
        diasReceta = activity?.intent?.getStringExtra("diasReceta").toString()

        db.collection("treatments").document(idTto).get().addOnSuccessListener { doc ->
            et_stock.hint = doc.get("stock").toString()
            et_frecuencia.hint = doc.get("frequency").toString()
            et_diasRecompra.hint = doc.get("days_buy_back").toString()
            et_diasReceta.hint = doc.get("days_recepy").toString()
            idAlarmas = doc.get("idAlarmas").toString()
            idMed = doc.get("id_med").toString()
        }


        btn_actualizar_tto.setOnClickListener{
            var new_stock = et_stock.text.toString().trim { it <= ' ' }
            var new_frecuencia = et_frecuencia.text.toString().trim { it <= ' ' }
            var new_diasRecompra = et_diasRecompra.text.toString().trim { it <= ' ' }
            var new_diasReceta = et_diasReceta.text.toString().trim { it <= ' ' }

            if (new_stock == "") {
                new_stock = stock
            }
            if (new_frecuencia == "") {
                new_frecuencia = frecuencia
            }
            if (new_diasRecompra == "") {
                new_diasRecompra = diasRecompra
            }
            if (new_diasReceta == "") {
                new_diasReceta = diasReceta
            }

            when {
                new_diasReceta.toFloat() > maxDiasRecordatorio() -> {
                    Toast.makeText(
                        requireActivity(),
                        "Los dias de aviso de nueva receta no pueden superar los ${maxDiasRecordatorio()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                new_diasRecompra.toFloat() > maxDiasRecordatorio() -> {
                    Toast.makeText(
                        requireActivity(),
                        "Los dias de aviso de nueva receta no pueden superar los ${maxDiasRecordatorio()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            db.collection("treatments").document(idTto).set(
                hashMapOf(
                    "stock" to new_stock,
                    "frequency" to new_frecuencia,
                    "days_buy_back" to new_diasRecompra,
                    "days_recepy" to new_diasReceta
                ), SetOptions.merge()
            ).addOnSuccessListener {
                idAlarmas = idAlarmas.replace("[","")
                idAlarmas = idAlarmas.replace("]","")
                val strings = idAlarmas.toString().split(", ").toTypedArray()
                alarmUpdate(strings[0].toInt(), "ALARMA", new_frecuencia.toInt())
                var lapsoFin = ((new_stock.toFloat()*new_frecuencia.toFloat())/24).toFloat()
                var lapsoReceta = lapsoFin - new_diasReceta.toInt()
                var lapsoRecompra = lapsoFin - new_diasRecompra.toInt()
                alertUpdate(strings[1].toInt(), "Receta", lapsoReceta.toInt())
                alertUpdate(strings[2].toInt(), "Recompra", lapsoRecompra.toInt())
                alertUpdate(strings[3].toInt(), "Fin de tratamiento", frecuencia.toInt())
                Toast.makeText(requireActivity(), "Se ha actualizado el tratamiento "+medName, Toast.LENGTH_LONG).show()
                findNavController().navigate(R.id.navigation_home)
            }

        }

        btn_cancelar_actualizar_tto.setOnClickListener{
            findNavController().navigate(R.id.ttoDetailFragment)
        }

        return root
    }

    private fun maxDiasRecordatorio(): Float {
        var num: Float = (stock.toFloat() * frecuencia.toFloat()) / 24
        return num
    }

    private fun alarmUpdate(id : Int, mensaje : String, frecuMed: Int) {

        db.collection("meds").document(idMed).get().addOnSuccessListener { doc ->
            var medName = doc.get("name").toString()
            val seconds = frecuMed * 60 * 60 * 1000 //edt_timer.text.toString().toInt() * 1000
            val intent = Intent(context, MainActivity.AlarmReceiver::class.java)
            //intent.putExtra("idMed",idMed)
            intent.putExtra("idAl",id)
            intent.putExtra("nameMed",medName)
            intent.putExtra("mensaje",mensaje)

            val pendingIntent = PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            Toast.makeText(context, "Alarma actualizada: " + medName, Toast.LENGTH_SHORT).show()
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() /* + seconds*/ ,60000/*seconds*/, pendingIntent)

        }
    }

    private fun alertUpdate(id : Int, mensaje : String, frecuMed: Int) {
        db.collection("meds").document(idMed).get().addOnSuccessListener { doc ->
            var medName = doc.get("name").toString()
            val seconds = frecuMed * 60 * 60 * 1000 //edt_timer.text.toString().toInt() * 1000
            val intent = Intent(context, MainActivity.AlarmReceiver::class.java)
            //intent.putExtra("idMed",idMed)
            intent.putExtra("idAl",id)
            intent.putExtra("nameMed",medName)
            intent.putExtra("mensaje",mensaje)

            val pendingIntent = PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            Toast.makeText(context, "Alarma actualizada: " + medName, Toast.LENGTH_SHORT).show()
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + /*seconds*/ 10000, pendingIntent)
        }
    }
}