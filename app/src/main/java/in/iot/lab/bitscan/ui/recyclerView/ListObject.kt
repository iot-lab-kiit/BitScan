package `in`.iot.lab.bitscan.ui.recyclerView

import `in`.iot.lab.bitscan.R
import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.net.Uri
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.AbstractItem
import kotlinx.android.synthetic.main.rv_layout.view.*
import java.io.File

class ListObject(
    val bitmap: ByteArray?,
    val title: String
) : AbstractItem<ListObjectViewHolder>() {
    override val layoutRes: Int
        get() = R.layout.rv_layout
    override val type: Int
        @SuppressLint("ResourceType")
        get() = 69

    override fun getViewHolder(v: View): ListObjectViewHolder {
        return ListObjectViewHolder(v)
    }


}

class ListObjectViewHolder(itemView: View) : FastAdapter.ViewHolder<ListObject>(itemView) {
    override fun bindView(item: ListObject, payloads: List<Any>) {
        val imgBmp = item.bitmap
        if(imgBmp != null) {
            Glide.with(itemView.context)
                .load(imgBmp)
                .apply(RequestOptions.skipMemoryCacheOf(true))
                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                .into(itemView.image_bmp_view)
        }

        itemView.image_title.text=item.title
    }

    override fun unbindView(item: ListObject) {

    }
}