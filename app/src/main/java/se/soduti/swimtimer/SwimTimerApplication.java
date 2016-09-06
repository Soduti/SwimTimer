package se.soduti.swimtimer;

import android.app.Application;

/**
 * Created by Larsi on 2016-02-22.
 * Application wide variables
 */
public class SwimTimerApplication extends Application {
    SwimState _swimState;

    @Override
    public void onCreate() {
        super.onCreate();
        // TODO Put your application initialization code here.
    }
    public void setSwimState(SwimState swimState) {
        _swimState = swimState;
    }
    public SwimState getSwimState() {
        return _swimState;
    }
}