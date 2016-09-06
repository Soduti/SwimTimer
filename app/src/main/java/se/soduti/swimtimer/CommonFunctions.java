package se.soduti.swimtimer;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Larsi on 2016-02-22.
 * Project wide functions
 */
class CommonFunctions {
    private static final String LOG_TAG = "SwimTimerLog";
    private static final int MAINLANE = 0;
    private static final boolean verbose = true;

    public static void writeSysOut(String output) {
        if (verbose)
            System.out.println(output);
    }
    public static void writeFile(String contents) throws IOException {

        //Formatting a date needs a timezone - otherwise the date get formatted to your system time zone.
        TimeZone timezone = TimeZone.getDefault();
        DateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        formatter.setTimeZone(timezone);
        Date now = new Date();
        String fileName = String.format("Swimtimer_%s.csv", formatter.format(now));

        if (!isExternalStorageWritable()) {
            Log.v(LOG_TAG, "External media not available");
            return;
        }
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        if (!path.isDirectory()) {
            Log.e(LOG_TAG, "Directory does not exist");
            return;
        }
        File file = new File(path.getPath(), fileName);
        FileWriter fw = new FileWriter(file, false);

        fw.write(contents);
        fw.flush();
        fw.close();
        writeSysOut(String.format("File:%s written to documents!", fileName));
    }


    public static String createTextResult(SwimState swimState) {
        StringBuilder sb = new StringBuilder(); // Final string
        StringBuilder sbWork; // Part of text

        TimeZone timezone = TimeZone.getDefault();

        //Formatting a date needs a timezone - otherwise the date get formatted to your system time zone.
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        formatter.setTimeZone(timezone);
        Date now = new Date();
        sb.append(String.format("Swimtimer %s\n\n", formatter.format(now)));

        int highest_count = 0;
        for (int laneIdx = 0; laneIdx < swimState.Lanes.size(); laneIdx++)
            if (swimState.Lanes.get(laneIdx).Laps.size() > highest_count)
                highest_count = swimState.Lanes.get(laneIdx).Laps.size();

        sb.append("Lap,");
        sbWork = new StringBuilder();
        //Start from lane 1 (skip mainlane)
        for (int laneIdx = MAINLANE + 1; laneIdx < swimState.Lanes.size(); laneIdx++) {
            sbWork.append(String.format("%s, ", swimState.Lanes.get(laneIdx).LaneName));
        }
        sb.append(sbWork.toString().substring(0, sbWork.length() - 2));
        sb.append("\n");

        for (int lapIdx = 0; lapIdx < highest_count; lapIdx++) {
            sbWork = new StringBuilder();
            for (int laneIdx = MAINLANE; laneIdx < swimState.Lanes.size(); laneIdx++) {
                if (laneIdx == MAINLANE)
                    sbWork.append(String.format("%d, ", lapIdx+1));
                else {
                    if (lapIdx < swimState.Lanes.get(laneIdx).Laps.size()) {
                        sbWork.append(String.format("%s, ", calculateLaptime(swimState.Lanes.get(laneIdx), lapIdx)));
                    } else {
                        sbWork.append(", ");
                    }
                }
            }
            sb.append(sbWork.toString().substring(0, sbWork.length()- 2));
            sb.append("\n");
        }
        sb.append("Total, ");
        sbWork = new StringBuilder();
        for (int laneIdx = MAINLANE + 1; laneIdx < swimState.Lanes.size(); laneIdx++) {
            sbWork.append(String.format("%s, ", DisplayTimerStringHundreds(getTotalTime(swimState.Lanes.get(laneIdx)))));
        }

        sb.append(sbWork.toString().substring(0, sbWork.length()- 2));
        sb.append("\n");
        return sb.toString();
    }
    public static long getTotalTime(Lane lane) {
        long sum = 0;
        if (lane.Laps.size() > 0) {
            for (int lapIdx = 0; lapIdx < lane.Laps.size(); lapIdx++) {
                sum += lane.Laps.get(lapIdx);
            }
            return sum;
        }
        else
            return 0;
    }
    public static String calculateLaptime(Lane lane, int lapIdx) {
        String newString;
        if (lane.Laps.size() == 0)
            return "No laps";
        else {
            //code for calculating last laptime
            long lapTime;
//            if (lane.Laps.size() == 1 || lapIdx == 0)
//                lapTime = lane.Laps.get(lapIdx) - lane.StartTime;
//            else
                lapTime = lane.Laps.get(lapIdx); // - lane.Laps.get(lapIdx - 1);

            newString = DisplayTimerStringHundreds(lapTime);
        }
        return newString;
    }

    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /**
     * Format a long (milliseconds) value to "00:00.0" or "0:00:00" if time over 1 hour
     *
     * @param millis milliseconds
     * @return string in format "00:00.0" or "0:00:00" if time over 1 hour
     */
    public static String DisplayTimerString(long millis) {
        int hours = (int) (millis / 3600000);
        int mins = (int) ((millis - hours * 3600000) / 60000);
        int secs = (int) ((millis - hours * 3600000 - mins * 60000) / 1000);
        int remainingMillis = (int) (millis - hours * 3600000 - mins * 60000 - secs * 1000);
        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, mins, secs);
        } else {
            return String.format("%02d:%02d.%d", mins, secs, remainingMillis / 100);
        }
    }

    public static String DisplayTimerStringHundreds(long millis) {
        int hours = (int) (millis / 3600000);
        int mins = (int) ((millis - hours * 3600000) / 60000);
        int secs = (int) ((millis - hours * 3600000 - mins * 60000) / 1000);
        int remainingMillis = (int) (millis - hours * 3600000 - mins * 60000 - secs * 1000);
        if (hours > 0) {
            return String.format("%d:%02d:%02d.%02d", hours, mins, secs, remainingMillis / 10);
        } else {
            return String.format("%d:%02d.%02d", mins, secs, remainingMillis / 10);
        }
    }

    public static String DisplayTimerStringHundredsCondensed(long millis) {
        int hours = (int) (millis / 3600000);
        int mins = (int) ((millis - hours * 3600000) / 60000);
        int secs = (int) ((millis - hours * 3600000 - mins * 60000) / 1000);
        int remainingMillis = (int) (millis - hours * 3600000 - mins * 60000 - secs * 1000);
        if (hours > 0)
            return String.format("%d:%02d:%02d", hours, mins, secs);
         else if (mins > 0)
            return String.format("%02d:%02d.%02d", mins, secs, remainingMillis / 100);
        else
            return String.format("%02d.%02d", secs, remainingMillis / 100); //only seconds to show


        /*if (mins > 9) {
            return String.format("%02d:%02d.%d", mins, secs, remainingMillis / 100);
        } else if (mins > 0) {
            return String.format("%d:%02d.%2d", mins, secs, remainingMillis / 100);
        } else
            return String.format("%02d.%2d", mins, secs, remainingMillis / 10);*/
    }

    public static int dpToPx(int dp)
    {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int pxToDp(int px)
    {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }

    public static int getScreenOrientation(Activity activity) {
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        int orientation;
        // if the device's natural orientation is portrait:
        if ((rotation == Surface.ROTATION_0
                || rotation == Surface.ROTATION_180) && height > width ||
                (rotation == Surface.ROTATION_90
                        || rotation == Surface.ROTATION_270) && width > height) {
            switch(rotation) {
                case Surface.ROTATION_0:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    break;
                case Surface.ROTATION_90:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    break;
                case Surface.ROTATION_180:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                    break;
                case Surface.ROTATION_270:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                    break;
                default:
                    // TODO: 2016-03-28 implement log functionality
//                    Log.e(TAG, "Unknown screen orientation. Defaulting to " +
//                            "portrait.");
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    break;
            }
        }
        // if the device's natural orientation is landscape or if the device
        // is square:
        else {
            switch(rotation) {
                case Surface.ROTATION_0:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    break;
                case Surface.ROTATION_90:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    break;
                case Surface.ROTATION_180:
                    orientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                    break;
                case Surface.ROTATION_270:
                    orientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                    break;
                default:
                    // TODO: 2016-03-28 implement log functionality
//                    Log.e(TAG, "Unknown screen orientation. Defaulting to " +
//                            "landscape.");
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    break;
            }
        }

        return orientation;
    }

}
