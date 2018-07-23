package com.nyayozangu.labs.fursa.activities

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.widget.Switch
import com.bumptech.glide.Glide
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentChange
import com.nyayozangu.labs.fursa.R
import com.nyayozangu.labs.fursa.adapters.UsersRecyclerAdapter
import com.nyayozangu.labs.fursa.helpers.CoMeth
import com.nyayozangu.labs.fursa.helpers.CoMeth.*
import com.nyayozangu.labs.fursa.models.Users
import kotlinx.android.synthetic.main.activity_users.*
import kotlinx.android.synthetic.main.content_users.*

class UsersActivity: AppCompatActivity() {

    private val coMeth: CoMeth = CoMeth()
    private lateinit var mAdapter: UsersRecyclerAdapter
    private val usersList: MutableList<Users> = ArrayList()
    private lateinit var userId: String
    private lateinit var destination: String
    private lateinit var actionBar: ActionBar
    private var mProgressDialog: ProgressDialog? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_users)

        handleToolBar()
        handleIntent()
        mAdapter = UsersRecyclerAdapter(usersList, Glide.with(this))
        coMeth.handlePostsView(this, this, usersRecyclerView)
        usersRecyclerView.adapter = mAdapter

        val  followRef: CollectionReference = coMeth.db.collection(
                "$USERS/$userId/$destination")
        loadUsers(followRef)

    }

    private fun handleToolBar() {
        setSupportActionBar(usersToolbar)
        actionBar = this.supportActionBar!! // check this
        actionBar.setDisplayHomeAsUpEnabled(true)
        usersToolbar.setNavigationOnClickListener { finish() }
        usersToolbar.setOnClickListener { _ ->
            usersRecyclerView.smoothScrollToPosition(0)
        }
    }

    private fun loadUsers(followRef: CollectionReference) {
        showProgress(getString(R.string.loading_text))
        followRef.get().addOnSuccessListener {
            if (!it.isEmpty){
                for (mDocument in it.documentChanges){
                    if (mDocument.type == DocumentChange.Type.ADDED){
                        val userId = mDocument.document.id
                        val userRef = coMeth.db.collection(USERS).document(userId)
                        userRef.get().addOnSuccessListener {
                            if (it.exists()){
                                val user = it.toObject(Users::class.java)!!.withId<Users>(userId)
                                usersList.add(user)
                                mAdapter.notifyDataSetChanged()
                                coMeth.stopLoading(mProgressDialog)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun handleIntent() {
        val intent = intent
        if (intent != null){
            if (intent.getStringExtra(DESTINATION) != null && intent.getStringExtra(USER_ID) != null){
                val mDestination = intent.getStringExtra(DESTINATION)
                userId = intent.getStringExtra(USER_ID)
                when(mDestination){
                    FOLLOWERS_VAL -> {
                        actionBar.title = FOLLOWERS
                        destination = FOLLOWERS
                    }
                    FOLLOWING_VAL -> {
                        actionBar.title = FOLLOWING
                        destination = FOLLOWING
                    }
                    else -> actionBar.title = USERS
                }
            }
        }
    }

    private fun showProgress(message: String){
        mProgressDialog = ProgressDialog(this)
        mProgressDialog!!.setMessage(message)
        mProgressDialog!!.show()
    }
}
