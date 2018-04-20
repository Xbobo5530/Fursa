package com.nyayozangu.labs.fursa;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MySubscriptionsActivity extends AppCompatActivity {


    private static final String TAG = "Sean";
    //firebase auth
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private ArrayList catSubsArray;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_subscriptions);

        Toolbar toolbar = findViewById(R.id.subsToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("My Subscriptions");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        //initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        //initialize firebase storage
        // Access a Cloud Firestore instance from your Activity
        db = FirebaseFirestore.getInstance();

        //get userId
        String currentUserId = mAuth.getCurrentUser().getUid();

        catSubsArray = new ArrayList<>();

        //get current user subs
        String catDoc = "categories";
        db.collection("Users/" + currentUserId + "/Subscriptions/categories/Categories").addSnapshotListener(MySubscriptionsActivity.this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {

                //check if query is empty
                if (!queryDocumentSnapshots.isEmpty()) {

                    //user has cats
                    for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {

                        if (doc.getType() == DocumentChange.Type.ADDED) {


                            Categories cat = doc.getDocument().toObject(Categories.class);

                            //add cat to list
                            catSubsArray.add(cat.getValue());

                        }

                    }

                    Log.d(TAG, "onEvent: \ncatSubArray contains: " + catSubsArray);

                    //create a simple adapter

                    List<HashMap<String, String>> aList = new ArrayList<>();

                    Log.d(TAG, "onCreate: at hashMap, catSubsArray contains " + catSubsArray);

                    for (int i = 0; i < catSubsArray.size(); i++) {
                        HashMap<String, String> hm = new HashMap<>();
                        hm.put("listView_title", (String) catSubsArray.get(i));
                        hm.put("unSubIcon", String.valueOf(R.drawable.ic_action_close));
                        aList.add(hm);

                    }

                    Log.d(TAG, "onCreate: aList is " + aList);

                    String[] from = {"listView_title", "unSubIcon"};
                    int[] to = {R.id.subListItemTextView, R.id.unsubImageButton};

                    SimpleAdapter simpleAdapter = new SimpleAdapter(getBaseContext(), aList, R.layout.subs_list_item, from, to);
                    ListView subsListView = findViewById(R.id.subsListView);
                    subsListView.setAdapter(simpleAdapter);

                    subsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                            Toast.makeText(MySubscriptionsActivity.this, position, Toast.LENGTH_SHORT).show();

                        }
                    });

                }

            }
        });


    }
}
