package `in`.iot.lab.bitscan.ui.recyclerView

import `in`.iot.lab.bitscan.R
import `in`.iot.lab.bitscan.entities.Note
import `in`.iot.lab.bitscan.util.Convertors
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.dashboard_card.view.*
import kotlinx.android.synthetic.main.rv_layout.view.*
import java.io.File

class DashboardAdapter(private val noteList: List<Note>,
                       private val listener : OnNoteClickListener, val context: Context
): RecyclerView.Adapter<DashboardAdapter.DashboardViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DashboardViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.dashboard_card,parent,false)

        return DashboardViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: DashboardViewHolder, position: Int) {
        val currentItem = noteList[position]

        val bitmap = Convertors.toBitmap(currentItem.thumbnail)
        Glide.with(context)
            .load(bitmap)
            .apply(RequestOptions.skipMemoryCacheOf(true))
            .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
            .into(holder.thumbnailImageView)
        holder.titleTextView.text = currentItem.title
        holder.dateTextView.text = currentItem.dateModified
    }

    override fun getItemCount() = noteList.size

    inner class DashboardViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val  thumbnailImageView: ImageView = itemView.dashboard_card_image_view
        val  titleTextView: TextView = itemView.dashboard_card_title
        val  dateTextView: TextView = itemView.dashboard_card_date

        init {
            itemView.dashboard_card_delete.setOnClickListener{
                val position = adapterPosition
                if(position != RecyclerView.NO_POSITION) {
                    listener.onNoteDelete(position)
                }
            }

            itemView.dashboard_card_share.setOnClickListener {
                val position = adapterPosition
                if(position != RecyclerView.NO_POSITION) {
                    listener.onNoteShare(position)
                }
            }

            itemView.setOnClickListener {
                val position = adapterPosition
                if(position != RecyclerView.NO_POSITION) {
                    listener.onNoteClick(position)
                }
            }
        }
    }

    interface  OnNoteClickListener{
        fun onNoteClick(position : Int)
        fun onNoteDelete(position: Int)
        fun onNoteShare(position:Int)
    }
}