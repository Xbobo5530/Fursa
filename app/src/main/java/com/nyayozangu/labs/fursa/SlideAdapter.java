package com.nyayozangu.labs.fursa;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Sean on 4/11/18.
 */

public class SlideAdapter extends PagerAdapter {

    /**
     * the content of the sliderView
     */
    //Arrays
    public int[] slide_images = {
            R.drawable.appic,
            R.drawable.share,
            R.drawable.party,
            R.drawable.places,
            R.drawable.school,
            R.drawable.jobs,
            R.drawable.services,
            R.drawable.help
    };
    public String[] slide_headings = {
            "FURSA",
            "SHARING",
            "EVENTS",
            "PLACES",
            "EDUCATION",
            "JOBS",
            "SERVICES",
            "GET HELP"
    };
    public String[] slide_descriptions = {
            "Welcome to Fursa, \nA place to share experiences and opportunities.",

            "We all have something to share,\nsee what opportunities everyone is sharing\nand be apart of the conversation.",

            "There's always something happening \nright around the corner,\nCheck out the hottest events near you.",

            "New venues are opened everyday, \nTake a look at all the amazing places around you and expand your horizon.",

            "Stay up to date on the latest \nScholarships, exchange programs, and courses offered around the world,\ndon't miss out on a chance \nto make your dream come true.",

            "Companies are looking for people with skills like yours,\nfind out who is hiring.",

            "There's a ton of service providers out there,\ntake a look at the listings \nto get the service you need.",

            "Get help with whatever you are looking for from the growing Fursa community."

    };
    Context context;
    LayoutInflater layoutInflater;

    public SlideAdapter(Context context) {

        this.context = context;
    }

    @Override
    public int getCount() {
        return slide_headings.length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        try {
            layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert layoutInflater != null;
            View view = layoutInflater.inflate(R.layout.slide_layout, container, false);

            ImageView slideImageView = view.findViewById(R.id.tutortialImageView);
            TextView slideHeading = view.findViewById(R.id.tutorialHeadingTextView);
            TextView slideDescriptions = view.findViewById(R.id.tutorialDescriptionTextView);

            slideImageView.setImageResource(slide_images[position]);
            slideHeading.setText(slide_headings[position]);
            slideDescriptions.setText(slide_descriptions[position]);

            container.addView(view);

            return view;
        } catch (NullPointerException e) {
            Log.i("Sean", "Error on inflating, error is " + e.getMessage());
            return null;
        }
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {

        container.removeView((ConstraintLayout) object);

    }


}
