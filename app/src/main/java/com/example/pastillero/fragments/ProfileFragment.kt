package com.example.pastillero.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.pastillero.R
import com.example.pastillero.activities.LoginActivity
import com.example.pastillero.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_profile.*

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val btnLogout = root.findViewById<View>(R.id.btn_logout)

        val user = Firebase.auth.currentUser
        val db = FirebaseFirestore.getInstance()
        if (user != null) {
            db.collection("users").document(user.email.toString())
                .get()
                .addOnSuccessListener {
                    p_mail.setText(it.get("email") as String?)
                    p_name.setText(it.get("name") as String?)
                    p_lastName.setText(it.get("last_name") as String?)
                    p_birthday.setText(it.get("birth_date") as String?)

                }
                .addOnFailureListener { exception ->

                }
        }
        btnLogout.setOnClickListener{
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(getActivity(), LoginActivity::class.java))
            
        }


        return root
    }
}