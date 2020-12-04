package com.mp.yourcalendar.ui.home

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.mp.yourcalendar.Event
import com.mp.yourcalendar.R
import com.mp.yourcalendar.ui.newevent.NewEventFragmentDirections
import kotlinx.android.synthetic.main.event_item.view.*
import org.w3c.dom.Text

class EventsAdapter(private val parentFragment: HomeFragment, allEvents: MutableList<Event>, originalEvents: MutableList<Event>) : RecyclerView.Adapter<EventsAdapter.ViewHolder>() {
    private val onClickListener: View.OnClickListener

    //private val eventit: MutableList<Event> = events

    // properList from home fragment (all events)
    private val events: MutableList<Event> = allEvents
    // eventList from home fragment (only original events)
    private val oEvents: MutableList<Event> = originalEvents

    companion object {
        var position: Int = 0
    }

    init {
        onClickListener = View.OnClickListener { v ->
            position = v.tag as Int

            /*val dEvent: Event = events[position]
            Log.d("Event", "${dEvent.eventName}")
            Log.d("Event", "${dEvent.eventKey}")*/

            // From the list with only original events, find event that matches eventKey with event from properList
            val dEvent: Event? = oEvents.find { it.eventKey == events[position].eventKey }


            val action: NavDirections = HomeFragmentDirections.actionNavHomeToEventDetailFragment(dEvent!!)
            parentFragment.findNavController().navigate(action)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventsAdapter.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.event_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = events.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val nameTextView: TextView = view.itemNameTextView
        val startDateTimeText: TextView = view.itemTimeTextView
        val colorTextView: TextView = view.colortextView

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val e: Event = events.get(position)
        //name
        holder.nameTextView.text = e.eventName
        //startdatetime
        //holder.startDateTimeText.text = "${e.eventStartTime} - ${e.eventEndTime}"
        //startdatetime, if multi day event, show first day end time 23:59
        holder.startDateTimeText.text =
                if (e.eventStartDate != e.eventEndDate) "${e.eventStartTime} - 23:59"
                else "${e.eventStartTime} - ${e.eventEndTime}"
        //color of the event
        //rv.setInt(R.id.colorTextView, "setBackgroundResource", R.drawable.box_blue)
        val drawable = holder.colorTextView.background as GradientDrawable
        Log.d("EventsAdapter", drawable.toString())
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


        // Onclicklistener for viewholders
        with(holder.itemView) {
            tag = position
            setOnClickListener(onClickListener)
        }
    }
}

/*class EventsAdapter(private val parentFragment: HomeFragment) : RecyclerView.Adapter<EventsAdapter.ViewHolder>() {
    private val onClickListener: View.OnClickListener

    companion object {
        var position: Int = 0
    }

    init {
        onClickListener = View.OnClickListener { v ->
            position = v.tag as Int

            val dEvent: Event = HomeFragment.eventList[position]
            Log.d("Event", "${dEvent.eventName}")

            val action: NavDirections = HomeFragmentDirections.actionNavHomeToEventDetailFragment(dEvent)
            parentFragment.findNavController().navigate(action)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventsAdapter.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.event_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = HomeFragment.eventList.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val nameTextView: TextView = view.itemNameTextView
        val startDateTimeText: TextView = view.itemTimeTextView
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val e: Event = HomeFragment.eventList.get(position)
        //name
        holder.nameTextView.text = e.eventName
        //startdatetime
        holder.startDateTimeText.text = "${e.eventStartTime} - ${e.eventEndTime}"

        // Onclicklistener for viewholders
        with(holder.itemView) {
            tag = position
            setOnClickListener(onClickListener)
        }
    }
}*/
