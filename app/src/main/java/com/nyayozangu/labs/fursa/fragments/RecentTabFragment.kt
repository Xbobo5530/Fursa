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
import org.jetbrains.anko.doAsync
import java.util.*

private const val RECENT_FRAGMENT = "RecentFragment"
private const val TAG = "RecentFragment"


class RecentTabFragment : Fragment() {

    private var postsList: MutableList<Post> = ArrayList()
    private var usersList: MutableList<User> = ArrayList()
    private val coMeth: CoMeth = CoMeth()
    private var isFirstPageFirstLoad = true
    private lateinit var lastVisiblePost: DocumentSnapshot
    private lateinit var adapter: PostsRecyclerAdapter
    private var randomPositionForAd = 0
    private var randomPositionForSponsoredPost = 0
    private lateinit var mProgressBar: ProgressBar
    private lateinit var calToday: Calendar
    private lateinit var calExpiryDate: Calendar

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_recent_tab, container, false)

        val mRecyclerView = view.findViewById<RecyclerView>(R.id.recentRecyclerView)
        adapter = PostsRecyclerAdapter(postsList, usersList,
                RECENT_FRAGMENT, Glide.with(this), activity)
        coMeth.handlePostsView(context, activity, mRecyclerView)
        mRecyclerView.adapter = adapter
        mProgressBar = view.findViewById(R.id.recentProgressBar)
        calToday = getCalToday()
        calExpiryDate = getCalExpiryDate()
        handleScrolling(mRecyclerView)
        loadPosts()
        handleBottomNavReselect(mRecyclerView)

        return view
    }

    private fun getCalToday(): Calendar {
        return GregorianCalendar()
    }

    private fun getCalExpiryDate(): Calendar {
        val calExpiryDate = GregorianCalendar() //Calendar.getInstance()
        calExpiryDate.add(Calendar.MONTH, 1)
        return calExpiryDate
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

    private fun loadPosts() {
        coMeth.showProgress(mProgressBar)
        val firstQuery = coMeth.db.collection(POSTS)
                .orderBy(TIMESTAMP, com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(10)
        firstQuery.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            if (firebaseFirestoreException == null) {
                if (querySnapshot != null) {
                    if (!querySnapshot.isEmpty) {
                        if (isFirstPageFirstLoad) {
                            lastVisiblePost = querySnapshot.documents[querySnapshot.size() - 1]
                            postsList.clear()
                            usersList.clear()

                            randomPositionForAd = coMeth.generateRandomInt(0,9)
                            randomPositionForSponsoredPost = coMeth.generateRandomInt(0,9)
                            Log.d(TAG, "random ad position is $randomPositionForAd " +
                                    "and for post is $randomPositionForSponsoredPost")

                            for (document in querySnapshot.documentChanges) {
                                if (document.type == DocumentChange.Type.ADDED) {
                                    val postId = document.document.id
                                    val post = document.document
                                            .toObject(Post::class.java).withId<Post>(postId)
                                    val postUserId = post.user_id
                                    getUserData(postUserId, post, randomPositionForAd)
                                }
                            }
                        }
                        isFirstPageFirstLoad = false
                    }
                } else {
                    Log.e(TAG, "returned null query")
                }
            } else {
                Log.e(TAG, "Failed to load posts: ${firebaseFirestoreException.message}")
//                val errorMessage = "${resources.getString(R.string.error_text)}: ${firebaseFirestoreException.message}"
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

    private fun addSponsoredPost(){
        Log.d(TAG, "adding sponsored post\ncal today is $calToday")
        coMeth.db.collection(SPONSORED)
                .orderBy(EXPIRES_AT)
                .startAfter(calToday.time)
                .endAt(calExpiryDate.time)
//                .whereLessThanOrEqualTo(EXPIRES_AT, calToday.time)
                .get().addOnSuccessListener {
                    if (!it.isEmpty){
                        val numberOfSponsoredPosts = it.size()
                        val randomPostPosition = coMeth.generateRandomInt(0,numberOfSponsoredPosts - 1)
                        Log.d(TAG, "number of sponsored posts is $numberOfSponsoredPosts\nrandom post position is $randomPostPosition")
                        val randomPostDoc = it.documentChanges[randomPostPosition]
                        val postId = randomPostDoc.document.get(POST_ID_VAL).toString()
                        getSponsoredPost(postId)
                    }else{
                        Log.d(TAG, "live sponsored posts ais empty")
                    }
                }
                .addOnFailureListener {
                    Log.w(TAG, "failed to get sponsored post\n${it.message}", it)
                }
    }

    private fun getSponsoredPost(postId: String){
        Log.d(TAG, "at get sponsored post")
        coMeth.db.collection(POSTS).document(postId).get().addOnSuccessListener {
            if (it.exists()){
                val post = it.toObject(Post::class.java)?.withId<Post>(postId)
                if (post != null){
                    post.post_type = POST_TYPE_SPONSORED
                    getSponsoredPostUser(post)
                }
            }else {
                Log.d(TAG, "post does not exist")
            }
        }
                .addOnFailureListener {
                    Log.w(TAG, "failed to get sponsored post user ${it.message}", it)
                }
    }

    private fun getSponsoredPostUser(post: Post) {
        Log.d(TAG, "at get sponsored post user")
        val postUserId = post.user_id
        coMeth.db.collection(USERS).document(postUserId).get().addOnSuccessListener {
            if (it.exists()){
                val user = it.toObject(User::class.java)?.withId<User>(postUserId)
                if (user != null){
                    postsList.add(post)
                    usersList.add(user)
                    adapter.notifyItemInserted(postsList.size - 1)
                    coMeth.stopLoading(mProgressBar)
                }
            }
        }
    }


    private fun getUserData(postUserId: String, post: Post, randomPositionForAd: Int) {
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
                        if (randomPositionForAd == postsList.size) {
                            addAd()
                        }else if (randomPositionForSponsoredPost != randomPositionForAd &&
                                randomPositionForSponsoredPost == postsList.size){
                            addSponsoredPost()
                        }
                        mProgressBar.visibility = View.GONE
                    }
                }
                .addOnFailureListener {
                    Log.d(TAG, "failed to get user details ${it.message}")
                }
    }

    private fun loadMorePosts() {
        coMeth.showProgress(mProgressBar)
        doAsync {
            val nextQuery = coMeth.db.collection(POSTS)
                    .orderBy(TIMESTAMP, Query.Direction.DESCENDING).startAfter(lastVisiblePost)
                    .limit(10)

            nextQuery.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException == null) {
                    if (!querySnapshot?.isEmpty!!) {
                        lastVisiblePost = querySnapshot.documents[querySnapshot.size() - 1]

                        randomPositionForAd = coMeth.generateRandomInt(postsList.size, postsList.size + 9)
                        randomPositionForSponsoredPost = coMeth.generateRandomInt(postsList.size, postsList.size + 9)
                        Log.d(TAG, "random ad position is $randomPositionForAd " +
                                "and for post is $randomPositionForSponsoredPost")

                        for (document in querySnapshot.documentChanges) {
                            if (document.type == DocumentChange.Type.ADDED) {
                                val postId = document.document.id
                                val post = document.document
                                        .toObject(Post::class.java).withId<Post>(postId)
                                val postUserId = post.user_id
                                getUserData(postUserId, post, randomPositionForAd)
                            }
                        }
                    }
                } else {
                    Log.d(TAG, "error ${firebaseFirestoreException.message}")
                }
            }
        }
    }
}