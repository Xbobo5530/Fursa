package com.nyayozangu.labs.fursa.activities.settings;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.nyayozangu.labs.fursa.R;
import com.nyayozangu.labs.fursa.activities.categories.ViewCategoryActivity;
import com.nyayozangu.labs.fursa.activities.categories.models.Categories;
import com.nyayozangu.labs.fursa.commonmethods.CoMeth;

import java.util.ArrayList;

public class MySubscriptionsActivity extends AppCompatActivity {


    private static final String TAG = "Sean";
    private CoMeth coMeth = new CoMeth();
    private Button catsButton;
    private ArrayList<String> catSubsArray;
    private String[] catsListItems;


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

        catsButton = findViewById(R.id.subsCatButton);
        catSubsArray = new ArrayList<String>();

        catsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //check if user is logged in
                if (coMeth.isConnected() && coMeth.isLoggedIn()) {
                    //get user_id
                    String currentUserId = coMeth.getUid();
                    //get current user subs
                    String catDoc = "categories";
                    coMeth.getDb()
                            .collection("Users/" + currentUserId + "/Subscriptions/categories/Categories")
                            .addSnapshotListener(MySubscriptionsActivity.this, new EventListener<QuerySnapshot>() {
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

                                        Log.d(TAG, "onClick: \ncatsListItems: " + catsListItems);
                                        catsListItems = catSubsArray.toArray((new String[catSubsArray.size()]));

                                        //open an an alert dialog for the subd cats
                                        AlertDialog.Builder catsSubBuilder = new AlertDialog.Builder(MySubscriptionsActivity.this);
                                        catsSubBuilder.setTitle(getString(R.string.categories_text))
                                                .setIcon(R.drawable.ic_action_categories)
                                                .setItems(catsListItems, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {

                                                        //open the view category activity
                                                        openCat(coMeth.getCatKey(catsListItems[which]));

                                                    }
                                                })
                                                .show();
                                        //empty the catSubsArray
                                        catSubsArray.clear();

                                    }

                                }
                            });
                } else {

                    if (!coMeth.isConnected()) {

                        showSnack(getString(R.string.failed_to_connect_text));

                    } else if (!coMeth.isLoggedIn()) {

                        showLoginAlertDialog(getString(R.string.login_to_view_subs_text));

                    }
                }

            }
        });


    }

    public void showLoginAlertDialog(String message) {
        //Prompt user to log in
        android.support.v7.app.AlertDialog.Builder loginAlertBuilder = new android.support.v7.app.AlertDialog.Builder(this);
        loginAlertBuilder.setTitle("Login")
                .setIcon(getApplicationContext().getDrawable(R.drawable.ic_action_alert))
                .setMessage("You are not logged in\n" + message)
                .setPositiveButton("Login", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //send user to login activity
                        goToLogin();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //cancel
                        dialog.cancel();
                    }
                })
                .show();
    }

    private void goToLogin() {

        Intent loginIntent = new Intent(MySubscriptionsActivity.this, LoginActivity.class);
        loginIntent.putExtra("source", "MySub");
        startActivity(loginIntent);

    }

    private void showSnack(String message) {
        Snackbar.make(findViewById(R.id.mysubs_layout),
                message, Snackbar.LENGTH_SHORT).show();
    }

    private void openCat(String catKey) {
        Intent openCatIntent = new Intent(MySubscriptionsActivity.this, ViewCategoryActivity.class);
        openCatIntent.putExtra("category", catKey);
        startActivity(openCatIntent);
    }
}
