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

class CharacterViewEpisodeAdapter : RecyclerView.Adapter<CharacterViewEpisodeAdapter.ViewHolder>() {
    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val episodeId = view.findViewById<TextView>(R.id.episodeId)!!
        val episodeName = view.findViewById<TextView>(R.id.EpisodeName)!!
        val airDate = view.findViewById<TextView>(R.id.air_date)!!
        val episode = view.findViewById<TextView>(R.id.episode)!!
    }

    private val differCallback = object : DiffUtil.ItemCallback<Episode>(){
        override fun areItemsTheSame(oldItem: Episode, newItem: Episode): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Episode, newItem: Episode): Boolean {
            return oldItem == newItem
        }

    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.character_view_episode_list, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
       val episode = differ.currentList[position]
        holder.episodeId.text = episode.id.toString()
        holder.episodeName.text = episode.name
        holder.airDate.text = episode.air_date
        holder.episode.text = episode.episode
        holder.itemView.setOnClickListener {
            onItemClickListener?.let {
                it(episode)
            }
        }
    }

    private var onItemClickListener: ((Episode) -> Unit)? = null

    fun setOnItemClickListener(listener: ((Episode) -> Unit)){
        onItemClickListener = listener
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }
}