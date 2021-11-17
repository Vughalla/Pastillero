package com.example.pastillero.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.pastillero.R
import com.example.pastillero.databinding.TtoDetailFragmentBinding
import kotlinx.android.synthetic.main.tto_detail_fragment.*



class TtoDetailFragment : Fragment() {

    private var _binding: TtoDetailFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var urlImg: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = TtoDetailFragmentBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val btnBorrarTto = root.findViewById<View>(R.id.btnBorrarTto)
        val btnEditarTto = root.findViewById<View>(R.id.btnEditarTto)
        val btnRecomprar = root.findViewById<View>(R.id.btnRecomprar)
        val img = root.findViewById<View>(R.id.imageTto)

        urlImg = activity?.intent?.getStringExtra("urlImg").toString()
        Glide.with(root).load(urlImg).into(img as ImageView)


        btnBorrarTto.setOnClickListener{
            findNavController().navigate(R.id.confirmarBorrarTto)
        }

        btnEditarTto.setOnClickListener{
            findNavController().navigate(R.id.editarTto)
        }

        btnRecomprar.setOnClickListener {
            findNavController().navigate(R.id.recompraFragment)
        }
        
        return root
    }

    override fun onStart() {
        super.onStart()

        val medName = activity?.intent?.getStringExtra("medName")
        val stock = activity?.intent?.getStringExtra("stock")
        val frecuencia = activity?.intent?.getStringExtra("frecuencia")
        val diasRecompra = activity?.intent?.getStringExtra("diasRecompra")
        val diasReceta = activity?.intent?.getStringExtra("diasReceta")
        val fechaInicio = activity?.intent?.getStringExtra("fechaInicio")

        tto_detail_medName.setText(medName)
        tto_detail_stock.setText(stock)
        tto_detail_frecuencia.setText(frecuencia)
        tto_detail_diasRecompra.setText(diasRecompra)
        tto_detail_diasReceta.setText(diasReceta)
        tto_detail_fechaInicio.setText(fechaInicio)
    }

}