package com.nyayozangu.labs.fursa;

import android.app.Application;
import android.content.Context;

import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

/**
 * Created by Sean on 5/9/18.
 */

public class Fursa extends Application {

    private RefWatcher refWatcher;

    public static RefWatcher getRefWatcher(Context context) {

        Fursa application = (Fursa) context.getApplicationContext();
        return application.refWatcher;

    }


    @Override
    public void onCreate() {
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        refWatcher = LeakCanary.install(this);
        // Normal app init code...
    }

}
