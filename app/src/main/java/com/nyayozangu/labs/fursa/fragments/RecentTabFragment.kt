package com.nyayozangu.labs.fursa.fragments


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import com.bumptech.glide.Glide
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentSnapshot
import com.nyayozangu.labs.fursa.R
import com.nyayozangu.labs.fursa.adapters.PostsRecyclerAdapter
import com.nyayozangu.labs.fursa.helpers.CoMeth
import com.nyayozangu.labs.fursa.helpers.CoMeth.*
import com.nyayozangu.labs.fursa.models.Posts
import com.nyayozangu.labs.fursa.models.Users

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 *
 */
class RecentTabFragment : Fragment() {

    var postsList: MutableList<Posts> = ArrayList()
    var usersList: MutableList<Users> = ArrayList()
    val coMeth: CoMeth = CoMeth()
    var isFirstPageFirstLoad = true
    lateinit var lastVisiblePost: DocumentSnapshot
    lateinit var adapter: PostsRecyclerAdapter
    lateinit var mSwipeRefreshLayout: SwipeRefreshLayout
    lateinit var mProgressBar: ProgressBar
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_recent_tab, container, false)

        val mRecyclerView = view.findViewById<RecyclerView>(R.id.recentRecyclerView)
        adapter = PostsRecyclerAdapter(postsList, usersList,
                "RecentFragment", Glide.with(this))
        coMeth.handlePostsView(context, activity, mRecyclerView)
        mRecyclerView.adapter = adapter

        mProgressBar = view.findViewById<ProgressBar>(R.id.recentProgressBar)
        mProgressBar.visibility = View.VISIBLE

        //listen to scrolling
        mRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {

                super.onScrolled(recyclerView, dx, dy)
                val reachedBottom = !mRecyclerView.canScrollVertically(1)
                if (reachedBottom) {
                    Log.d(TAG, "at addOnScrollListener\n reached bottom")
                    loadMorePosts()
                }
            }
        })

        val firstQuery = coMeth.db.collection(POSTS)
                .orderBy(CoMeth.TIMESTAMP, com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(10)
        loadPosts(firstQuery)

        mSwipeRefreshLayout = view.findViewById<SwipeRefreshLayout>(R.id.recentSwipeRefresh)
        mSwipeRefreshLayout.setOnRefreshListener {
            mRecyclerView.recycledViewPool.clear()
            postsList.clear()
            usersList.clear()
            loadPosts(firstQuery)
        }

        return view
    }

    private fun loadPosts(firstQuery: com.google.firebase.firestore.Query) {
        firstQuery.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            if (firebaseFirestoreException == null) {
                if (!querySnapshot?.isEmpty!!) {
                    if (isFirstPageFirstLoad) {
                        lastVisiblePost = querySnapshot.documents[querySnapshot.size() - 1]
                        postsList.clear()
                        usersList.clear()

                        for (document in querySnapshot.documentChanges) {
                            if (document.type == DocumentChange.Type.ADDED) {
                                val postId = document.document.id
                                val post = document.document
                                        .toObject(Posts::class.java).withId<Posts>(postId)
                                val postUserId = post.user_id
                                //get user data
                                getUserData(postUserId, post)
                            }
                        }
                    }
                    isFirstPageFirstLoad = false
                }
            }
        }
    }

    private fun getUserData(postUserId: String, post: Posts) {
        coMeth.db.collection(USERS).document(postUserId).get()
                .addOnSuccessListener {
                    if (it.exists()) {
                        val user = it.toObject(Users::class.java)?.withId<Users>(postUserId)
                        if (isFirstPageFirstLoad) {
                            if (user != null) {
                                usersList.add(0, user)
                                postsList.add(0, post)
                                adapter.notifyDataSetChanged()
                            }
                        } else {
                            if (user != null) {
                                usersList.add(user)
                                postsList.add(post)
                                adapter.notifyDataSetChanged()
                            }
                        }
                        mProgressBar.visibility = View.GONE
                        coMeth.stopLoading(mSwipeRefreshLayout)
                    }
                }
                .addOnFailureListener {
                    Log.d(TAG, "failed to get user details ${it.message}")
                }
    }

    private fun loadMorePosts() {
        Log.d(TAG, "at load more posts")
        mProgressBar.visibility = View.VISIBLE
        val nextQuery = coMeth.db.collection(POSTS)
                .orderBy(TIMESTAMP, com.google.firebase.firestore.Query.Direction.DESCENDING)
                .startAfter(lastVisiblePost)
                .limit(10)

        nextQuery.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            if (firebaseFirestoreException == null) {
                if (!querySnapshot?.isEmpty!!) {
                    lastVisiblePost = querySnapshot.documents[querySnapshot.size() - 1]
                    for (document in querySnapshot.documentChanges) {
                        if (document.type == DocumentChange.Type.ADDED) {
                            val postId = document.document.id
                            val post = document.document
                                    .toObject(Posts::class.java).withId<Posts>(postId)
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