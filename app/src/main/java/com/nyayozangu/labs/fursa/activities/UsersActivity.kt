package com.nyayozangu.labs.fursa.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.nyayozangu.labs.fursa.R
import kotlinx.android.synthetic.main.activity_users.*
import kotlinx.android.synthetic.main.content_users.*

class UsersActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_users)
        setSupportActionBar(usersToolbar)
        val actionBar = supportActionBar
        usersToolbar.setOnClickListener { it ->
            usersRecyclerView.smoothScrollToPosition(0)
        }

    }
}
