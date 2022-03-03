package com.taweewong.horizontalscrollbar

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.taweewong.horizontalscrollbar.databinding.ViewItemBinding

class MyAdapter(private val itemList: List<String>) : RecyclerView.Adapter<MyAdapter.MyItemViewHolder>() {

    override fun getItemCount(): Int = itemList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyItemViewHolder {
        return MyItemViewHolder(
            ViewItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: MyItemViewHolder, position: Int) {
        holder.bind(itemList[position])
    }

    inner class MyItemViewHolder(binding: ViewItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: String) {

        }
    }
}