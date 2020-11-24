package com.mp.yourcalendar.ui.home

import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.spans.DotSpan

class EventDecorator(color: Int, dates: Collection<CalendarDay>) : DayViewDecorator {

    private var color: Int = color
    private var dates: HashSet<CalendarDay> = HashSet(dates)

    /*EventDecorator(color: Int, dates: Collection<CalendarDay>) {
        this.color = color
        this.dates = HashSet(dates)
    }*/

    override fun shouldDecorate(day: CalendarDay?): Boolean {
        return dates.contains(day)
    }

    override fun decorate(view: DayViewFacade?) {
        if (view != null) {
            view.addSpan(DotSpan(5F, color))
        }
    }
}