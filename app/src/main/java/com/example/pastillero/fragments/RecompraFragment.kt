package com.example.pastillero.fragments

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.pastillero.R
import com.example.pastillero.activities.MainActivity
import com.example.pastillero.databinding.FragmentRecompraBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.android.synthetic.main.fragment_editar_tto.*

class RecompraFragment : Fragment() {

    private var _binding: FragmentRecompraBinding? = null
    private val binding get() = _binding!!
    private lateinit var medName: String
    private lateinit var amount: String
    private lateinit var idMed: String
    private lateinit var idTto: String
    private lateinit var oldStock: String
    private lateinit var frecuencia: String
    private lateinit var idAlarmas: String
    private lateinit var diasRecompra: String
    lateinit var alarmManager: AlarmManager
    val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentRecompraBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val db = FirebaseFirestore.getInstance()
        val btnConfRecompra = root.findViewById<View>(R.id.btnConfRecompra)
        val btnCancelarRecompra = root.findViewById<View>(R.id.btnCancelarRecompra)
        alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager

        idTto = activity?.intent?.getStringExtra("idTto").toString()
        idMed = activity?.intent?.getStringExtra("idMed").toString()
        medName = activity?.intent?.getStringExtra("medName").toString()
        amount = activity?.intent?.getStringExtra("amount").toString()
        oldStock = activity?.intent?.getStringExtra("stock").toString()
        frecuencia = activity?.intent?.getStringExtra("frecuencia").toString()
        diasRecompra = activity?.intent?.getStringExtra("diasRecompra").toString()

        db.collection("treatments").document(idTto).get().addOnSuccessListener { doc ->
            idAlarmas = doc.get("idAlarmas").toString()
        }


        btnConfRecompra.setOnClickListener{
            db.collection("treatments").document(idTto).set(
                hashMapOf(
                    "stock" to oldStock.toInt() + amount.toInt()
                ), SetOptions.merge()
            ).addOnSuccessListener {
                idAlarmas = idAlarmas.replace("[","")
                idAlarmas = idAlarmas.replace("]","")
                val strings = idAlarmas.toString().split(", ").toTypedArray()
                alarmUpdate(strings[0].toInt(), "ALARMA", frecuencia.toInt())
                var lapsoFin = ((oldStock.toFloat()*frecuencia.toFloat())/24).toFloat()
                var lapsoReceta = lapsoFin - frecuencia.toInt()
                var lapsoRecompra = lapsoFin - diasRecompra.toInt()
                alertUpdate(strings[1].toInt(), "Receta", lapsoReceta.toInt())
                alertUpdate(strings[2].toInt(), "Recompra", lapsoRecompra.toInt())
                alertUpdate(strings[3].toInt(), "Fin de tratamiento", frecuencia.toInt())
                Toast.makeText(requireActivity(), "Se ha recomprado "+medName, Toast.LENGTH_LONG).show()
                findNavController().navigate(R.id.navigation_home)
            }
        }

        btnCancelarRecompra.setOnClickListener{
            findNavController().navigate(R.id.ttoDetailFragment)
        }

        return root
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

