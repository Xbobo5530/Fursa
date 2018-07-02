package com.nyayozangu.labs.fursa.fragments


import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.Query
import com.nyayozangu.labs.fursa.R
import com.nyayozangu.labs.fursa.adapters.NotificationsRecyclerAdapter
import com.nyayozangu.labs.fursa.helpers.CoMeth
import com.nyayozangu.labs.fursa.helpers.CoMeth.*
import com.nyayozangu.labs.fursa.models.Notifications
import kotlinx.android.synthetic.main.activity_main.*

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
    var notification = Notifications()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        // Inflate the layout for this fragment
        val view =
                inflater.inflate(R.layout.fragment_notifications, container, false)
        val notifRecyclerView = view.findViewById<RecyclerView>(R.id.notifRecyclerView)
        coMeth.handlePostsView(context, activity, notifRecyclerView)
        val notificationsList = ArrayList<Notifications>()
        val adapter = NotificationsRecyclerAdapter(notificationsList, this.context!!)
        notifRecyclerView.adapter = adapter

        val progressBar = view.findViewById<ProgressBar>(R.id.notifsProgressBar)
        val userId = coMeth.uid

        if (!coMeth.isConnected) showSnack(resources.getString(R.string.failed_to_connect_text))

        loadNotifications(userId, notificationsList, adapter, progressBar)

        val newPostFab = activity?.newPostFab
        newPostFab?.setImageDrawable(view.resources.getDrawable(R.drawable.ic_clear_white))
        //have the new post fab clear notifications when notif fragment visible
        newPostFab?.setOnClickListener(View.OnClickListener {
            Log.d(TAG, "clear notifs is clicked")
            for (notification in notificationsList) {
                coMeth.updateNotificationStatus(notification, coMeth.uid)
            }
            loadNotifications(userId, notificationsList, adapter, progressBar)
        })

        return view
    }

    private fun loadNotifications(userId: String?,
                                  notificationsList: ArrayList<Notifications>,
                                  adapter: NotificationsRecyclerAdapter,
                                  progressBar: ProgressBar) {
        notificationsList.clear()
        coMeth.db
                .collection("$USERS/$userId/$NOTIFICATIONS")
//                .orderBy(STATUS)
                .orderBy(TIMESTAMP, Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener { queryDocumentSnapshots ->
                    if (!queryDocumentSnapshots.isEmpty) {
                        for (doc in queryDocumentSnapshots.documentChanges) {
                            if (doc.type == DocumentChange.Type.ADDED) {
                                val notificationId = doc.document.id

                                notification = doc.document.toObject(Notifications::class.java)
                                notification.doc_id = notificationId
                                notificationsList.add(notification)
                                adapter.notifyDataSetChanged()
                                progressBar.visibility = View.GONE

                            }
                        }
                    } else {
                    }
                }
                .addOnFailureListener { Log.d(TAG, "onFailure: failed to fetch latest comment") }
    }

    private fun showSnack(message: String) {
        activity?.findViewById<View>(R.id.mainSnack)?.let {
            Snackbar.make(it, message, Snackbar.LENGTH_LONG).show()
        }
    }
}
