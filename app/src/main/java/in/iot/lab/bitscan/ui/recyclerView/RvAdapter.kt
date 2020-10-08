package com.aaryaman.designs.recyclerView

import `in`.iot.lab.bitscan.R
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.rv_layout.view.*


//lateinit var imageList: MutableList<ListObject> //Change it accordingly


class BlogRecyclerViewAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return  ImagesListHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.rv_layout,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder){
            is ImagesListHolder -> {
                holder.bind(imageList!![position])
            }
        }
    }

    override fun getItemCount(): Int {
        return imageList!!.size
    }
}

class ImagesListHolder constructor(itemView: View): RecyclerView.ViewHolder(itemView) {

    val RVImageDisplayer = itemView.image_bmp_view
    var RVTitleDisplayer = itemView.image_title


    fun bind(baseModel: ListObject) {
        RVTitleDisplayer.text = baseModel.title
//        val resized = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(resources, R.drawable.doc_sample), 400, 400, true)
//        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.doc_sample)
//        RVImageDisplayer.setImageBitmap(resized)
//        RVImageDisplayer.setImageResource(R.drawable.doc_sample)
        Glide.with(itemView).load(R.drawable.doc_sample).into(RVImageDisplayer)

    }
///////////Use this if you need click listener
//    init {
//        itemView.setOnClickListener {
//            val intent= Intent(itemView.context, WHatever::class.java)
//            intent.putExtra("body",  pass_variable)
//            itemView.context.startActivity(intent)
//        }
//
//    }



}

