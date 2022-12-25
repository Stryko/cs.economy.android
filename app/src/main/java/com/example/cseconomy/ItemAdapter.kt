package com.example.cseconomy

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class ItemAdapter : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    private var itemsList: ArrayList<ItemModel> = ArrayList()
    private var onClickItem:((ItemModel)->Unit)? = null
    private var onClickAddFavItem:((ItemModel)->Unit)? = null

    fun addItems(items: ArrayList<ItemModel>) {
        this.itemsList = items
        notifyDataSetChanged()
    }

    fun setOnClickItem(callback: (ItemModel)->Unit) {
        this.onClickItem = callback
    }

    fun setOnClickAddFavItem(callback: (ItemModel)->Unit) {
        this.onClickAddFavItem = callback
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ItemViewHolder (
        LayoutInflater.from(parent.context).inflate(R.layout.items_layout, parent, false)
    )

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val items = itemsList[position]
        holder.bindView(items)
        holder.itemView.setOnClickListener { onClickItem?.invoke(items) }
        holder.btnAddFav.setOnClickListener { onClickAddFavItem?.invoke(items) }
    }

    override fun getItemCount(): Int {
        return itemsList.size
    }

    class ItemViewHolder(var view: View) : RecyclerView.ViewHolder(view) {
        private var name = view.findViewById<TextView>(R.id.tvItemName)
        private var price = view.findViewById<TextView>(R.id.tvItemPrice)
        private var image = view.findViewById<ImageView>(R.id.tvItemImage)
        var btnAddFav = view.findViewById<ImageButton>(R.id.btnAddFav)

        fun bindView(item:ItemModel) {
            val imageLink = "https://community.cloudflare.steamstatic.com/economy/image/" + item.item_icon_url
            name.text = item.item_name
            price.text = "Price: " + item.item_last_price.toString() + " ${item.currency}";
            Picasso.get().load(imageLink).into(image)
        }
    }
}