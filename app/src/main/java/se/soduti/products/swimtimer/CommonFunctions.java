package se.soduti.products.swimtimer;

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

/**
 * Created by Larsi on 2016-02-22.
 * Project wide functions
 */
class CommonFunctions {
    private static final String LOG_TAG = "SwimTimerLog";

    public static void writeFile(String fileName, String contents) throws IOException {

        if (!isExternalStorageWritable()) {
            Log.v(LOG_TAG, "External media not available");
            return;
        }
        File file = getDocumentStorageDir(fileName);
        FileWriter fw = new FileWriter(file, false);

        fw.write(contents);
        fw.flush();
        fw.close();
        System.out.println("write to file done!");
    }

    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }
    public static File getDocumentStorageDir(String fileName) {
        // Get the directory for the user's public document directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS), fileName);
        if (!file.mkdirs()) {
            Log.e(LOG_TAG, "Directory not created");
        }
        return file;
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

    public static String DisplayTimerStringCondensed(long millis) {
        int hours = (int) (millis / 3600000);
        int mins = (int) ((millis - hours * 3600000) / 60000);
        int secs = (int) ((millis - hours * 3600000 - mins * 60000) / 1000);
        int remainingMillis = (int) (millis - hours * 3600000 - mins * 60000 - secs * 1000);
        if (hours > 0)
            return String.format("%d:%02d:%02d", hours, mins, secs);
         else if (mins > 0)
            return String.format("%d:%02d.%d", mins, secs, remainingMillis / 100);
        else
            return String.format("%02d.%2d", secs, remainingMillis / 100); //only seconds to show


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
