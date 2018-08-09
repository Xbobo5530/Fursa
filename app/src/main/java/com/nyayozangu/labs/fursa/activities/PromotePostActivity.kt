package com.nyayozangu.labs.fursa.activities

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.nyayozangu.labs.fursa.R
import com.nyayozangu.labs.fursa.helpers.CoMeth.*
import kotlinx.android.synthetic.main.activity_promote_post.*

class PromotePostActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_promote_post)

        //set up the toolbar
        setSupportActionBar(promotePostToolbar)
        val actionBar = supportActionBar
        actionBar?.title = getString(R.string.promote_post_text)
        actionBar?.setDisplayHomeAsUpEnabled(true)
        promotePostToolbar.setNavigationOnClickListener { finish() }

        val postId = handleIntent()

        //set up the recycler adapter

        //load the promotions

    }
    fun handleIntent(): String? {
        if (intent != null){
            val postId = intent.getStringExtra(POST_ID)
            if (postId != null) {
                return postId
            }else{
                goToMain()
                return null
            }
        }else{
            goToMain()
            return null
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
