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
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pastillero.R
import com.example.pastillero.activities.MainActivity
import com.example.pastillero.adapters.Tratamiento
import com.example.pastillero.adapters.TratamientosAdapter
import com.example.pastillero.databinding.FragmentHomeBinding


import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var recTratamiento : RecyclerView
    private var listaTratamiento : MutableList<Tratamiento> = mutableListOf()
    private val db = FirebaseFirestore.getInstance()
    lateinit var alarmManager: AlarmManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager

        recTratamiento = root.findViewById(R.id.recTratamiento)
        return root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        buttonToForm.setOnClickListener {
            findNavController().navigate(R.id.formFragment)
        }
    }

    override fun onStart() {
        super.onStart()
        var email = activity?.intent?.getStringExtra("email_id")

        listaTratamiento.clear()
        db.collection("treatments")
            .get()
            .addOnSuccessListener { tratamientos ->
                activity?.intent?.putExtra("cant_Tratamientos", tratamientos.size().toString())
                for (tratamiento in tratamientos){
                    if (tratamiento.get("user_id") == email) {
                        var id = tratamiento.get("id_med")

                        db.collection("meds").document(id.toString()).get().addOnSuccessListener { doc ->
                            var medName = doc.get("name").toString()
                            var stock = tratamiento.get("stock").toString()
                            var frecuencia = tratamiento.get("frequency").toString()
                            var diasRecompra = tratamiento.get("days_buy_back").toString()
                            var diasReceta = tratamiento.get("days_recepy").toString()
                            var fechaInicio = tratamiento.get("start_date").toString()
                            var idTto = tratamiento.id
                            var idMed = doc.id
                            var amount = doc.get("amount").toString()
                            var urlImg = doc.get("imgUrl").toString()
                            listaTratamiento.add(Tratamiento(medName, stock, frecuencia, diasRecompra, diasReceta, fechaInicio, idTto, idMed, amount, urlImg))
                            recTratamiento.setHasFixedSize(true)
                            recTratamiento.layoutManager = LinearLayoutManager(context)
                            recTratamiento.adapter = TratamientosAdapter(listaTratamiento){ index ->
                                onItemClick(index)
                            }
                        }
                    }
                }
            }
            .addOnFailureListener { exception -> }
    }

    fun onItemClick(pos: Int) {
        activity?.intent?.putExtra("medName", listaTratamiento[pos].medName)
        activity?.intent?.putExtra("stock", listaTratamiento[pos].stock)
        activity?.intent?.putExtra("frecuencia", listaTratamiento[pos].frecuencia)
        activity?.intent?.putExtra("diasRecompra", listaTratamiento[pos].diasRecompra)
        activity?.intent?.putExtra("diasReceta", listaTratamiento[pos].diasReceta)
        activity?.intent?.putExtra("fechaInicio", listaTratamiento[pos].fechaInicio)
        activity?.intent?.putExtra("idTto", listaTratamiento[pos].idTto)
        activity?.intent?.putExtra("idMed", listaTratamiento[pos].idMed)
        activity?.intent?.putExtra("amount", listaTratamiento[pos].amount)
        activity?.intent?.putExtra("urlImg", listaTratamiento[pos].urlImg)
        findNavController().navigate(R.id.ttoDetailFragment)
    }

    override fun onResume() {
        super.onResume()
        checkStockCero()
    }

    override fun onStop() {
        super.onStop()
        checkStockCero()
    }

    private fun alarmCancel(idAlarma : Int) {
        val intent = Intent(context, MainActivity.AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, idAlarma, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        alarmManager.cancel(pendingIntent)
    }

    private fun checkStockCero(){

        db.collection("treatments")
            .get()
            .addOnSuccessListener { tratamientos ->
                for (tratamiento in tratamientos){
                    if(tratamiento.get("stock").toString().toInt() == 0)
                    {
                        var ids = tratamiento.get("idAlarmas")
                        ids = ids.toString().replace("[","")
                        ids = ids.toString().replace("]","")
                        var strings = ids.toString().split(", ").toTypedArray()
                        alarmCancel(strings[0].toInt())
                        alarmCancel(strings[1].toInt())
                        alarmCancel(strings[2].toInt())
                        alarmCancel(strings[3].toInt())
                        //findNavController().navigate(R.id.ttoDetailFragment)
                    }
                }
            }
    }

}