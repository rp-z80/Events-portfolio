package com.example.meeting

import android.annotation.SuppressLint
import android.content.Intent
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.event_cardview_layout.view.*
import kotlinx.android.synthetic.main.fragment_profile.*
import org.w3c.dom.Text
import java.text.SimpleDateFormat


class RecyclerAdapter(private val eventsList: List<Event>) : RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.event_cardview_layout,
            parent,
            false
        )
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return eventsList.size
    }

    @SuppressLint("SimpleDateFormat", "SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.apply{

            Picasso.get().load(eventsList[position].imageLocation.toString()).into(imageViewCardView)

            eventCategoryTextView.text = eventsList[position].category
            eventTitleTextView.text = eventsList[position].title

            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm")

            val eventDateString = "${eventsList[position].date} ${eventsList[position].time}"

            val eventDate = sdf.parse(eventDateString)

            dateAndTimeTextViewCardView.text = "" +
                    "${DateFormat.format("EEEE", eventDate)} " +
                    "${DateFormat.format("dd", eventDate)} " +
                    "${DateFormat.format("MMM", eventDate)} " +
                    "${eventsList[position].time}"

            eventDescriptionTextView.text = eventsList[position].description
        }
    }


    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var itemCategory : TextView = itemView.findViewById(R.id.eventCategoryTextView)
        var itemTitle: TextView = itemView.findViewById(R.id.eventTitleTextView)
        var itemDate: TextView = itemView.findViewById(R.id.dateAndTimeTextViewCardView)
        var itemDescription: TextView = itemView.findViewById(R.id.eventDescriptionTextView)

        init {
        itemView.setOnClickListener{
            val position: Int = adapterPosition

            //on click on event in homepage we open up the EventActivity passing the Event object with the
            //data to fill EventActivity layout with all Event info
            val intent = Intent(itemView.context, EventActivity::class.java)
            intent.putExtra("EVENT", eventsList[position])
            itemView.context.startActivity(intent)

            }
        }
    }


}


