package se.soduti.swimtimer;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Larsi on 2016-02-17.
 * State object for all Stopwatches
 * Passed between fragments and restored if activity is destroyed
 */
public class SwimState implements Serializable {
    public boolean isRunning; //Is stopwatch isRunning
    public long startTime; //Inital start time
    public long stopTime; //Keep track of pause period
    public int position; //current position in incremental stepping
    public ArrayList<Lane> Lanes; //split times
}
