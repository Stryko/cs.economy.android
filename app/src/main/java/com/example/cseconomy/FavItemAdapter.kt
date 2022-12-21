package com.example.cseconomy

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class FavItemAdapter : RecyclerView.Adapter<FavItemAdapter.FavItemViewHolder>() {

    private var itemsList: ArrayList<ItemModel> = ArrayList()
    private var onClickItem:((ItemModel)->Unit)? = null
    private var onClickRemoveFavItem:((ItemModel)->Unit)? = null

    fun addItems(items: ArrayList<ItemModel>) {
        this.itemsList = items
        notifyDataSetChanged()
    }

    fun setOnClickItem(callback: (ItemModel)->Unit) {
        this.onClickItem = callback
    }

    fun setOnClickRemoveFavItem(callback: (ItemModel)->Unit) {
        this.onClickRemoveFavItem = callback
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = FavItemViewHolder (
        LayoutInflater.from(parent.context).inflate(R.layout.fav_items_layout, parent, false)
    )

    override fun onBindViewHolder(holder: FavItemViewHolder, position: Int) {
        val items = itemsList[position]
        holder.bindView(items)
        holder.itemView.setOnClickListener { onClickItem?.invoke(items) }
        holder.btnRemoveFav.setOnClickListener { onClickRemoveFavItem?.invoke(items) }
    }

    override fun getItemCount(): Int {
        return itemsList.size
    }

    class FavItemViewHolder(var view: View) : RecyclerView.ViewHolder(view) {
        private var name = view.findViewById<TextView>(R.id.tvItemName)
        private var price = view.findViewById<TextView>(R.id.tvItemPrice)
        private var image = view.findViewById<ImageView>(R.id.tvItemImage)
        var btnRemoveFav = view.findViewById<ImageButton>(R.id.btnRemoveFav)

        fun bindView(item:ItemModel) {
            val imageLink = "https://community.cloudflare.steamstatic.com/economy/image/" + item.item_icon_url
            name.text = item.item_name
            price.text = item.item_last_price.toString()
            Picasso.get().load(imageLink).into(image)
        }
    }
}