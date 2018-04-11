package com.nyayozangu.labs.fursa;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.SimpleAdapter;


/**
 * A simple {@link Fragment} subclass.
 */
public class CategoriesFragment extends Fragment {

    private static final String TAG = "Sean";
    //cat texts Array
    String[] catTitle = new String[]{

            "Featured",
            "Popular",
            "UpComing",
            "Events",
            "Business",
            "Buy and sell",
            "Education",
            "Jobs",
            "Queries"

    };

    //categories images Array
    int catImages[] = {

            R.drawable.ic_action_image_placeholder,
            R.drawable.ic_thumb_person,
            R.drawable.ic_action_like_unclicked,
            R.drawable.ic_action_bookmark,
            R.drawable.ic_action_call,
            R.drawable.ic_action_email,
            R.drawable.ic_action_location,
            R.drawable.ic_action_time,
            R.drawable.ic_action_contact

    };

    private SimpleAdapter adapter;
    private GridView gridView;


    public CategoriesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_categories, container, false);


        // Each row in the list stores country name, currency and flag
        /*List<HashMap<String,String>> aList = new ArrayList<HashMap<String,String>>();

        for(int i=0;i<9;i++){
            HashMap<String, String> hm = new HashMap<String,String>();
            hm.put("title", catTitle[i]);
            hm.put("image", Integer.toString(catImages[i]) );
            aList.add(hm);
        }*/

        // Keys used in Hashmap
        /*String[] from = { "image","title"};*/

        // Ids of views in listview_layout
        /*int[] to = { R.id.catGridItemImageView, R.id.catGridItemTextView};*/

        // Instantiating an adapter to store each items
        // R.layout.listview_layout defines the layout of each item

        /*adapter = new SimpleAdapter(getContext() , aList, R.layout.cat_grid_item_layout, from, to);*/


//        Log.d(TAG, "onCreateView: at oncreate");

//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
//                R.layout.cat_grid_item_layout, catTitle);

//        Log.d(TAG, "onCreateView: adapter is " + adapter);

        // Getting a reference to gridview of MainActivity
//        gridView = container.findViewById(R.id.categoriesGridView);

//        Log.d(TAG, "onCreateView: gridview initiated");

        /*// Setting an adapter containing images to the gridview
        gridView.setAdapter(adapter);*/


        /*SimpleAdapter adapter = new SimpleAdapter(getActivity(), aList,
                R.layout.cat_grid_item_layout, new String[] { "shop_image"},new int[] { R.drawable. });

        gridView.setAdapter(adapter);*/

        // TODO: 4/12/18 handle the adapter on the categories

        return view;

    }

}
