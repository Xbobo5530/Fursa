package com.nyayozangu.labs.fursa.fragments


import android.os.Bundle
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
import com.nyayozangu.labs.fursa.R
import com.nyayozangu.labs.fursa.adapters.PostsRecyclerAdapter
import com.nyayozangu.labs.fursa.helpers.CoMeth
import com.nyayozangu.labs.fursa.helpers.CoMeth.POSTS
import com.nyayozangu.labs.fursa.helpers.CoMeth.USERS
import com.nyayozangu.labs.fursa.models.Post
import com.nyayozangu.labs.fursa.models.User

private const val POPULAR_FRAGMENT = "PopularFragment"
/**
 * A simple [Fragment] subclass.
 *
 */
class RecommendedTabFragment : Fragment() {

    private var postsList: MutableList<Post> = ArrayList()
    private var usersList: MutableList<User> = ArrayList()
    private val coMeth: CoMeth = CoMeth()
    private var isFirstPageFirstLoad = true
    private lateinit var lastVisiblePost: DocumentSnapshot
    private lateinit var adapter: PostsRecyclerAdapter
    private lateinit var mProgressBar: ProgressBar

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_recommended_tab, container, false)

        val mRecyclerView = view.findViewById<RecyclerView>(R.id.popularRecyclerView)
        adapter = PostsRecyclerAdapter(postsList, usersList,
                POPULAR_FRAGMENT, Glide.with(this), activity)
        coMeth.handlePostsView(context, activity, mRecyclerView)
        mRecyclerView.adapter = adapter

        mProgressBar = view.findViewById(R.id.popularProgressBar)
        mProgressBar.visibility = View.VISIBLE

        mRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {

                super.onScrolled(recyclerView, dx, dy)
                val reachedBottom = !mRecyclerView.canScrollVertically(1)
                if (reachedBottom) {
                    loadMorePosts()
                }
            }
        })
        handleScrolling(mRecyclerView)
        val firstQuery = coMeth.db.collection(CoMeth.POSTS).limit(10)
        loadPosts(firstQuery)

        return view
    }

    private fun handleScrolling(mRecyclerView: RecyclerView) {
        mRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {

                super.onScrolled(recyclerView, dx, dy)
                val reachedBottom = !mRecyclerView.canScrollVertically(1)
                if (reachedBottom) {
                    Log.d(CoMeth.TAG, "at addOnScrollListener\n reached bottom")
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

    private fun getUserData(postUserId: String, post: Post) {
        coMeth.db.collection(USERS).document(postUserId).get()
                .addOnSuccessListener {
                    if (it.exists()) {
                        val user = it.toObject(User::class.java)?.withId<User>(postUserId)
                        if (isFirstPageFirstLoad && !postsList.contains(post)) {
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
                        mProgressBar.visibility = View.GONE
                    }
                }
                .addOnFailureListener {
                    Log.d(CoMeth.TAG, "failed to get user details ${it.message}")
                }
    }

    private fun loadMorePosts() {
        mProgressBar.visibility = View.VISIBLE
        val nextQuery = coMeth.db.collection(POSTS).startAfter(lastVisiblePost).limit(10)

        nextQuery.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            if (firebaseFirestoreException == null) {
                if (!querySnapshot?.isEmpty!!) {
                    lastVisiblePost = querySnapshot.documents[querySnapshot.size() - 1]
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
                Log.d(CoMeth.TAG, "error ${firebaseFirestoreException.message}")
            }
        }
    }
}
