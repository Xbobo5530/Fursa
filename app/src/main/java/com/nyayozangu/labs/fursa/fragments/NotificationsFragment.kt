package com.nyayozangu.labs.fursa.fragments


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.Query
import com.nyayozangu.labs.fursa.R
import com.nyayozangu.labs.fursa.adapters.NotificationsRecyclerAdapter
import com.nyayozangu.labs.fursa.helpers.CoMeth
import com.nyayozangu.labs.fursa.helpers.CoMeth.*
import com.nyayozangu.labs.fursa.models.Notifications
import kotlinx.android.synthetic.main.activity_main.*


/**
 * A simple [Fragment] subclass.
 *
 */
class NotificationsFragment : Fragment() {
    val coMeth = CoMeth()
    private lateinit var toast: Toast
    private var notification = Notifications()
    private lateinit var notificationsList: MutableList<Notifications>
    lateinit var adapter: NotificationsRecyclerAdapter
    private lateinit var mProgressBar: ProgressBar

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view =
                inflater.inflate(R.layout.fragment_notifications, container, false)
        val notifRecyclerView = view.findViewById<RecyclerView>(R.id.notifRecyclerView)
        coMeth.handlePostsView(context, activity, notifRecyclerView)
        notificationsList = ArrayList<Notifications>()
        adapter = NotificationsRecyclerAdapter(notificationsList, this.context!!)
        notifRecyclerView.adapter = adapter

        mProgressBar = view.findViewById<ProgressBar>(R.id.notifsProgressBar)
        val userId = coMeth.uid

        toast = Toast.makeText(context, "", Toast.LENGTH_LONG)
        if (!coMeth.isConnected) coMeth.showToast(toast, activity, context,
                resources.getString(R.string.failed_to_connect_text))

        loadNotifications()

        val newPostFab = activity?.newPostFab
        newPostFab?.setImageDrawable(view.resources.getDrawable(R.drawable.ic_clear_white))
        newPostFab?.setOnClickListener(View.OnClickListener {
            Log.d(TAG, "clear notifs is clicked")
            for (notification in notificationsList) {
                if (notification.status == 0) {
                    coMeth.updateNotificationStatus(notification, coMeth.uid)
                }
            }
            loadNotifications()
        })

        return view
    }

    override fun onDetach() {
        super.onDetach()
        toast.cancel()
    }

    override fun onPause() {
        super.onPause()
        loadNotifications()
    }

    override fun onStart() {
        super.onStart()
        loadNotifications()
    }

    private fun loadNotifications() {
        notificationsList.clear()
        val userId = coMeth.uid
        coMeth.db.collection("$USERS/$userId/$NOTIFICATIONS")
//                .orderBy(STATUS)
                .orderBy(TIMESTAMP, Query.Direction.DESCENDING).get()
                .addOnSuccessListener {
                    if (!it.isEmpty) {
                        for (document in it.documentChanges) {
                            if (document.type == DocumentChange.Type.ADDED) {
                                val notificationId = document.document.id
                                notification = document.document.toObject(Notifications::class.java)
                                notification.doc_id = notificationId
                                notificationsList.add(notification)
                                adapter.notifyDataSetChanged()
                                mProgressBar.visibility = View.GONE
                            }
                        }
                    } else {
                        val message = resources.getString(R.string.notifs_will_appear_here_text)
//                                coMeth.showToast(toast, activity, activity, message)
                        mProgressBar.visibility = View.GONE
                    }
                }
                .addOnFailureListener {
                    Log.d(TAG, "Failed to get notifications\n${it.message}")
//                    coMeth.showToast(toast, activity, context,
//                            "${resources.getString(R.string.failed_to_get_notifications)}: " +
//                                    "${it.message}")
                }
    }
}