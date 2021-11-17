package com.example.pastillero.fragments

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.pastillero.R
import com.example.pastillero.activities.MainActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.signup_tab_fragment.*
import java.util.*

class SignupTabFragment : Fragment() {

    lateinit var btnRegister : Button
    lateinit var v: View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        v = inflater.inflate(R.layout.signup_tab_fragment, container, false)
        btnRegister = v.findViewById(R.id.registerButton)
        val db = FirebaseFirestore.getInstance()

        val calendar: Calendar = Calendar.getInstance()


        btnRegister.setOnClickListener {
            datePicker.maxDate = calendar.timeInMillis
            val mail: String = emailRegister.text.toString().trim { it <= ' ' }
            var isValid = isValidEmail(mail)
            if (!isValid){
                Toast.makeText(requireActivity(), "El formato de mail es invalido ", Toast.LENGTH_SHORT
                ).show()
            }
            when {

                TextUtils.isEmpty(name.text.toString().trim { it <= ' ' }) -> {
                    Toast.makeText(requireActivity(), "Por favor ingrese un nombre", Toast.LENGTH_SHORT
                    ).show()
                }
                TextUtils.isEmpty(lastName.text.toString().trim { it <= ' ' }) -> {
                    Toast.makeText(requireActivity(), "Por favor ingrese un apellido", Toast.LENGTH_SHORT
                    ).show()
                }
                TextUtils.isEmpty(emailRegister.text.toString().trim { it <= ' ' }) -> {
                    Toast.makeText(requireActivity(), "Por favor ingrese un email", Toast.LENGTH_SHORT
                    ).show()
                }
                TextUtils.isEmpty(passwordRegister.text.toString().trim { it <= ' ' }) -> {
                    Toast.makeText(requireActivity(), "Por favor ingrese una contraseña", Toast.LENGTH_SHORT
                    ).show()
                }
                else -> {

                    val email: String = emailRegister.text.toString().trim { it <= ' ' }
                    val password: String = passwordRegister.text.toString().trim { it <= ' ' }
                    val name: String = name.text.toString().trim { it <= ' ' }
                    val lastName: String = lastName.text.toString().trim { it <= ' ' }
                    val age: String = ""+datePicker.dayOfMonth +"/"+datePicker.month+"/"+datePicker.year
                    val mailTutor: String = tutor.text.toString().trim { it <= ' ' }

                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(OnCompleteListener<AuthResult>() { task ->

                            if (task.isSuccessful){

                                var year = Calendar.getInstance().get(Calendar.YEAR).toString()
                                var month = Calendar.getInstance().get(Calendar.MONTH).toString()
                                var day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH).toString()

                                 db.collection("users").document(email).set(
                                    hashMapOf(
                                        "birth_date" to age,
                                        "created_at" to day + "/" + month + "/" + year,
                                        "email" to email,
                                        "last_name" to lastName,
                                        "name" to name,
                                        "tutor_email" to mailTutor
                                    )
                                ).addOnSuccessListener { documentReference ->
                                    Toast.makeText(requireActivity(), "Document added", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(requireActivity(), "Error adding document, try again", Toast.LENGTH_SHORT).show()
                                }

                                Toast.makeText(requireActivity(), "Usuario creado exitosamente", Toast.LENGTH_SHORT
                                ).show()

                                val intent = Intent(getActivity(), MainActivity::class.java)
                                //intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                intent.putExtra("user_id", FirebaseAuth.getInstance().currentUser!!.uid)
                                intent.putExtra("email_id", email)
                                startActivity(intent)

                                //finish()
                            } else {
                                Toast.makeText(requireActivity(), task.exception!!.message.toString(), Toast.LENGTH_SHORT
                                ).show()
                            }
                        })
                }
            }
        }

        return v
    }

    private fun isValidEmail(email: String): Boolean {
        var isValid = false
        if (email.contains("@")) {
            val email = email.split("@")
            if (email.size == 2 && email[1].contains(".")) {
                val email = email[1].split(".")
                if (email.size >= 2) {
                    if (email[0] != "" && email[1] != ""){
                        isValid = true
                    }
                }
            }
        }
        return isValid
    }
}