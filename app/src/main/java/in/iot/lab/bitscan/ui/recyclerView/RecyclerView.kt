package com.aaryaman.designs.recyclerView

import `in`.iot.lab.bitscan.R
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_recycler_view.*
import java.io.File
import java.util.*

var imageList: MutableList<ListObject> = mutableListOf() //Change it accordingly

class RecyclerView : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recycler_view)

        lateinit var blogAdapter: BlogRecyclerViewAdapter

        recycler_view.apply {
            layoutManager = GridLayoutManager(this@RecyclerView, 2)
            blogAdapter = BlogRecyclerViewAdapter()
            addItemDecoration(ListDecor(30))
            adapter = blogAdapter
        }

    var l1= ListObject()
    l1.image = File("R.drawable.ic_apps_red_24dp")
    l1.title = "FIRst"
    var l2= ListObject()
    l2.image = File("R.drawable.ic_apps_red_24dp")
    l2.title = "SecoNd"
    var l3= ListObject()
    l3.image = File("R.drawable.ic_apps_red_24dp")
    l3.title = "ThiRD"
    var l4= ListObject()
    l4.image = File("R.drawable.ic_apps_red_24dp")
    l4.title = "Fourthh"
    var l5= ListObject()
    l5.image = File("R.drawable.ic_apps_red_24dp")
    l5.title = "fiFth"

//    imageList= mutableListOf(l1!!, l2!!, l3!!, l4!!, l5!!)
        imageList.add(l1)
        imageList.add(l2)
        imageList.add(l3)
        imageList.add(l4)
        imageList.add(l5)

    }
}