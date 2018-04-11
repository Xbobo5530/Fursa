package com.nyayozangu.labs.fursa;

import android.app.Activity;
import android.app.Fragment;
import android.support.v4.app.FragmentActivity;

/**
 * Created by Sean on 4/11/18.
 */

public class BaseFragment extends Fragment {


    protected FragmentActivity mActivity;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (FragmentActivity) activity;
    }

}
