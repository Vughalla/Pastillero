package com.example.pastillero.fragments

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.pastillero.R
import com.example.pastillero.activities.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.form_fragment.*
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.signup_tab_fragment.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.floor
import kotlin.math.roundToInt

class FormFragment : Fragment(R.layout.form_fragment), DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener{

    lateinit var v: View
    lateinit var btnConfirmar : Button
    lateinit var btnProxima : Button
    lateinit var spinnerFrecuencia : Spinner

    val medicamentos: ArrayList<String> = ArrayList()
    val idMedicamentos: ArrayList<String> = ArrayList()
    val amountMedicamentos: ArrayList<String> = ArrayList()

    var day = 0
    var month = 0
    var year = 0
    var hour = 0
    var minute = 0

    var savedDay = 0
    var savedMonth = 0
    var savedYear = 0
    var savedHour = 0
    var savedMinute = 0

    var idMed: String = ""
    var nameMed: String = ""
    var frecuMed: String = ""
    var amountMed: String = ""

    private var idAlarmas : MutableList<Int> = mutableListOf()

    private val db = FirebaseFirestore.getInstance()
    lateinit var alarmManager: AlarmManager


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.form_fragment, container, false)
        btnConfirmar = v.findViewById(R.id.btn_confirmar)
        btnProxima = v.findViewById(R.id.btnProximaToma)
        spinnerFrecuencia = v.findViewById(R.id.spinnerFrecuencia)
        alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        loadDataBase()
        loadSpinner()
        pickDate()
        val context = requireContext()
        btnConfirmar.setOnClickListener{


            when{
                spinnerMedicamentos.selectedItem.toString() == ("--Seleccione un medicamento--") -> {
                    Toast.makeText(requireActivity(), "Por favor seleccione un medicamento", Toast.LENGTH_SHORT).show()
                }
                TextUtils.isEmpty(editNuevasRecetas.text.toString().trim { it <= ' ' }) -> {
                    Toast.makeText(requireActivity(), "Por favor ingrese cuando quiere ser notificado (nueva receta)", Toast.LENGTH_SHORT).show()
                }
                TextUtils.isEmpty(editRecompra.text.toString().trim { it <= ' ' }) -> {
                    Toast.makeText(requireActivity(), "Por favor ingrese cuando quiere ser notificado (recompra)", Toast.LENGTH_SHORT).show()
                }
                editNuevasRecetas.text.toString().toFloat() > maxDiasRecordatorio() -> {
                    Toast.makeText(requireActivity(), "Los dias de aviso de nueva receta no pueden superar los ${maxDiasRecordatorio()}", Toast.LENGTH_SHORT).show()
                }
                editRecompra.text.toString().toFloat() > maxDiasRecordatorio() -> {
                    Toast.makeText(requireActivity(), "Los dias de aviso de recompra no pueden superar los ${maxDiasRecordatorio()}", Toast.LENGTH_SHORT).show()
                }
                else -> {

                    val days_buy_back: String = editNuevasRecetas.text.toString().trim { it <= ' ' }
                    val days_recepy: String = editRecompra.text.toString().trim { it <= ' ' }

                    val user = FirebaseAuth.getInstance().currentUser

                    var year = Calendar.getInstance().get(Calendar.YEAR).toString()
                    var month = Calendar.getInstance().get(Calendar.MONTH).toString()
                    var day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH).toString()

                    var cant = (activity?.intent?.getStringExtra("cant_Tratamientos")?.toInt() ?: 0) * 4
                    idAlarmas.add(cant + 1)
                    idAlarmas.add(cant + 2)
                    idAlarmas.add(cant + 3)
                    idAlarmas.add(cant + 4)


                    if (user != null) {
                        db.collection("treatments").add(
                            hashMapOf(
                                "active" to true,
                                "created_at" to day + "/" + month + "/" + year,
                                "days_buy_back" to days_buy_back,
                                "days_recepy" to days_recepy,
                                "frequency" to frecuMed,
                                "id_med" to idMed,
                                "start_date" to "$savedDay/$savedMonth/$savedYear",
                                "start_time" to "$savedHour:$savedMinute",
                                "stock" to amountMed,
                                "user_id" to user.email,
                                "idAlarmas" to idAlarmas
                            )
                        ).addOnSuccessListener { documentReference ->
                            Toast.makeText(requireActivity(), "Tratamiento Agregado", Toast.LENGTH_SHORT).show()
                            alarmSet(idAlarmas.get(0), "ALARMA")
                            var lapsoFin = ((amountMed.toFloat()*frecuMed.toFloat())/24).toFloat()
                            var lapsoReceta = lapsoFin - days_recepy.toInt()
                            var lapsoRecompra = lapsoFin - days_buy_back.toInt()
                            alertSet(idAlarmas.get(1),lapsoReceta.toInt() * 24 * 60 * 60 * 1000, "Receta")
                            alertSet(idAlarmas.get(2),lapsoRecompra.toInt() * 24 * 60 * 60 * 1000, "Recompra")

                            alertSet(idAlarmas.get(3), lapsoFin.toInt()*24* 60 * 60 * 1000,"Fin de tratamiento" )

                            findNavController().navigate(R.id.navigation_home)

                        }
                            .addOnFailureListener { e ->
                                Toast.makeText(requireActivity(), "Error adding document, try again", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
            }
        }
        return v
    }


    private fun alarmSet(id : Int, mensaje : String) {

        db.collection("meds").document(idMed).get().addOnSuccessListener { doc ->
            var medName = doc.get("name").toString()
            val seconds = frecuMed.toInt() * 60 * 60 * 1000 //edt_timer.text.toString().toInt() * 1000
            val intent = Intent(context, MainActivity.AlarmReceiver::class.java)
            intent.putExtra("idMed",idMed)
            intent.putExtra("idAl",id)
            intent.putExtra("nameMed",nameMed)
            intent.putExtra("mensaje",mensaje)

            val pendingIntent = PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            //Toast.makeText(context, "La alarma del tratamiento " + medName + " fue creada a las " + Date().toString(), Toast.LENGTH_SHORT).show()
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() /* + seconds*/ ,60000/*seconds*/, pendingIntent)

        }
    }

    private fun alertSet(id : Int, seconds : Int, mensaje: String) {
        db.collection("meds").document(idMed).get().addOnSuccessListener { doc ->
            var medName = doc.get("name").toString()
            val intent = Intent(context, MainActivity.AlarmReceiver::class.java)
            intent.putExtra("trat",idMed)
            intent.putExtra("idAl",id)
            intent.putExtra("nameMed",nameMed)
            intent.putExtra("mensaje",mensaje)

            val pendingIntent = PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            //Toast.makeText(context, "seteo: "+mensaje+ " " + medName, Toast.LENGTH_LONG).show()
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + /*seconds*/ 10000, pendingIntent)

        }
    }




    private fun maxDiasRecordatorio(): Float {
        var num:Float = (amountMed.toFloat()*frecuMed.toFloat())/24
        return num
    }


    private fun loadSpinner() {
        val lista: List<String> = listOf("Seleccione la frecuencia","1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17","18","19","20","21","22","23","24")
        val adapter: ArrayAdapter<String> = object:ArrayAdapter<String>(v.context, android.R.layout.simple_spinner_dropdown_item, lista)
        {
            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view:TextView = super.getDropDownView(position, convertView, parent) as TextView

                view.setTypeface(Typeface.DEFAULT, Typeface.ITALIC)

                if(position == 0){
                    view.setTextColor(Color.LTGRAY)
                }

                if (position %2 == 1){
                    view.background = ColorDrawable(Color.parseColor("#F2F4F6"))
                }else{
                    view.background = ColorDrawable(Color.parseColor("#FFFFFF"))
                }
                return view
            }
            override fun isEnabled(position: Int): Boolean {
                return position != 0
            }
        }
        spinnerFrecuencia.dropDownVerticalOffset
        spinnerFrecuencia.adapter = adapter
        spinnerFrecuencia.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                if (pos != 0) {
                    var selectedItem = parent!!.getItemAtPosition(pos)

                    frecuMed = selectedItem.toString()
                    spinnerFrecuencia.setSelection(pos)
                }
                //Toast.makeText(requireActivity(), "$selectedItem Seleccionado", Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

        }
    }


    private fun loadDataBase() {

        medicamentos.clear()
        idMedicamentos.clear()
        amountMedicamentos.clear()
        db.collection("meds")
            .get()
            .addOnSuccessListener { documents ->
                medicamentos.add(0,"Seleccione un medicamento")
                for (document in documents){
                    medicamentos.add(document.get("name").toString())
                    idMedicamentos.add(document.id.toString())
                    amountMedicamentos.add(document.get("amount").toString())
                }
                //val adapter = ArrayAdapter<String>(v.context, android.R.layout.simple_dropdown_item_1line, medicamentos)
                val adapter:ArrayAdapter<String> = object: ArrayAdapter<String>(
                    v.context, android.R.layout.simple_spinner_dropdown_item,medicamentos)
                {
                    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                        val view:TextView = super.getDropDownView(position, convertView, parent) as TextView

                        view.setTypeface(Typeface.DEFAULT, Typeface.ITALIC)

                        if (position %2 == 1){
                            view.background = ColorDrawable(Color.parseColor("#F2F4F6"))
                        }else{
                            view.background = ColorDrawable(Color.parseColor("#FFFFFF"))
                        }
                        return view
                    }
                    override fun isEnabled(position: Int): Boolean {
                        return position != 0
                    }
                }
                spinnerMedicamentos.adapter = adapter
                //spinnerMedicamentos.prompt = "--Seleccione un medicamento--"
                spinnerMedicamentos.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {

                        val selectedItem = parent!!.getItemAtPosition(pos)

                        if (pos != 0) {
                            nameMed = medicamentos.get(pos)
                            idMed = idMedicamentos.get(pos-1)
                            amountMed = amountMedicamentos.get(pos-1)
                            spinnerMedicamentos.setSelection(pos)
                        }

                        Toast.makeText(requireActivity(), "$selectedItem Seleccionado", Toast.LENGTH_SHORT).show()
                    }

                    override fun onNothingSelected(p0: AdapterView<*>?) {

                    }

                }

            }
            .addOnFailureListener { exception ->

            }


    }

    private fun pickDate() {
        btnProxima.setOnClickListener{
            getDateTimeCalendar()

            DatePickerDialog(v.context, this, year, month, day).show()
        }
    }

    private fun getDateTimeCalendar(){
        val cal = Calendar.getInstance()
        day = cal.get(Calendar.DAY_OF_MONTH)
        month = cal.get(Calendar.MONTH)
        year = cal.get(Calendar.YEAR)
        hour = cal.get(Calendar.HOUR)
        minute = cal.get(Calendar.MINUTE)

    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        savedDay = dayOfMonth
        savedMonth = month
        savedYear = year

        getDateTimeCalendar()

        TimePickerDialog(v.context,this,hour, minute, true).show()
    }

    override fun onTimeSet(p0: TimePicker?, hour: Int, minute: Int) {
        savedHour = hour
        savedMinute = minute

        tvDate.setText("Fecha de inicio: $savedDay-$savedMonth-$savedYear - $savedHour:$savedMinute")
        //Toast.makeText(requireActivity(), "$savedDay-$savedMonth-$savedYear - $savedHour:$savedMinute", Toast.LENGTH_SHORT).show()
    }

}