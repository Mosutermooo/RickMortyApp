package com.example.rickmorty.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.rickmorty.R
import com.example.rickmorty.models.ApiCharacter
import de.hdodenhof.circleimageview.CircleImageView

class CharacterAdapter : RecyclerView.Adapter<CharacterAdapter.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image = view.findViewById<CircleImageView>(R.id.charactersImage)!!
        val name = view.findViewById<TextView>(R.id.name)!!
        val episodes = view.findViewById<TextView>(R.id.EpisodesCount)!!
    }

    private val differCallback = object : DiffUtil.ItemCallback<ApiCharacter>(){
        override fun areItemsTheSame(oldItem: ApiCharacter, newItem: ApiCharacter): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ApiCharacter, newItem: ApiCharacter): Boolean {
            return oldItem == newItem
        }

    }

    val differ = AsyncListDiffer(this, differCallback)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater
            .from(parent.context)
            .inflate(R.layout.characters_list_view, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val character = differ.currentList[position]
        Glide.with(holder.itemView).load(character.image).centerCrop()
            .placeholder(R.drawable.ic_outline_account_circle_24)
            .into(holder.image)
        holder.name.text = character.name
        holder.episodes.text = "${character.episode.size} Episode(s)"
        holder.itemView.setOnClickListener {
            onItemClickListener?.let {
                it(character)
            }
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }
    private var onItemClickListener: ((ApiCharacter) -> Unit)? = null

    fun setOnItemClickListener(listener: ((ApiCharacter) -> Unit)){
        onItemClickListener = listener
    }
}