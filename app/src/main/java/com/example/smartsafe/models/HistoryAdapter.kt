package com.example.smartsafe.models

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.smartsafe.R
import java.text.SimpleDateFormat
import java.util.*

class HistoryAdapter(context: Context, data: List<HistoryItem>) :
    ArrayAdapter<HistoryItem>(context, R.layout.list_item_history, data) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        val holder: ViewHolder

        // Create a new ViewHolder if the view hasn't been inflated yet
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.list_item_history, parent, false)
            holder = ViewHolder(
                view.findViewById(R.id.timestampTextView),
                view.findViewById(R.id.imageView),
                view.findViewById(R.id.idTextView)
            )
            view.tag = holder
        } else {
            holder = view.tag as ViewHolder
        }

        // Bind the data to the ViewHolder's views
        val historyItem = getItem(position) ?: return view!!

        holder.timestampTextView.text = SimpleDateFormat("MMM d, yyyy hh:mm a", Locale.getDefault()).format(historyItem.timestamp)
        holder.imageView.setImageResource(R.drawable.ic_image_placeholder)
        holder.idTextView.text = "ID: ${historyItem?.id}"
        return view!!
    }

    // ViewHolder class to hold the views for each item in the list
    private data class ViewHolder(val timestampTextView: TextView, val imageView: ImageView, val idTextView: TextView)
}
