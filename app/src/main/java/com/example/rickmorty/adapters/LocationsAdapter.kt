package com.example.rickmorty.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.rickmorty.R
import com.example.rickmorty.models.ApiCharacter
import com.example.rickmorty.models.Episode
import com.example.rickmorty.models.SingleLocation

class LocationsAdapter : RecyclerView.Adapter<LocationsAdapter.ViewHolder>() {
    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val locationId = view.findViewById<TextView>(R.id.locationId)!!
        val locationName = view.findViewById<TextView>(R.id.locationName)!!
        val type = view.findViewById<TextView>(R.id.locationType)!!
        val dimension = view.findViewById<TextView>(R.id.locationDimension)!!
    }

    private val differCallback = object : DiffUtil.ItemCallback<SingleLocation>(){
        override fun areItemsTheSame(oldItem: SingleLocation, newItem: SingleLocation): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: SingleLocation, newItem: SingleLocation): Boolean {
            return oldItem == newItem
        }

    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.location_list_view, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
       val location = differ.currentList[position]
        holder.locationId.text = location.id.toString()
        holder.locationName.text = location.name
        holder.type.text = location.type
        holder.dimension.text = location.dimension
        holder.itemView.setOnClickListener {
            onItemClickListener?.let {
                it(location)
            }
        }
    }

    private var onItemClickListener: ((SingleLocation) -> Unit)? = null

    fun setOnItemClickListener(listener: ((SingleLocation) -> Unit)){
        onItemClickListener = listener
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }
}