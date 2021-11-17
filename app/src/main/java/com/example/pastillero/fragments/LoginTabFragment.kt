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
import com.example.pastillero.activities.LoginActivity
import com.example.pastillero.activities.MainActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.login_tab_fragment.*

class LoginTabFragment : Fragment() {

    lateinit var btnLogin : Button
    lateinit var v: View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        v = inflater.inflate(R.layout.login_tab_fragment, container, false)
        btnLogin = v.findViewById(R.id.login)

        btnLogin.setOnClickListener{
            when {
                TextUtils.isEmpty(email.text.toString().trim { it <= ' ' }) -> {
                    Toast.makeText(requireActivity(), "Porfavor ingrese un email", Toast.LENGTH_SHORT
                    ).show()
                }
                TextUtils.isEmpty(password.text.toString().trim { it <= ' ' }) -> {
                    Toast.makeText(requireActivity(), "Porfavor ingrese una contraseña", Toast.LENGTH_SHORT
                    ).show()
                }
            else -> {
                val email: String = email.text.toString().trim { it <= ' ' }
                val password: String = password.text.toString().trim { it <= ' ' }

                FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->

                        if (task.isSuccessful){

                            Toast.makeText(requireActivity(), "Logueado exitosamente", Toast.LENGTH_SHORT
                            ).show()

                            val intent = Intent(getActivity(), MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            intent.putExtra("user_id", FirebaseAuth.getInstance().currentUser!!.uid)
                            intent.putExtra("email_id", email)
                            startActivity(intent)
                            //finish()
                        } else {
                            Toast.makeText(requireActivity(), task.exception!!.message.toString(), Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

            }
        }

        return v
    }

}