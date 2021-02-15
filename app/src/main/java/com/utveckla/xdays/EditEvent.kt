package com.utveckla.xdays

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.DatePicker
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.utveckla.xdays.db.AppDatabase
import com.utveckla.xdays.db.Event
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class EditEvent : Fragment() {
    private var job: Job = Job()
    private val scope = CoroutineScope(job + Dispatchers.Main)

    private fun getEvent(): Event {
        val event = Event()

        if (arguments != null) {
            event.id = arguments?.getString("id")!!.toInt()
            event.name = arguments?.getString("name")!!
            event.date = arguments?.getString("date")!!
        }

        return event
    }

    private fun deleteEvent() {
        AlertDialog.Builder(context).apply {
            setTitle("Forget forever?")
            setMessage("There is no undo button")
            setPositiveButton("Yup") { _, _ ->
                scope.launch {
                    context?.let {
                        AppDatabase(it).eventDao().deleteEvent(getEvent())
                        findNavController().navigate(R.id.action_EditEvent_to_EventList)
                    }
                }
            }
            setNegativeButton("Nurp") { _, _ ->
            }
        }.create().show()
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.edit_event, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val eventName = view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.event_name_edit)
        val eventDate = view.findViewById<DatePicker>(R.id.event_date_edit)

        val event = getEvent()

        if (arguments != null) {
            val originalDate = event.date
            val sdf = SimpleDateFormat("yyyy/MM/dd")
            val setDateFormatted: Date = sdf.parse(originalDate)

            val tempCal: Calendar = GregorianCalendar()
            tempCal.time = setDateFormatted

            eventName.setText(event.name)
            eventDate.init(tempCal[Calendar.YEAR], tempCal[Calendar.MONTH], tempCal[Calendar.DAY_OF_MONTH], null)
        }
        else {
            view.findViewById<FloatingActionButton>(R.id.fab).visibility = View.GONE;
        }

        view.findViewById<Button>(R.id.button_save_edit).setOnClickListener {
            val name: String = eventName.text.toString()
            val date: String = "" + eventDate.year + "/" + checkDigit(eventDate.month + 1).toString() + "/" + checkDigit(eventDate.dayOfMonth)
            event.name = name
            event.date = date

            if (name.isEmpty()) {
                eventName.error = "event name required"
                eventName.requestFocus()
                return@setOnClickListener
            }

            scope.launch {
                context?.let {
                    if (arguments != null) {
                        AppDatabase(it).eventDao().updateEvent(event)
                    }
                    else {
                        AppDatabase(it).eventDao().addEvent(event)
                    }
                }
            }

            eventName.text?.clear()
            findNavController().navigate(R.id.action_EditEvent_to_EventList)
        }

        view.findViewById<FloatingActionButton>(R.id.fab).setOnClickListener {
            deleteEvent()
        }
    }

    private fun checkDigit(number: Int): String? {
        return if (number <= 9) "0$number" else number.toString()
    }
}