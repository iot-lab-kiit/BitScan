package `in`.iot.lab.bitscan.ui.recyclerView

import `in`.iot.lab.bitscan.R
import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.net.Uri
import android.view.View
import com.bumptech.glide.Glide
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.AbstractItem
import kotlinx.android.synthetic.main.rv_layout.view.*
import java.io.File

class ListObject(
    val imagePath: String,
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
        val uri: Uri = Uri.fromFile(File(item.imagePath))
        Glide.with(itemView.context).load(uri).into(itemView.image_bmp_view)
        itemView.image_title.text=item.title
    }

    override fun unbindView(item: ListObject) {

    }


}