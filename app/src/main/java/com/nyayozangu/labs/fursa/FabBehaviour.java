package com.nyayozangu.labs.fursa;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class FabBehaviour extends CoordinatorLayout.Behavior<FloatingActionButton> {

    private static final String TAG = "ScrollingFABBehavior";
    Handler mHandler;

    public FabBehaviour(Context context, AttributeSet attrs) {
        super();
    }

    public FabBehaviour() {
        super();
    }

//    @Override
//    public void onStopNestedScroll(@NonNull CoordinatorLayout coordinatorLayout,
//                                   @NonNull final FloatingActionButton child,
//                                   @NonNull View target,
//                                   int type) {
//        super.onStopNestedScroll(coordinatorLayout, child, target, type);
//        if (mHandler == null)
//            mHandler = new Handler();
//
//
//        mHandler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                child.animate().translationY(0).setInterpolator(new LinearInterpolator()).start();
//                Log.d("FabAnim", "startHandler()");
//            }
//        }, 1000);
//    }

    @Override
    public void onNestedScroll(@NonNull CoordinatorLayout coordinatorLayout,
                               @NonNull FloatingActionButton child,
                               @NonNull View target,
                               int dxConsumed,
                               int dyConsumed,
                               int dxUnconsumed,
                               int dyUnconsumed,
                               int type) {
        super.onNestedScroll(coordinatorLayout,
                child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type);

        child.hide(new FloatingActionButton.OnVisibilityChangedListener() {
            /**
             * Called when a FloatingActionButton has been hidden
             *
             * @param fab the FloatingActionButton that was hidden.
             */
            @Override
            public void onHidden(FloatingActionButton fab) {
                super.onShown(fab);
                fab.setVisibility(View.INVISIBLE);
            }
        });

        if (dyConsumed > 0) {
            Log.d("Scrolling", "Up");
            child.hide();
//            CoordinatorLayout.LayoutParams layoutParams =
//                    (CoordinatorLayout.LayoutParams) child.getLayoutParams();
//            int fab_bottomMargin = layoutParams.bottomMargin;
//            child.animate().translationY(child.getHeight() +
//                    fab_bottomMargin).setInterpolator(new LinearInterpolator()).start();
        } else if (dyConsumed < 0) {
            Log.d("Scrolling", "down");
            child.show();
//            child.animate().translationY(0).setInterpolator(new LinearInterpolator()).start();
        }
    }

    @Override
    public boolean onStartNestedScroll(@NonNull CoordinatorLayout coordinatorLayout,
                                       @NonNull FloatingActionButton child,
                                       @NonNull View directTargetChild,
                                       @NonNull View target,
                                       int axes,
                                       int type) {
        if (mHandler != null) {
            mHandler.removeMessages(0);
            Log.d("Scrolling", "stopHandler()");
        }
        return axes == ViewCompat.SCROLL_AXIS_VERTICAL;
    }


}
