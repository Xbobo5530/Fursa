package com.nyayozangu.labs.fursa.adapters

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.support.constraint.ConstraintLayout
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.nyayozangu.labs.fursa.R
import com.nyayozangu.labs.fursa.activities.CommentsActivity
import com.nyayozangu.labs.fursa.activities.ViewCategoryActivity
import com.nyayozangu.labs.fursa.activities.ViewPostActivity
import com.nyayozangu.labs.fursa.helpers.CoMeth
import com.nyayozangu.labs.fursa.helpers.CoMeth.*
import com.nyayozangu.labs.fursa.models.Notifications

class NotificationsRecyclerAdapter(val notificationsList: List<Notifications>, var context: Context) :
        RecyclerView.Adapter<NotificationsRecyclerAdapter.ViewHolder>() {

    private val coMeth: CoMeth = CoMeth()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            NotificationsRecyclerAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.notif_list_item, parent, false)
        this.context = parent.context
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val notification = notificationsList[position]
        val title = notification.title
        val message = notification.message
        val notifType = notification.notif_type
        val extra = notification.extra
        val notifId = notification.notif_id
        val status = notification.status

        holder.setData(title, message, status)

        val notifButton =
                holder.itemView.findViewById<ConstraintLayout>(R.id.notifItemLayout)
        notifButton.setOnClickListener(View.OnClickListener {

            coMeth.updateNotificationStatus(notification, coMeth.uid)
//            updateNotificationStatus(notification)
            when (notifType) {
                CoMeth.COMMENT_UPDATES -> openCommentsNotif(holder, extra)
                CoMeth.CATEGORIES_UPDATES -> openCatsNotif(extra)
                CoMeth.LIKES_UPDATES -> openPostNotif(extra)
                CoMeth.NEW_POST_UPDATES -> openPostNotif(extra)
                else -> {
                    openNofitDialog(title, message)
                }
            }
        })
    }

    private fun openNofitDialog(title: String, message: String) {
        val notifDialogBuilder = AlertDialog.Builder(context)
        notifDialogBuilder.setTitle(title)
                .setMessage(message)
                .setIcon(context.resources.getDrawable(R.drawable.appiconshadow))
                .setPositiveButton(context.resources.getString(R.string.ok_text)) { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
    }

    private fun openPostNotif(postId: String?) {
        val openLikedPostIntent = Intent(context, ViewPostActivity::class.java)
        openLikedPostIntent.putExtra(POST_ID, postId)
        context.startActivity(openLikedPostIntent)
    }

    private fun openCatsNotif(cat: String?) {
        val openCatsIntent = Intent(context, ViewCategoryActivity::class.java)
        openCatsIntent.putExtra(CATEGORY, cat)
        context.startActivity(openCatsIntent)
    }

    private fun openCommentsNotif(holder: ViewHolder, postId: String?) {
        val openCommentsIntent = Intent(context, CommentsActivity::class.java)
        openCommentsIntent.putExtra(POST_ID, postId)
        openCommentsIntent.putExtra(SOURCE, NOTIFICATIONS_VAL)
        context.startActivity(openCommentsIntent)
    }

    override fun getItemCount() = notificationsList.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun setData(title: String, message: String, status: Int?) {
            itemView.findViewById<TextView>(R.id.notifTitleTextView).text = title
            itemView.findViewById<TextView>(R.id.notifDescTextView).text = message
            val notifLayout = itemView.findViewById<ConstraintLayout>(R.id.notifItemLayout)
            if (status == 0) { // 0 means unread
                notifLayout.setBackgroundColor(ContextCompat.getColor(itemView.context,
                        R.color.colorWhite))
            } else {
                notifLayout.setBackgroundColor(ContextCompat.getColor(itemView.context,
                        R.color.black_transparent))
            }
        }
    }
}
