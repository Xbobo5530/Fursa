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
import android.widget.ImageView
import android.widget.TextView
import com.nyayozangu.labs.fursa.R
import com.nyayozangu.labs.fursa.activities.CommentsActivity
import com.nyayozangu.labs.fursa.activities.UserPageActivity
import com.nyayozangu.labs.fursa.activities.ViewCategoryActivity
import com.nyayozangu.labs.fursa.activities.ViewPostActivity
import com.nyayozangu.labs.fursa.helpers.CoMeth
import com.nyayozangu.labs.fursa.helpers.CoMeth.*
import com.nyayozangu.labs.fursa.models.Notifications
import kotlinx.android.synthetic.main.notif_list_item.view.*

class NotificationsRecyclerAdapter(private val notificationsList: List<Notifications>,
                                   var context: Context) :
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
//        val notifId = notification.notif_id
        val status = notification.status

        holder.setData(title, message, status)
        val mImage = holder.itemView.notifImageView
        setImage(mImage, notifType, status)
        val notifButton =
                holder.itemView.findViewById<ConstraintLayout>(R.id.notifItemLayout)
        handleItemClick(notifButton, notification, notifType, extra, title, message)
    }

    private fun handleItemClick(notificationButton: ConstraintLayout, notification: Notifications,
                                notificationType: String?, extra: String?, title: String, message: String) {
        notificationButton.setOnClickListener {
            coMeth.updateNotificationStatus(notification, coMeth.uid)
            when (notificationType) {
                COMMENT_UPDATES -> openCommentsNotification(extra)
                CATEGORIES_UPDATES -> openCatsNotification(extra)
                LIKES_UPDATES -> openPostNotification(extra)
                NEW_POST_UPDATES -> openPostNotification(extra)
                FOLLOWER_POST -> openPostNotification(extra)
                NEW_FOLLOWERS_UPDATE -> openUserPage(extra)
                else -> {
                    openNofitDialog(title, message)
                }
            }
        }
    }

    private fun setImage(mImageView: ImageView, notificationType: String?, status: Int?) {

        when (notificationType) {
            COMMENT_UPDATES -> when(status){
                NOTIFICATION_STATUS_UNREAD -> mImageView.setImageDrawable(context.resources.getDrawable(R.drawable.ic_comments_green))
                NOTIFICATION_STATUS_READ -> mImageView.setImageDrawable(context.resources.getDrawable(R.drawable.ic_action_comment))
            }
            CATEGORIES_UPDATES -> when(status){
                NOTIFICATION_STATUS_UNREAD -> mImageView.setImageDrawable(context.resources.getDrawable(R.drawable.ic_notif_active_red))
                NOTIFICATION_STATUS_READ -> mImageView.setImageDrawable(context.resources.getDrawable(R.drawable.ic_action_notifications))
            }
            LIKES_UPDATES -> when(status){
                NOTIFICATION_STATUS_UNREAD -> mImageView.setImageDrawable(context.resources.getDrawable(R.drawable.ic_action_liked))
                NOTIFICATION_STATUS_READ -> mImageView.setImageDrawable(context.resources.getDrawable(R.drawable.ic_action_like_unclicked))
            }
            NEW_POST_UPDATES -> when (status){
                NOTIFICATION_STATUS_UNREAD -> mImageView.setImageDrawable(context.resources.getDrawable(R.drawable.ic_action_new_post_notif))
                NOTIFICATION_STATUS_READ -> mImageView.setImageDrawable(context.resources.getDrawable(R.drawable.ic_new_post_update_dark))
            }
            FOLLOWER_POST -> when(status){
                NOTIFICATION_STATUS_UNREAD -> mImageView.setImageDrawable(context.resources.getDrawable(R.drawable.ic_follower_post_active))
                NOTIFICATION_STATUS_READ -> mImageView.setImageDrawable(context.resources.getDrawable(R.drawable.ic_follower_post))
            }
            NEW_FOLLOWERS_UPDATE -> when(status){
                NOTIFICATION_STATUS_UNREAD -> mImageView.setImageDrawable(context.resources.getDrawable(R.drawable.ic_new_follower_update_active))
                NOTIFICATION_STATUS_READ -> mImageView.setImageDrawable(context.resources.getDrawable(R.drawable.ic_new_follower_update))
            }
            else -> {
                mImageView.setImageDrawable(
                        context.resources.getDrawable(R.drawable.ic_action_notifications))
            }
        }
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

    private fun openPostNotification(postId: String?) {
        val openLikedPostIntent = Intent(context, ViewPostActivity::class.java)
        openLikedPostIntent.putExtra(POST_ID, postId)
        context.startActivity(openLikedPostIntent)
    }

    private fun openUserPage(userId: String?) {
        val openFollowerPageIntent = Intent(context, UserPageActivity::class.java)
        openFollowerPageIntent.putExtra(USER_ID, userId)
        context.startActivity(openFollowerPageIntent)
    }

    private fun openCatsNotification(cat: String?) {
        val openCatsIntent = Intent(context, ViewCategoryActivity::class.java)
        openCatsIntent.putExtra(CATEGORY, cat)
        context.startActivity(openCatsIntent)
    }

    private fun openCommentsNotification(postId: String?) {
        val openCommentsIntent = Intent(context, CommentsActivity::class.java)
        openCommentsIntent.putExtra(POST_ID, postId)
        openCommentsIntent.putExtra(SOURCE, NOTIFICATIONS_VAL)
        context.startActivity(openCommentsIntent)
    }

    override fun getItemCount() = notificationsList.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

//        val notifImage = itemView.findViewById<ImageView>(R.id.notifImageView)

        fun setData(title: String, message: String, status: Int?) {
            itemView.findViewById<TextView>(R.id.notifTitleTextView).text = title
            itemView.findViewById<TextView>(R.id.notifDescTextView).text = message
            val notifLayout = itemView.findViewById<ConstraintLayout>(R.id.notifItemLayout)
            if (status == NOTIFICATION_STATUS_UNREAD) { // 0 is unread
                notifLayout.setBackgroundColor(ContextCompat.getColor(itemView.context,
                        R.color.colorWhite))
            } else {
                notifLayout.setBackgroundColor(ContextCompat.getColor(itemView.context,
                        R.color.grey_background))
            }
        }
    }
}
