package com.nyayozangu.labs.fursa.activities

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.util.Log
import com.bumptech.glide.Glide
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentSnapshot
import com.nyayozangu.labs.fursa.R
import com.nyayozangu.labs.fursa.adapters.UsersRecyclerAdapter
import com.nyayozangu.labs.fursa.helpers.CoMeth
import com.nyayozangu.labs.fursa.helpers.CoMeth.*
import com.nyayozangu.labs.fursa.models.User
import kotlinx.android.synthetic.main.activity_users.*
import kotlinx.android.synthetic.main.content_users.*
import org.jetbrains.anko.progressDialog
import org.jetbrains.anko.startActivity

class UsersActivity: AppCompatActivity() {

    private val coMeth: CoMeth = CoMeth()
    private lateinit var mAdapter: UsersRecyclerAdapter
    private val usersList: MutableList<User> = ArrayList()
    private lateinit var userId: String
    private lateinit var destination: String
    private lateinit var actionBar: ActionBar
    private var isFirstPageFirstLoad = true
    private lateinit var lastVisiblePost: DocumentSnapshot
    private lateinit var  followRef: CollectionReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_users)

        handleToolBar()
        handleIntent()
        mAdapter = UsersRecyclerAdapter(usersList, Glide.with(this))
        coMeth.handlePostsView(this, this, usersRecyclerView)
        usersRecyclerView.adapter = mAdapter
        followRef = coMeth.db.collection("$USERS/$userId/$destination")
        handleScrolling()
        loadUsers()
    }

    private fun handleScrolling() {
        usersRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val reachedBottom = !usersRecyclerView.canScrollVertically(1)
                if (reachedBottom) {
                    loadMoreUsers()
                }
            }
        })
    }

    private fun loadUsers() {
        coMeth.showProgress(usersProgressBar)
        followRef.limit(10).get().addOnSuccessListener { querySnapshot ->
            if (!querySnapshot.isEmpty){
                if (isFirstPageFirstLoad){
                    lastVisiblePost = querySnapshot.documents[querySnapshot.documents.size - 1]
                    usersList.clear()

                    for (mDocument in querySnapshot.documentChanges){
                        if (mDocument.type == DocumentChange.Type.ADDED){
                            val userId = mDocument.document.id
                            val userRef = coMeth.db.collection(USERS).document(userId)
                            userRef.get().addOnSuccessListener { it ->
                                if (it.exists()){
                                    val user = it.toObject(User::class.java)?.withId<User>(userId)
                                    addUser(user)
                                }
                            }
                        }
                    }
                }
                isFirstPageFirstLoad = false
            }else{
                Log.d(TAG, "the query for users is empty")
                coMeth.stopLoading(usersProgressBar)
            }
        }
    }

    private fun loadMoreUsers() {
        coMeth.showProgress(usersProgressBar)
        followRef.startAfter(lastVisiblePost).limit(10).get()
                .addOnSuccessListener {
                    if (!it.isEmpty){
                        if (isFirstPageFirstLoad){
                            lastVisiblePost = it.documents[it.size() - 1]
                            usersList.clear()
                            for (document in it.documentChanges){
                                if (document.type == DocumentChange.Type.ADDED){
                                    val userId = document.document.id
                                    val user = document.document.toObject(User::class.java).withId<User>(userId)
                                    addUser(user)
                                }
                            }
                        }
                    }else{
                        Log.d(TAG, "the query for users is empty")
                        coMeth.stopLoading(usersProgressBar)
                    }
                }
                .addOnFailureListener{
                    Log.e(TAG, "failed to add user ${it.message}")
                    val errorMessage = "${resources.getString(R.string.error_text)}: ${it.message}"
                    coMeth.stopLoading(usersProgressBar)
                    showSnack(errorMessage)
                }
    }

    private fun addUser(user: User?) {
        if (user != null){
            if (isFirstPageFirstLoad){
                usersList.add(0, user)
                mAdapter.notifyItemInserted(usersList.size - 1)
            }else{
                usersList.add(user)
                mAdapter.notifyItemInserted(usersList.size - 1)
            }
            coMeth.stopLoading(usersProgressBar)
        }
    }


    private fun handleToolBar() {
        setSupportActionBar(usersToolbar)
        actionBar = this.supportActionBar!!
        actionBar.setDisplayHomeAsUpEnabled(true)
        usersToolbar.setNavigationOnClickListener { finish() }
        usersToolbar.setOnClickListener { _ ->
            usersRecyclerView.smoothScrollToPosition(0)
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
                    else -> {
                        Log.d(TAG, "To Dev, $DESTINATION and $USER_ID is has issues")
                        startActivity<MainActivity>()
                        finish()
                    }
                }
            }else{
                startActivity<MainActivity>()
                finish()
            }
        }else{
            startActivity<MainActivity>()
            finish()

        }
    }

    fun showSnack(message: String){
        Snackbar.make(findViewById(R.id.usersLayout), message, Snackbar.LENGTH_LONG).show()
    }
}
