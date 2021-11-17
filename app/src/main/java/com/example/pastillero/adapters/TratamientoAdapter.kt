package com.example.pastillero.adapters;

import android.view.LayoutInflater
import android.view.View;
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide
import com.example.pastillero.R

class TratamientosAdapter(
    var listaTratamiento : MutableList<Tratamiento>,
    var onClick: (Int) -> Unit
    ) : RecyclerView.Adapter<TratamientosAdapter.TratamientoHolder>() {

    class TratamientoHolder (v:View) : RecyclerView.ViewHolder(v) {
        private var view: View

        init {
            this.view = v
        }

        fun setImagenTratamiento(imgUrl : String) {
            var imagen : ImageView = view.findViewById(R.id.imgRecyclerView)
            Glide.with(view).load(imgUrl).into(imagen)
        }

        fun setNombreTratamiento(title : String) {
            var txtNombre : TextView = view.findViewById(R.id.txtTratamientoNombre)
            txtNombre.text = title
        }

        fun getCardView() : CardView {
            return view.findViewById(R.id.CardViewTto)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TratamientoHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_tratamiento, parent, false)
        return (TratamientoHolder(view))
    }

    override fun onBindViewHolder(holder: TratamientoHolder, position: Int) {
        holder.setNombreTratamiento(listaTratamiento[position].medName)
        holder.setImagenTratamiento(listaTratamiento[position].urlImg)
        holder.getCardView().setOnClickListener{
            onClick(position)
        }
    }

    override fun getItemCount(): Int {
        return listaTratamiento.size
    }


}
