package com.citaurus.gmspps;

import android.app.Application;
import android.content.Context;

/**
 * Created by rzwisler on 06.10.2015.
 */
public class Doit4You extends Application {

    private static Context context;

    public void onCreate(){
        super.onCreate();
        Doit4You.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return Doit4You.context;
    }
}
