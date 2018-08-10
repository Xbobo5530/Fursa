package com.nyayozangu.labs.fursa.activities

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.Query
import com.nyayozangu.labs.fursa.R
import com.nyayozangu.labs.fursa.adapters.PromotionsRecyclerAdapter
import com.nyayozangu.labs.fursa.helpers.CoMeth
import com.nyayozangu.labs.fursa.helpers.CoMeth.*
import com.nyayozangu.labs.fursa.models.Promotion
import com.nyayozangu.labs.fursa.models.User
import kotlinx.android.synthetic.main.activity_promote_post.*
import kotlinx.android.synthetic.main.content_promote_post.*

class PromotePostActivity : AppCompatActivity() {

    private lateinit var mAdapter: PromotionsRecyclerAdapter
    private val promotionsList: MutableList<Promotion> = ArrayList()
    private val common: CoMeth = CoMeth()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_promote_post)

        //set up the toolbar
        setSupportActionBar(promotePostToolbar)
        val actionBar = supportActionBar
        actionBar?.title = getString(R.string.promotions_text)
        actionBar?.setDisplayHomeAsUpEnabled(true)
        promotePostToolbar.setNavigationOnClickListener { finish() }

        val postId = getPostId()

        handleCreditCount()

        if (postId != null)
            mAdapter = PromotionsRecyclerAdapter(promotionsList, postId, this)
        common.handlePostsView(this, this, promotePostRecyclerView)
        promotePostRecyclerView.adapter = mAdapter
        loadPromotions()
    }

    private fun handleCreditCount() {
        val currentUserId = common.uid
        common.db.collection(USERS).document(currentUserId).addSnapshotListener {
            documentSnapshot, e ->
            if (e == null){
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    val user = documentSnapshot.toObject(User::class.java)
                    val credit = user?.credit
                    promotePostCreditTextView.text = credit.toString()
                }
            }else{
                Log.w(TAG, "failed to check for use credit\n${e.message}", e)
            }
        }
    }

    private fun getPostId(): String? {
        return if (intent != null){
            val postId = intent.getStringExtra(POST_ID)
            if (postId != null) {
                postId
            }else{
                goToMain()
                null
            }
        }else{
            goToMain()
            null
        }
    }

    private fun loadPromotions(){
        common.showProgress(promotePostProgressBar)
        common.db.collection(PROMOTIONS).orderBy(COST, Query.Direction.ASCENDING).get()
                .addOnSuccessListener {
                    if (!it.isEmpty){
                        promotionsList.clear()
                        for (document in it.documentChanges){
                            if (document.type == DocumentChange.Type.ADDED){
                                val promotion = document.document.toObject(Promotion::class.java)
                                promotionsList.add(promotion)
                                mAdapter.notifyItemInserted(promotionsList.size - 1)
                                common.stopLoading(promotePostProgressBar)
                            }
                        }
                    }
                }
    }

    private fun goToMain() {
        val goToMainIntent = Intent(this, MainActivity::class.java)
        goToMainIntent.putExtra(ACTION, NOTIFY)
        goToMainIntent.putExtra(MESSAGE, getString(R.string.something_went_wrong_text))
        startActivity(goToMainIntent)
        finish()
    }
}
