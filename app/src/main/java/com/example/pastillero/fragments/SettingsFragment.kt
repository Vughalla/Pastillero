package com.example.pastillero.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.pastillero.R
import com.example.pastillero.databinding.FragmentSettingsBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_settings.*

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val btnGuardar = root.findViewById<View>(R.id.btn_guardar)
        val user = Firebase.auth.currentUser
        val db = FirebaseFirestore.getInstance()

        if (user != null) {
            db.collection("users").document(user.email.toString())
                .get()
                .addOnSuccessListener {
                    val mailTutor = it.get("tutor_email")
                    if (mailTutor != "") {
                        p_mailTutor.setText("Mail Tutor: " + mailTutor as String?)
                    } else {
                        p_mailTutor.setText("No se han encontrado tutores." as String?)
                    }
                }
                .addOnFailureListener { exception ->

                }
        }

        btnGuardar.setOnClickListener{
            val mailTutor: String = et_mailTutor.text.toString().trim { it <= ' ' }
            var isValid = isValidEmail(mailTutor)
            if (isValid){
                if (user != null) {
                    db.collection("users").document(user.email.toString())
                        .set(
                            hashMapOf(
                                "tutor_email" to mailTutor
                            ), SetOptions.merge()
                        ).addOnSuccessListener {
                            p_mailTutor.setText("Mail Tutor: " + mailTutor)
                            et_mailTutor.setText("")
                        }
                }
            } else {
                Toast.makeText(requireActivity(), "El mail ingresado es incorrecto.", Toast.LENGTH_SHORT).show()
            }
        }
        return root
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