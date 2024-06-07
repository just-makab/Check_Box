package com.example.checkbox

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CategoryAdapter(
    private val categoryTitleItemArrayList: ArrayList<CategoryTitleItem>
) : RecyclerView.Adapter<CategoryAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.categories_rv, parent, false)
        return MyViewHolder(v)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val categoryTitleItem = categoryTitleItemArrayList[position]
        holder.categoryTitle.text = categoryTitleItem.title
    }

    override fun getItemCount(): Int = categoryTitleItemArrayList.size

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryTitle: TextView = itemView.findViewById(R.id.CategoryItem)
    }
}