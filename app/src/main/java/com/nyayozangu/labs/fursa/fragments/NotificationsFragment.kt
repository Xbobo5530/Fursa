package com.nyayozangu.labs.fursa.fragments


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import com.nyayozangu.labs.fursa.R
import com.nyayozangu.labs.fursa.adapters.NotificationsRecyclerAdapter
import com.nyayozangu.labs.fursa.helpers.CoMeth
import com.nyayozangu.labs.fursa.models.Notifications

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 *
 */
class NotificationsFragment : Fragment() {
    val coMeth = CoMeth()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_notifications, container, false)
        val notifRecyclerView = view.findViewById<RecyclerView>(R.id.notifRecyclerView)
        coMeth.handlePostsView(context, activity, notifRecyclerView)
        val notificationsList = ArrayList<Notifications>()
        val adapter = NotificationsRecyclerAdapter(notificationsList, this.context!!)
        notifRecyclerView.adapter = adapter

        //handle progress bar
        val progressBar = view.findViewById<ProgressBar>(R.id.notifsProgressBar)

        val userId = coMeth.uid
//        coMeth.db.collection(USERS + "/" + userId + "/" + NOTIFICATIONS).get()
        //TODO   .addOnSuccessListener(OnSuccessListener {})

        //sample notifications
        val testNotifOne = Notifications(
                "test",
                "test message",
                "test",
                "extra",
                1,
                0
        )
        val testNotifTwo = testNotifOne.copy(
                "test2",
                "message 2",
                status = 1)

        notificationsList.add(testNotifOne)
        notificationsList.add(testNotifTwo)

        adapter.notifyDataSetChanged()
        progressBar.visibility = View.GONE
        return view
    }
}
