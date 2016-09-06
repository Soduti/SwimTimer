package se.soduti.swimtimer;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Larsi on 2016-02-17.
 * Lane holds information if lane is isRunning and all the lap times recorded
 */
public class Lane implements Serializable {
    boolean isRunning; //Is lane isRunning
    long StartTime; //Start time for this
    long LastStartTime; //Keep track of pauses
    String LaneName; //Description Lane 1 etc
    ArrayList<Long> Laps = new ArrayList<>(); // Split times
}
