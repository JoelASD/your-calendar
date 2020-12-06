package com.mp.yourcalendar.ui.home

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mp.yourcalendar.Event
import com.mp.yourcalendar.R
import kotlinx.android.synthetic.main.event_item.view.*
import kotlinx.android.synthetic.main.event_list.*

/**
 * This Fragment is for showing the events of a day
 */
class EventSlidePageFragment: Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.event_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val events: ArrayList<Event> = requireArguments().getParcelableArrayList("events")!!
        val originalEvents: ArrayList<Event> = requireArguments().getParcelableArrayList("originalEvents")!!
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = EventsAdapter(events, originalEvents )
    }

    inner class EventsAdapter(val events: ArrayList<Event>, val originalEvents: MutableList<Event>): RecyclerView.Adapter<EventsAdapter.EventViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
            return EventViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.event_item, parent, false))
        }

        override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
            val e: Event = events[position]
            //name
            holder.nameTextView.text = e.eventName
            //startdatetime
            //startdatetime, if multi day event, show first day end time 23:59
            holder.startDateTimeText.text =
                if (e.eventStartDate != e.eventEndDate) "${e.eventStartTime} - 23:59"
                else if (e.eventStartTime == "00:00" && e.eventEndTime == "23:59") "Whole day"
                else "${e.eventStartTime} - ${e.eventEndTime}"
            //holder.startDateTimeText.text = "${e.eventStartTime} - ${e.eventEndTime}"
            //color of the event
            //rv.setInt(R.id.colorTextView, "setBackgroundResource", R.drawable.box_blue)
            val drawable = holder.colorTextView.background as GradientDrawable
            when (e.eventType) {
                0 -> drawable.setColor(Color.BLUE) //holder.colorTextView.setBackgroundResource(R.drawable.box_blue)
                1 -> holder.colorTextView.setBackgroundResource(R.drawable.box_green)
                2 -> drawable.setColor(Color.RED) //holder.colorTextView.setBackgroundResource(R.drawable.box_red)
                3 -> holder.colorTextView.setBackgroundResource(R.drawable.box_yellow)
                4 -> holder.colorTextView.setBackgroundResource(R.drawable.box_pink)
                5 -> holder.colorTextView.setBackgroundResource(R.drawable.box_purple)
                6 -> holder.colorTextView.setBackgroundResource(R.drawable.box_brown)
                else -> holder.colorTextView.setBackgroundResource(R.color.black)
            }
        }

        override fun getItemCount(): Int {
            return events.size
        }

        inner class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val nameTextView: TextView = itemView.itemNameTextView
            val startDateTimeText: TextView = itemView.itemTimeTextView
            val colorTextView: TextView = itemView.colortextView

            init {
                itemView.setOnClickListener {
                    val position = adapterPosition
                    if (position == RecyclerView.NO_POSITION) {
                        return@setOnClickListener
                    }
                    //val event = events[position]
                    // From the list with only original events, find event that matches eventKey with event from properList
                    //val dEvent: Event? = oEvents.find { it.eventKey == events[position].eventKey }
                    //val dEvent: Event? = events.find { it.eventKey == events[com.mp.yourcalendar.ui.home.EventsAdapter.position].eventKey }
                    val dEvent: Event? = originalEvents.find {it.eventKey == events[position].eventKey }
                    val action: NavDirections = HomeFragmentDirections.actionNavHomeToEventDetailFragment(dEvent!!)
                    findNavController().navigate(action)
                }
            }
        }
    }
}