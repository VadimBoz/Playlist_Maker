package com.vadim.playlistmaker.presentation

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vadim.playlistmaker.R
import com.vadim.playlistmaker.domain.model.Track
import com.vadim.playlistmaker.domain.repository.ImageLoader

class TracksAdapter(private var tracks: List<Track>,
                    private val imageLoader: ImageLoader,
                    private val onTrackItemClick: (Track) -> Unit
        ) : RecyclerView.Adapter<TrackViewHolder>() {

    fun updateTracks(newTracks: List<Track>) {
        tracks = newTracks
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        return TrackViewHolder(parent, imageLoader)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        val track = tracks[position]
        holder.bind(track)
        holder.itemView.setOnClickListener {
            onTrackItemClick(track)
        }
    }

    override fun getItemCount(): Int {
        return tracks.size
    }
}

class TrackViewHolder(parent: ViewGroup, private val imageLoader: ImageLoader)
    : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.item_track, parent, false)) {

    private val artWorkIv = itemView.findViewById<ImageView>(R.id.artWork_IV)
    private val nameTrackTv = itemView.findViewById<TextView>(R.id.name_Track_TV)
    private val artistNameTv = itemView.findViewById<TextView>(R.id.artistName_TV)
    private val trackTimeTv = itemView.findViewById<TextView>(R.id.trackTime_TV)

    fun bind(track: Track) {
        nameTrackTv.text = track.trackName
        artistNameTv.text = track.artistName
        trackTimeTv.text = track.trackDuration


        artWorkIv.setImageResource(R.drawable.placeholder)

        imageLoader.loadTrackImage(track) { imageData ->
            imageData.imageBytes?.let { bytes ->
                val bitmap = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                artWorkIv.setImageBitmap(bitmap)
            }
        }
    }
}
