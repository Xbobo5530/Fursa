package com.nyayozangu.labs.fursa.activities.tutorial;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nyayozangu.labs.fursa.R;
import com.nyayozangu.labs.fursa.activities.main.MainActivity;
import com.nyayozangu.labs.fursa.activities.settings.PrivacyPolicyActivity;
import com.nyayozangu.labs.fursa.activities.tutorial.adapters.SlideAdapter;
import com.nyayozangu.labs.fursa.commonmethods.CoMeth;

public class TutorialActivity extends AppCompatActivity {

    private static final String TAG = "Sean";
    private CoMeth coMeth = new CoMeth();
    private ViewPager mSlideViewPager;
    private LinearLayout mDotsLayout;
    private SlideAdapter sliderAdapter;
    private Button mBackButton;
    private Button mNextButton;

    //the dots
    private TextView[] mDots;
    private int mCurrentPage;
    private ProgressDialog progressDialog;
    ViewPager.OnPageChangeListener viewListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        /**
         * overrides the onPageSelected behaviour
         * @param i the selected page
         */
        @SuppressLint("SetTextI18n")
        @Override
        public void onPageSelected(int i) {

            addDotsIndicator(i);
            mCurrentPage = i;

            if (i == 0) { //first page
                mNextButton.setEnabled(true);
                mBackButton.setEnabled(false);
                mBackButton.setVisibility(View.INVISIBLE);
                mNextButton.setText(getResources().getString(R.string.next_text));
                mBackButton.setText("");

            } else if (i == mDots.length - 1) { // last page
                mNextButton.setEnabled(true);
                mBackButton.setEnabled(true);
                mBackButton.setVisibility(View.VISIBLE);
                mNextButton.setText(R.string.finish_tut_text);
                mNextButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        //show loading
//                        showProgress(getString(R.string.loading_text));
                        //start the main activity when finish is clicked

                        SharedPreferences sharedPref =
                                TutorialActivity.this.getPreferences(Context.MODE_PRIVATE);
                        String hasAcceptedTerms = sharedPref
                                .getString(getString(R.string.has_accepted_terms),
                                        getResources().getString(R.string.has_accepted_terms));
                        Log.d(TAG, "onClick: has accepted terms " + hasAcceptedTerms);
                        if (hasAcceptedTerms.equals("true")) {
                            Log.d(TAG, "onClick: has accepted terms is true");
                            goToMain();
                        } else {
                            Log.d(TAG, "onClick: has not accepted terms");
                            handleTerms();
                        }

//                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
//                        coMeth.stopLoading(progressDialog);
//                        finish();

                    }
                });
                mBackButton.setText(getString(R.string.back_tut_text));

            } else { //middle page
                mNextButton.setEnabled(true);
                mBackButton.setEnabled(true);
                mBackButton.setVisibility(View.VISIBLE);
                mNextButton.setText(R.string.next_text);
                mBackButton.setText(R.string.back_tut_text);
                mNextButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        mSlideViewPager.setCurrentItem(mCurrentPage + 1);
                    }
                });
            }
        }
        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    /**
     * asks user to accept terms and condition of the licence agreement
     */
    private void handleTerms() {
        Log.d(TAG, "handleTerms: ");
        AlertDialog.Builder termBuilder = new AlertDialog.Builder(TutorialActivity.this);
        termBuilder.setTitle("Terms and Conditions")
                .setIcon(getResources().getDrawable(R.drawable.ic_action_book))
                .setMessage("By proceeding you accept to abide by our terms and conditions.")
                .setPositiveButton("Agree", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        SharedPreferences sharedPref =
                                TutorialActivity.this.getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString(getString(R.string.has_accepted_terms),
                                getString(R.string.true_text));
                        editor.apply();

                        goToMain();
                    }
                })
                .setNegativeButton(R.string.view_terms_text, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        goToTerms();
                    }
                })
                .setCancelable(false)
                .show();
    }

    private void goToMain() {
        startActivity(new Intent(TutorialActivity.this, MainActivity.class));
        finish();
    }

    private void goToTerms() {
        Log.d(TAG, "goToTerms: ");
        Intent goToTermsIntent =
                new Intent(TutorialActivity.this, PrivacyPolicyActivity.class);
        goToTermsIntent.putExtra("source", "tut");
        startActivity(goToTermsIntent);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);

        mSlideViewPager = findViewById(R.id.tutorial_view_pager_layout);
        mDotsLayout = findViewById(R.id.dots_layout);
        mBackButton = findViewById(R.id.tutorial_prev_button);
        mNextButton = findViewById(R.id.tutorial_next_button);

        sliderAdapter = new SlideAdapter(this);
        mSlideViewPager.setAdapter(sliderAdapter);

        addDotsIndicator(0);

        mSlideViewPager.addOnPageChangeListener(viewListener);

        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mSlideViewPager.setCurrentItem(mCurrentPage + 1);
            }
        });

        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSlideViewPager.setCurrentItem(mCurrentPage - 1);
            }
        });

    }

    public void addDotsIndicator(int position) {

        mDots = new TextView[8];
        mDotsLayout.removeAllViews();

        for (int i = 0; i < mDots.length; i++) {
            mDots[i] = new TextView(this);
            mDots[i].setText(Html.fromHtml("&#8226;"));
            mDots[i].setTextSize(35);
            mDots[i].setTextColor(getResources().getColor(R.color.colorWhiteTransparent));
            mDotsLayout.addView(mDots[i]);

        }

        if (mDots.length > 0) {
            mDots[position].setTextColor(getResources().getColor(R.color.colorWhite));
        }
    }

}
