package com.nyayozangu.labs.fursa.adapters

import android.app.AlertDialog
import android.content.Context
import android.support.design.widget.Snackbar
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.firebase.firestore.FieldValue
import com.nyayozangu.labs.fursa.R
import com.nyayozangu.labs.fursa.activities.LoginActivity
import com.nyayozangu.labs.fursa.activities.MainActivity
import com.nyayozangu.labs.fursa.helpers.CoMeth
import com.nyayozangu.labs.fursa.helpers.CoMeth.*
import com.nyayozangu.labs.fursa.models.Promotion
import com.nyayozangu.labs.fursa.models.User
import org.jetbrains.anko.startActivity
import java.util.*
import java.util.Calendar.DAY_OF_YEAR


class PromotionsRecyclerAdapter(private val promotionsList :List<Promotion>, val postId: String, var context: Context):
        RecyclerView.Adapter<PromotionsRecyclerAdapter.ViewHolder>() {

    private val common: CoMeth = CoMeth()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PromotionsRecyclerAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.promotion_list_item, parent, false)
        this.context = parent.context
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = promotionsList.size

    override fun onBindViewHolder(holder: PromotionsRecyclerAdapter.ViewHolder, position: Int) {
        val promotion = promotionsList[position]
        holder.setPromoDetails(promotion)
        holder.promoItemView.setOnClickListener{
            handleBuyingPromotion(holder, promotion)
        }
    }

    private fun handleBuyingPromotion(holder: ViewHolder, promotion: Promotion) {
        val cost = promotion.cost
        if (common.isLoggedIn){
            val currentUserId = common.uid
            common.db.collection(USERS).document(currentUserId).get()
                    .addOnSuccessListener {
                        if (it.exists()){
                            val user = it.toObject(User::class.java)
                            if (user != null){
                                val userCredit = user.credit
                                if (userCredit >= cost){
                                    buyPromotion(holder, promotion, userCredit, currentUserId)
                                }else{
                                    showInsufficientCreditDialog()
                                }
                            }
                        }
                    }
                    .addOnFailureListener {
                        Log.w(TAG, "failed to get user details\n${it.message}",it)
                        val message = "${context.getString(R.string.error_text)}: ${it.message}"
                        Snackbar.make(holder.promoItemView, message, Snackbar.LENGTH_LONG)
                                .setAction(context.getString(R.string.retry_text)) { _ ->
                                    handleBuyingPromotion(holder, promotion)
                                }
                                .show()
                    }

        }else{
            val loginMessage = context.getString(R.string.login_to_promote)
            context.startActivity<LoginActivity>(loginMessage to MESSAGE)
        }
    }

    private fun showInsufficientCreditDialog() {
        AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.insufficient_credit_text))
                .setMessage(context.getString(R.string.insufficient_credit_message_for_promo_message))
                .setIcon(context.resources.getDrawable(R.drawable.ic_credit))
                .setPositiveButton(context.getString(R.string.ok_text)){ dialog, _ ->
                    dialog.dismiss()
                }
                .show()
    }

    private fun buyPromotion(holder: ViewHolder, promotion: Promotion, userCredit: Int, currentUserId: String){
        val duration = promotion.duration
        val cost = promotion.cost
        val balanceCredit = userCredit - cost
        val userCreditMap = hashMapOf<String, Any>(CREDIT to balanceCredit)
        common.db.collection(USERS).document(currentUserId).update(userCreditMap)
                .addOnSuccessListener {
                    addSponsoredPost(holder, currentUserId, duration, cost, balanceCredit)
                }
                .addOnFailureListener {
                    Log.w(TAG, "failed to update user credit\n${it.message}", it)
                    val message = "${context.getString(R.string.error_text)}: ${it.message}"
                    Snackbar.make(holder.promoItemView, message, Snackbar.LENGTH_LONG)
                            .setAction(context.getString(R.string.retry_text)) { _ ->
                                buyPromotion(holder, promotion, userCredit, currentUserId)
                            }
                            .show()
                }
    }

    private fun addSponsoredPost(holder: ViewHolder, currentUserId: String, duration: Int, cost: Int, balanceCredit: Int) {

        val expiresAt = GregorianCalendar().apply {
            add(DAY_OF_YEAR, duration)
        }
        val sponsoredPostMap = hashMapOf(
                USER_ID_VAL to currentUserId,
                POST_ID_VAL to postId,
                CREATED_AT to FieldValue.serverTimestamp(),
                EXPIRES_AT to expiresAt.time
        )
        common.db.collection(SPONSORED).add(sponsoredPostMap).addOnSuccessListener {
            showSuccessDialog(cost, balanceCredit, duration)
        }
                .addOnFailureListener {
                    Log.w(TAG, "failed to add sponsored post\n${it.message}", it)
                    val message = "${context.getString(R.string.error_text)}: ${it.message}"
                    Snackbar.make(holder.promoItemView, message, Snackbar.LENGTH_LONG)
                            .setAction(context.getString(R.string.retry_text)) { _ ->
                                addSponsoredPost(holder, currentUserId, duration, cost, balanceCredit)
                            }
                            .show()
                }
    }

    private fun showSuccessDialog(cost: Int, balanceCredit: Int, duration: Int) {
        val message = "${context.getString(R.string.post_promoted_message)}\n" +
                "You have spent $cost credits\n" +
                "You now have $balanceCredit credit(s) in your account\n" +
                "Your post will be promoted for $duration day(s)"
        AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.congratultions_text))
                .setMessage(message)
                .setIcon(context.resources.getDrawable(R.drawable.ic_credit))
                .setPositiveButton(context.getString(R.string.ok_text)){ dialog, _ ->
                    dialog.dismiss()
                    context.startActivity<MainActivity>()
                }
                .setCancelable(false)
                .show()
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        val promoItemView: CardView = itemView.findViewById(R.id.promoCardView)

        fun setPromoDetails(promotion: Promotion){
            val title = promotion.title
            val description = promotion.description
            val cost = promotion.cost
            itemView.findViewById<TextView>(R.id.promoTitleTextView).text = title
            itemView.findViewById<TextView>(R.id.promoDescTextView).text = description
            itemView.findViewById<TextView>(R.id.promoCreditAmountTextView).text = "$cost"
        }
    }

}