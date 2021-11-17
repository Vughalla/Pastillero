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
import com.example.pastillero.databinding.FragmentConfirmarBorrarTtoBinding
import com.google.firebase.firestore.FirebaseFirestore

class ConfirmarBorrarTto : Fragment() {

    private var _binding: FragmentConfirmarBorrarTtoBinding? = null
    private val binding get() = _binding!!
    lateinit var alarmManager: AlarmManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentConfirmarBorrarTtoBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val db = FirebaseFirestore.getInstance()
        val idTto = activity?.intent?.getStringExtra("idTto")
        val medName = activity?.intent?.getStringExtra("medName")
        alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val btnConfBorrarTto = root.findViewById<View>(R.id.btnConfBorrarTto)
        val btnCancelarBorrarTto = root.findViewById<View>(R.id.btnCancelarBorrarTto)

        btnConfBorrarTto.setOnClickListener{
            db.collection("treatments").document(idTto.toString())
                .get().addOnSuccessListener { doc ->
                    var ids = doc.get("idAlarmas")
                    ids = ids.toString().replace("[","")
                    ids = ids.toString().replace("]","")
                    val strings = ids.toString().split(", ").toTypedArray()
                    Log.d("_____", strings[0] + strings[1] + strings[2] + strings[3])
                    alarmCancel(strings[0].toInt())
                    alarmCancel(strings[1].toInt())
                    alarmCancel(strings[2].toInt())
                    alarmCancel(strings[3].toInt())
                    db.collection("treatments").document(idTto.toString())
                        .delete()
                        .addOnSuccessListener {

                            Toast.makeText(requireActivity(), "Se ha borrado el tratamiento "+medName, Toast.LENGTH_LONG).show()
                            findNavController().navigate(R.id.navigation_home)
                        }
                }
        }

        btnCancelarBorrarTto.setOnClickListener{
            findNavController().navigate(R.id.ttoDetailFragment)
        }

        return root
    }

    private fun alarmCancel(idAlarma : Int) {
        val intent = Intent(context, MainActivity.AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, idAlarma, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        alarmManager.cancel(pendingIntent)
    }

}

