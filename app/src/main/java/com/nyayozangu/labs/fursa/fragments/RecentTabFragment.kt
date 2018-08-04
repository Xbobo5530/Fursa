package com.nyayozangu.labs.fursa.fragments


import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import com.bumptech.glide.Glide
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.nyayozangu.labs.fursa.R
import com.nyayozangu.labs.fursa.adapters.PostsRecyclerAdapter
import com.nyayozangu.labs.fursa.helpers.CoMeth
import com.nyayozangu.labs.fursa.helpers.CoMeth.*
import com.nyayozangu.labs.fursa.models.Post
import com.nyayozangu.labs.fursa.models.User

private const val RECENT_FRAGMENT = "RecentFragment"

/**
 * A simple [Fragment] subclass.
 *
 */
class RecentTabFragment : Fragment() {

    private var postsList: MutableList<Post> = ArrayList()
    private var usersList: MutableList<User> = ArrayList()
    private val coMeth: CoMeth = CoMeth()
    private var isFirstPageFirstLoad = true
    private lateinit var lastVisiblePost: DocumentSnapshot
    private lateinit var adapter: PostsRecyclerAdapter
    private lateinit var mProgressBar: ProgressBar
    private var randomPosition = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_recent_tab, container, false)

        val mRecyclerView = view.findViewById<RecyclerView>(R.id.recentRecyclerView)
        adapter = PostsRecyclerAdapter(postsList, usersList,
                RECENT_FRAGMENT, Glide.with(this), activity)
        coMeth.handlePostsView(context, activity, mRecyclerView)
        mRecyclerView.adapter = adapter

        mProgressBar = view.findViewById(R.id.recentProgressBar)
        mProgressBar.visibility = View.VISIBLE
        handleScrolling(mRecyclerView)
        val firstQuery = coMeth.db.collection(POSTS)
                .orderBy(TIMESTAMP, com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(10)
        loadPosts(firstQuery)
        handleBottomNavReselect(mRecyclerView)

        return view
    }

    private fun handleBottomNavReselect(mRecyclerView: RecyclerView) {
        val mBottomNav = activity?.findViewById<BottomNavigationView>(R.id.mainBottomNav)
        mBottomNav?.setOnNavigationItemReselectedListener {
            mRecyclerView.smoothScrollToPosition(0)
        }
    }

    private fun handleScrolling(mRecyclerView: RecyclerView) {
        mRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val reachedBottom = !mRecyclerView.canScrollVertically(1)
                if (reachedBottom) {
                    loadMorePosts()
                }
            }
        })
    }

    private fun loadPosts(firstQuery: com.google.firebase.firestore.Query) {
        firstQuery.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            if (firebaseFirestoreException == null) {
                if (!querySnapshot?.isEmpty!!) {
                    if (isFirstPageFirstLoad) {
                        lastVisiblePost = querySnapshot.documents[querySnapshot.size() - 1]
                        postsList.clear()
                        usersList.clear()

                        randomPosition = coMeth.generateRandomInt()
                        Log.d(TAG, "random ad position is $randomPosition")

                        for (document in querySnapshot.documentChanges) {
                            if (document.type == DocumentChange.Type.ADDED) {
                                val postId = document.document.id
                                val post = document.document
                                        .toObject(Post::class.java).withId<Post>(postId)
                                val postUserId = post.user_id
                                getUserData(postUserId, post)
                            }
                        }
                    }
                    isFirstPageFirstLoad = false
                }
            }
        }
    }

    private fun addAd() {
        val post = Post()
        post.post_type = POST_TYPE_AD
        postsList.add(post)
        usersList.add(User())
        adapter.notifyItemInserted(postsList.size - 1)
        Log.d(TAG, "ad post is added")
    }

    private fun getUserData(postUserId: String, post: Post) {
        coMeth.db.collection(USERS).document(postUserId).get()
                .addOnSuccessListener {
                    if (it.exists()) {
                        val user = it.toObject(User::class.java)?.withId<User>(postUserId)
                        if (isFirstPageFirstLoad) {
                            if (user != null) {
                                usersList.add(0, user)
                                postsList.add(0, post)
                                adapter.notifyItemInserted(postsList.size - 1)
                            }
                        } else {
                            if (user != null) {
                                usersList.add(user)
                                postsList.add(post)
                                adapter.notifyItemInserted(postsList.size - 1)
                            }
                        }
                        if (randomPosition == postsList.size) {
                            addAd()
                        }
                        mProgressBar.visibility = View.GONE
                    }
                }
                .addOnFailureListener {
                    Log.d(TAG, "failed to get user details ${it.message}")
                }
    }

    private fun loadMorePosts() {
        mProgressBar.visibility = View.VISIBLE
        val nextQuery = coMeth.db.collection(POSTS)
                .orderBy(TIMESTAMP, Query.Direction.DESCENDING).startAfter(lastVisiblePost)
                .limit(10)

        nextQuery.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            if (firebaseFirestoreException == null) {
                if (!querySnapshot?.isEmpty!!) {
                    lastVisiblePost = querySnapshot.documents[querySnapshot.size() - 1]
                    randomPosition = coMeth.generateRandomInt()

                    for (document in querySnapshot.documentChanges) {
                        if (document.type == DocumentChange.Type.ADDED) {
                            val postId = document.document.id
                            val post = document.document
                                    .toObject(Post::class.java).withId<Post>(postId)
                            val postUserId = post.user_id
                            getUserData(postUserId, post)
                        }
                    }
                }
            } else {
                Log.d(TAG, "error ${firebaseFirestoreException.message}")
            }
        }
    }
}