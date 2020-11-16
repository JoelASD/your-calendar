package com.mp.yourcalendar.ui.home

import android.content.Context
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

/*class EventsAdapter(val mContext: Context, val layoutResId: Int, val eventList: MutableList<Event>) : ArrayAdapter<Event>(mContext, layoutResId, eventList) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val layoutInflater: LayoutInflater = LayoutInflater.from(context)
        val view: View = layoutInflater.inflate(layoutResId, null)

        val itemNameText = view.findViewById<TextView>(R.id.itemNameTextView)
        val itemStartDateTime = view.findViewById<TextView>(R.id.itemDateTimeTextView)

        val event = eventList[position]

        itemNameText.text = event.eventName
        itemStartDateTime.text = "${event.eventStartDate} - ${event.eventStartTime}"

        return view
    }
}*/

class EventsAdapter(private val parentFragment: HomeFragment) : RecyclerView.Adapter<EventsAdapter.ViewHolder>() {
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
}
