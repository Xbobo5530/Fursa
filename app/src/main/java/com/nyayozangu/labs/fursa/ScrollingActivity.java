package com.nyayozangu.labs.fursa;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ScrollingActivity extends AppCompatActivity {

    private static final String TAG = "Sean";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


        //test load post details
        loadPostDetails("1h ago", "South Africa, Johanesburg", "Tshs 300,000/=", "nitafute@gmail.com", null);

    }


    private void loadPostDetails(String time, String location, String price, String email, String phone) {
        //edit to make reusable
        //load the more items menu
        Log.d(TAG, "at loadPostDetails");

        // Array of strings for ListView Title
        final String[] listViewTitle = new String[]{
                time,
                location,
                price,
                email,
                phone
        };

        int[] listViewImage = new int[]{
                R.drawable.ic_action_time,
                R.drawable.ic_action_location,
                R.drawable.ic_action_price,
                R.drawable.ic_action_email,
                R.drawable.ic_action_call
        };


        List<HashMap<String, String>> aList = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            HashMap<String, String> hm = new HashMap<String, String>();
            hm.put("listView_title", listViewTitle[i]);
            hm.put("listView_image", Integer.toString(listViewImage[i]));
            aList.add(hm);
        }

        String[] from = {"listView_image", "listView_title"};
        int[] to = {R.id.postItemImageView, R.id.postItemTextView};

        SimpleAdapter simpleAdapter = new SimpleAdapter(getBaseContext(), aList, R.layout.post_detals_items, from, to);
        ListView androidListView = findViewById(R.id.postDetailsListView);
        androidListView.setAdapter(simpleAdapter);


        androidListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                //handle the item value clicks
                /*switch (listViewTitle[position]) {
                    case "Product Requests":
                        setFragment(moreFragment, getString(R.string.store_requests_url));
                        break;
                    case "Blog":
                        setFragment(moreFragment, getString(R.string.blog_url));
                        break;
                    *//*case "Chat with us":
                        openChat();
                        break;*//*
                    case "Email us":
                        setFragment(moreFragment, getString(R.string.contact_url));
                        break;
                    case "About us":
                        setFragment(moreFragment, getString(R.string.about_url));
                        break;
                    default:
                        setFragment(moreFragment, null);
                        break;
                }*/
            }
        });

        //load ad banner
//        loadAds();
    }
}
