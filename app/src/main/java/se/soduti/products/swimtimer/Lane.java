package se.soduti.products.swimtimer;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Larsi on 2016-02-17.
 * Lane holds information if lane is running and all the lap times recorded
 */
public class Lane implements Serializable {
    boolean Running;
    long StartTime;
    String LaneName;
    ArrayList<Long> Laps = new ArrayList<>();
}
