package com.vadim.playlistmaker

import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

class TrackAdapter(val tracks: List<Track>) : RecyclerView.Adapter<TrackViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        return TrackViewHolder(parent)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        holder.bind(tracks[position])
    }

    override fun getItemCount(): Int {
        return tracks.size
    }
}

class TrackViewHolder(parent: ViewGroup)
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
        trackTimeTv.text = track.trackTime

        Glide.with(itemView)
            .load(track.artworkUrl100)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    Log.e("GlideDebug", "Image load failed", e)
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: com.bumptech.glide.load.DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    return false
                }
            })
            .placeholder(R.drawable.placeholder)
            .error(R.drawable.cat_ico)
            .into(artWorkIv)
    }
}
