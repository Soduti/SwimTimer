package se.soduti.products.swimtimer;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Larsi on 2016-02-17.
 * State object for all Stopwatches
 * Passed between fragments and restored if activity is destroyed
 */
public class SwimState implements Serializable {
    public boolean running;
    public long startTime;
    public ArrayList<Lane> Lanes;
}
