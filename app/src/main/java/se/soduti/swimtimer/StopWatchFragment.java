package se.soduti.swimtimer;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;

import static se.soduti.swimtimer.CommonFunctions.DisplayTimerString;
import static se.soduti.swimtimer.CommonFunctions.dpToPx;
import static se.soduti.swimtimer.CommonFunctions.getScreenOrientation;
import static se.soduti.swimtimer.CommonFunctions.writeSysOut;
import static se.soduti.swimtimer.CommonFunctions.createTextResult;
import static se.soduti.swimtimer.CommonFunctions.getTotalTime;

/**
 * Created by Larsi on 2016-02-17.
 * Handle stopwatch counters
 */
public class StopWatchFragment extends Fragment implements IFragment {
    private static final String KEY_SWIMSTATE = "swimstate";
    private static final String KEY_STOPTIME = "stoptime";
    private static final int MAINLANE = 0;
    private static final int LANE1 = 1;
    private static final int POSITION_LANE_NUMBER = 0;
    private static final int POSITION_LANE_NAME = 1;
    private static final int POSITION_TEXTVIEW = 2;
    private static final int POSITION_BUTTON_START = 3;
    private static final int POSITION_BUTTON_RESET = 4;
    private static final int POSITION_BUTTON_STOP = 5;
    private static final int MODE_INCREMENT = 0;
    private static final int MODE_INDIVIDUAL = 1;


    public StopWatchFragment() {
    }

    SwimState _swimState;
    ArrayList<String> _laneNames;
    View _rootView;
    Handler _timerHandler = new Handler();
    int _mode = MODE_INDIVIDUAL;
    long _stopTime;

    /**
     * Update timers with formatted strings 00:00.0
     */
    Runnable timerStopWatchUpdater = new Runnable() {

        @Override
        public void run() {

            LinearLayout llSW = (LinearLayout) _rootView.findViewById(R.id.llSW);
            for (int llIdx = MAINLANE; llIdx < ((MainActivity) getActivity()).getNumberOfLanes(); llIdx++) {
                LinearLayout ll = (LinearLayout) llSW.getChildAt(llIdx);
                EditText et = (EditText) ll.getChildAt(POSITION_TEXTVIEW);
                int laneNumber = (int) et.getTag();
                if (_swimState.Lanes.get(laneNumber).isRunning)
                    et.setText(DisplayTimerString(getElapsedTime(laneNumber)));
            }

            _timerHandler.postDelayed(this, 100);
        }
    };
    /**
     *
     */
    Runnable timerRunnableShowTotals = new Runnable() {

        @Override
        public void run() {
            // Show total time for all lanes if they are stopped
            LinearLayout llSW = (LinearLayout) _rootView.findViewById(R.id.llSW);
            for (int laneIdx = MAINLANE; laneIdx < ((MainActivity) getActivity()).getNumberOfLanes(); laneIdx++) {
                LinearLayout ll = (LinearLayout) llSW.getChildAt(laneIdx);
                EditText et = (EditText) ll.getChildAt(POSITION_TEXTVIEW);
                Lane lane = _swimState.Lanes.get(laneIdx);
                if (!lane.isRunning) {
                    if (laneIdx == MAINLANE) {
                        et.setText(DisplayTimerString(System.currentTimeMillis() - lane.StartTime));
                        _swimState.isRunning = false;
                        _swimState.startTime = 0;
                    } else {
                        et.setText(DisplayTimerString(getTotalTime(lane)));
                    }
                }
            }
        }
    };

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        writeSysOut("===== " + this.getClass().getSimpleName() + "." + new Object() {
        }.getClass().getEnclosingMethod().getName() + " =====  onSAVEinstanceSTATE Stopwatch *****");
        // Save the SwimState
        savedInstanceState.putSerializable(KEY_SWIMSTATE, _swimState);
        // Always call the superclass so it can save the view hierarchy _swimState
        super.onSaveInstanceState(savedInstanceState);

        stopTimer();

    }

    @Override
    public void onPause() {

        // TODO: 2016-03-17 maybe not necessary to save _swimState to main, main is asking for _swimState when other fragments are asking for it...
        //Send _swimState to activity!
        super.onPause();
        mCallback.onSaveState(_swimState);
        writeSysOut("===== " + this.getClass().getSimpleName() + "." + new Object() {
        }.getClass().getEnclosingMethod().getName() + " ===== Save _swimState to activity ");
    }

    @Override
    public void onResume() {
        super.onResume();

        writeSysOut("===== " + this.getClass().getSimpleName() + "." + new Object() {
        }.getClass().getEnclosingMethod().getName() + " =====");

        if (_swimState.isRunning) {
            writeSysOut("===== " + this.getClass().getSimpleName() + "." + new Object() {
            }.getClass().getEnclosingMethod().getName() + " ==== _swimState is isRunning ");
            _timerHandler.postDelayed(timerStopWatchUpdater, 0);
        }
    }

    public SwimState getSwimState() {
        return _swimState;
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static StopWatchFragment newInstance() {
        return new StopWatchFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_stopwatch, container, false);

        _rootView = rootView;

        writeSysOut("===== " + this.getClass().getSimpleName() + "." + new Object() {
        }.getClass().getEnclosingMethod().getName() + " =====");

        // Standard code for restoring _swimState
        super.onCreate(savedInstanceState); // Always call the superclass first

        // Check whether we're recreating a previously destroyed instance
        if (savedInstanceState != null) {
            // Restore SwimState
            _swimState = (SwimState) savedInstanceState.getSerializable(KEY_SWIMSTATE);
            _mode = ((MainActivity)getActivity()).getMode();
        } else {
            // Initialize members with default values for a new instance
            _swimState = new SwimState();
            _swimState.Lanes = new ArrayList<Lane>();
            for (int i = 0; i < ((MainActivity) getActivity()).getNumberOfLanes(); i++) {
                Lane lane = new Lane();
                _swimState.Lanes.add(lane);
            }
        }

        buildFragment();

        if (_swimState.isRunning) {
            forceStartTimer(); //restart timer when recreating activity
        }

        if (_mode == MODE_INDIVIDUAL) {
            if (_swimState.isRunning) {

                LinearLayout llSW = (LinearLayout) _rootView.findViewById(R.id.llSW);
                for (int i = MAINLANE; i < ((MainActivity) getActivity()).getNumberOfLanes(); i++) {
                    LinearLayout ll = (LinearLayout) llSW.getChildAt(i);
                    Button btn = (Button) ll.getChildAt(POSITION_BUTTON_START);
                    int laneNumber = (int) btn.getTag();
                    if (laneNumber == MAINLANE) {
                        btn.setText(getString(R.string.StopAll));
                    } else {
                        if (_swimState.Lanes.get(laneNumber).isRunning)
                            btn.setText(getString(R.string.Lap));
                        else
                            btn.setText(getString(R.string.Start));
                    }

                }
            } else {
                // Show total time for all lanes except for main which is reset to zero
                // Do not use showTotals() as this method shows text and delays... not pretty on startup
                // Show total time for all lanes
                LinearLayout llSW = (LinearLayout) _rootView.findViewById(R.id.llSW);
                for (int laneIdx = MAINLANE; laneIdx < ((MainActivity) getActivity()).getNumberOfLanes(); laneIdx++) {
                    LinearLayout ll = (LinearLayout) llSW.getChildAt(laneIdx);
                    EditText et = (EditText) ll.getChildAt(POSITION_TEXTVIEW);
                    if (laneIdx == MAINLANE) {
                        et.setText(DisplayTimerString(0)); //reset this one for clarity
                    } else {
                        et.setText(DisplayTimerString(getTotalTime(_swimState.Lanes.get(laneIdx))));
                    }
                }
            }
        }
        if (_mode == MODE_INCREMENT) {
            if (_swimState.isRunning) {

                LinearLayout llSW = (LinearLayout) _rootView.findViewById(R.id.llSW);
                LinearLayout ll = (LinearLayout) llSW.getChildAt(MAINLANE);
                Button btn = (Button) ll.getChildAt(POSITION_BUTTON_START);
                int laneNumber = (int) btn.getTag();
                if (laneNumber == MAINLANE) {
                    btn.setText(String.format(getString(R.string.increment_split), getNextLaneIdx()));
                }
            } else {
                // Show total time for all lanes
                LinearLayout llSW = (LinearLayout) _rootView.findViewById(R.id.llSW);
                for (int laneIdx = MAINLANE; laneIdx < ((MainActivity) getActivity()).getNumberOfLanes(); laneIdx++) {
                    LinearLayout ll = (LinearLayout) llSW.getChildAt(laneIdx);
                    EditText et = (EditText) ll.getChildAt(POSITION_TEXTVIEW);
                    et.setText(DisplayTimerString(getTotalTime(_swimState.Lanes.get(laneIdx))));
                }
            }
        }

        return rootView;
    }

    private EditText createCustomTimerView(int tag, String viewText) {
        EditText et = new EditText(getContext());
        LinearLayout.LayoutParams paramsTextView = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        paramsTextView.setMargins(0, 0, 5, 2);
        et.setEnabled(false); //No edit!
        et.setFocusable(false); // Keep keyboard from popping up!
        et.setLayoutParams(paramsTextView);
        et.setTag(tag);
        et.setText(viewText);
        et.setTextColor(Color.WHITE);
        et.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorDarkBackground));
        et.setMaxWidth(500);
        et.setPadding(0, -50, 0, -50);
//use in info fragment        tv.setHintTextColor(Color.WHITE);
//        tv.setGravity(Gravity.CENTER);

        return et;
    }

    private EditText createCustomEditText(String viewText) {
        EditText et = new EditText(getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 5, 2);
        et.setEnabled(false); //No edit!
        et.setFocusable(false); // Keep keyboard from popping up!
        et.setLayoutParams(params);
        et.setText(viewText);
        et.setTextColor(Color.WHITE);
        et.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorDarkBackground));
        et.setPadding(0, -50, 0, -50);

        return et;
    }

    private Button createCustomButton(int tag, String buttonText) {
        Button btn = new Button(getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        //params.setMargins(0, -10, 0, -10); set in resizescreen
        btn.setTag(tag);
        btn.setMaxWidth(50);
        btn.setText(buttonText);
        btn.setTextColor(ContextCompat.getColor(getContext(), android.R.color.holo_blue_dark));
        btn.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.smoothbutton));

        //btn.setPadding(10, 0, 10, 0);
        btn.setMinWidth(0);
        btn.setMinHeight(0);
        btn.setLayoutParams(params);

        return btn;
    }

    public void setMode(int mode) {
        if (_mode != mode) {
            _mode = mode;
            buildFragment();
        }
    }
    /**
     * Start or stop all lanes
     */
    private OnClickListener onClickStartStopMainButton = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Button b = (Button) v;

            long currentTime = System.currentTimeMillis();
            if (!_swimState.isRunning) {
                // Start all lanes!
                for (Lane lane : _swimState.Lanes) {
                    lane.StartTime = currentTime;
                    lane.LastStartTime = currentTime;
                    lane.isRunning = true;
                    lane.Laps.clear();
                }

                _swimState.startTime = currentTime;

                b.setText(getString(R.string.StopAll));

                // Set all buttons to "LAP" (main is already set)
                LinearLayout llSW = (LinearLayout) _rootView.findViewById(R.id.llSW);
                for (int laneIdx = LANE1; laneIdx < ((MainActivity) getActivity()).getNumberOfLanes(); laneIdx++) {
                    LinearLayout ll = (LinearLayout) llSW.getChildAt(laneIdx);
                    ((Button) ll.getChildAt(POSITION_BUTTON_START)).setText(getString(R.string.Lap));

                    ((Button) ll.getChildAt(POSITION_BUTTON_RESET)).setVisibility(View.GONE);
                    ((Button) ll.getChildAt(POSITION_BUTTON_STOP)).setVisibility(View.VISIBLE);
                }

                // Keep screen alive while timers are isRunning
                ((MainActivity)getActivity()).keepScreenOn();
                writeSysOut("===== " + this.getClass().getSimpleName() + "." + new Object() {
                }.getClass().getEnclosingMethod().getName() + " ===== START ALL button pressed!");

                startTimer();
            } else {
                //Stop StopWatch!
                // tv.setText(DisplayTimerString(getElapsedTime(laneNumber)));

                //Special case stop all!
                stopTimer();
                resetButtons();
                stopAllLanes();
                showTotals();

                // No need to keep app alive if no timers are isRunning
                ((MainActivity)getActivity()).clearScreenOn();

                _swimState.isRunning = false;
                createShareIntent();
                writeSysOut("===== " + this.getClass().getSimpleName() + "." + new Object() {
                }.getClass().getEnclosingMethod().getName() + " ===== STOP ALL button pressed!");
            }
        }
    };
    private void createShareIntent() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("text/plain");

        //Prepare state with names from Infofragment
        loadLaneNamesIntoSwimState(_swimState, _laneNames);

        intent.putExtra(Intent.EXTRA_TEXT, createTextResult(_swimState));

        ((MainActivity)getActivity()).doShare(intent);
    }

    private void loadLaneNamesIntoSwimState(SwimState swimState, ArrayList<String> laneNames) {
        String nameString;
        for (int laneIdx = MAINLANE + 1; laneIdx < swimState.Lanes.size(); laneIdx++) {
            if (laneNames != null && laneNames.size() > laneIdx)
                if (laneNames.get(laneIdx).trim().equals(""))
                    nameString = String.format("Lane %s", Integer.toString(laneIdx));
                else
                    nameString = laneNames.get(laneIdx);
            else
                nameString = String.format("Lane %s", Integer.toString(laneIdx));

            swimState.Lanes.get(laneIdx).LaneName = nameString;
        }
    }
        
    /**
     * Start lane or if isRunning already record lap time
     */
    private OnClickListener onClickStartLapButton = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Button b = (Button) v;
            int laneNumber = (int) b.getTag();

            long currentTime = System.currentTimeMillis();
            LinearLayout llSW = (LinearLayout) _rootView.findViewById(R.id.llSW);
            LinearLayout ll = (LinearLayout) llSW.getChildAt(laneNumber);
            if (_swimState.Lanes.get(laneNumber).isRunning) {
                //display elapsed time between start or last lap
                writeSysOut("LANE " + laneNumber + " ==LAP== button pressed!");

                ((EditText) ll.getChildAt(POSITION_TEXTVIEW)).setText(DisplayTimerString(getElapsedTime(laneNumber)));
                Lane lane = _swimState.Lanes.get(laneNumber);
                lane.Laps.add(currentTime - lane.LastStartTime);
                lane.LastStartTime = currentTime;
                b.setText(getString(R.string.Lap));
            } else {
                //Start specific lane and master lane from scratch!
                writeSysOut("LANE " + laneNumber + " ==START== button pressed!");
                Lane lane = _swimState.Lanes.get(laneNumber);
                lane.StartTime = currentTime;
                lane.LastStartTime = currentTime;
                lane.isRunning = true;
                //lane.Laps.clear(); only do this when pressing Clear
                b.setText(getString(R.string.Lap));
                ((Button) ll.getChildAt(POSITION_BUTTON_RESET)).setVisibility(View.GONE);
                ((Button) ll.getChildAt(POSITION_BUTTON_STOP)).setVisibility(View.VISIBLE);

                Lane mainLane = _swimState.Lanes.get(MAINLANE);
                if (!mainLane.isRunning) {
                    _swimState.startTime = currentTime;
                    mainLane.StartTime = currentTime; // TODO: 2016-04-11 use smart timing here for incorporating paus behaviour on main timer, spread to all timers with stop but not clear 
                    mainLane.LastStartTime = currentTime;
                    mainLane.isRunning = true;
                    //mainLane.Laps.clear(); only do this when pressing clear
                    ll = (LinearLayout) llSW.getChildAt(MAINLANE); //find block for lane number
                    ((Button) ll.getChildAt(POSITION_BUTTON_START)).setText(getString(R.string.StopAll)); //set button to "Stop all"
                }
            }
            startTimer();
        }
    };

    /**
     * Stop lane 
     */
    private OnClickListener onClickStopButton = new OnClickListener() {
        @Override
        public void onClick(View v) {
            long currentTime = System.currentTimeMillis();
            Button b = (Button) v;
            int laneNumber = (int) b.getTag();

            if (_swimState.Lanes.get(laneNumber).isRunning) {
                writeSysOut("LANE " + laneNumber + " ==STOP== button pressed!");

                //display elapsed time between start or last lap
                LinearLayout llSW = (LinearLayout) _rootView.findViewById(R.id.llSW);
                LinearLayout ll = (LinearLayout) llSW.getChildAt(laneNumber);
                //laptime // TODO: 2016-03-29 laptime wait and then total then wait and then the total time is shown ((EditText) ll.getChildAt(POSITION_TEXTVIEW)).setText(DisplayTimerString(getElapsedTime(laneNumber)));
                Lane lane = _swimState.Lanes.get(laneNumber);
                lane.Laps.add(currentTime - _swimState.Lanes.get(laneNumber).LastStartTime);
                lane.isRunning = false;
                ((EditText) ll.getChildAt(POSITION_TEXTVIEW)).setText(DisplayTimerString(lane.Laps.get(lane.Laps.size() - 1)));
                ((Button) ll.getChildAt(POSITION_BUTTON_START)).setText(getString(R.string.Start));
                ((Button) ll.getChildAt(POSITION_BUTTON_RESET)).setVisibility(View.VISIBLE);
                ((Button) ll.getChildAt(POSITION_BUTTON_STOP)).setVisibility(View.GONE);

                // When stop button is clicked create a new share intent for all lanes
                createShareIntent();
            }
        }
    };

    private void stopTimer() {
        _timerHandler.removeCallbacksAndMessages(null);
        writeSysOut("===== " + this.getClass().getSimpleName() + "." + new Object() {
        }.getClass().getEnclosingMethod().getName() + " ===== STOP TIMER !!!!!");
    }

    private void startTimer() {
        if (!_swimState.isRunning) {
            // This should only happen once, started or not started for all lanes
            _timerHandler.postDelayed(timerStopWatchUpdater, 0);
            _swimState.isRunning = true;
            writeSysOut("===== " + this.getClass().getSimpleName() + "." + new Object() {
            }.getClass().getEnclosingMethod().getName() + " ===== TIMER STARTED !!!!!");
        }
    }

    private void forceStartTimer() {
        _timerHandler.postDelayed(timerStopWatchUpdater, 0);
        writeSysOut("===== " + this.getClass().getSimpleName() + "." + new Object() {
        }.getClass().getEnclosingMethod().getName() + " ===== TIMER RESTARTED !!!!!");
    }

    public void stopAllLanes() {
        // Store total time in mainlane
        _swimState.Lanes.get(MAINLANE).Laps.add(System.currentTimeMillis() - _swimState.Lanes.get(MAINLANE).StartTime);

        _swimState.isRunning = false;
        for (Lane lane : _swimState.Lanes) {
            lane.isRunning = false;
        }
    }

/*
    public void showLastLapForLane(Lane lane) {

        // Set timers to "Total"
        LinearLayout llSW = (LinearLayout) _rootView.findViewById(R.id.llSW);
        LinearLayout ll = (LinearLayout) llSW.getChildAt(laneNumber);

        ((EditText) ll.getChildAt(POSITION_TEXTVIEW)).setText(DisplayTimerString(getTotalTime(lane))); //DisplayTimerString((lane.Laps.get(lane.Laps.size() - 1))));
       // _timerHandler.postDelayed(this.timerRunnableShowTotals, 500);
    }
*/

    public void showTotals() {

        // Set all timers to "Total"
        LinearLayout llSW = (LinearLayout) _rootView.findViewById(R.id.llSW);
        for (int laneIdx = MAINLANE; laneIdx < ((MainActivity) getActivity()).getNumberOfLanes(); laneIdx++) {
            LinearLayout ll = (LinearLayout) llSW.getChildAt(laneIdx);
/*
            if (laneIdx == MAINLANE)
                ((EditText) ll.getChildAt(POSITION_TEXTVIEW)).setText(getString(R.string.Reset));
            else
*/
                ((EditText) ll.getChildAt(POSITION_TEXTVIEW)).setText(getString(R.string.Total));
        }

        _timerHandler.postDelayed(this.timerRunnableShowTotals, 500);
    }

    public void resetButtons() {
        // Set all buttons to "Start"
        LinearLayout llSW = (LinearLayout) _rootView.findViewById(R.id.llSW);
        for (int laneIdx = MAINLANE; laneIdx < ((MainActivity) getActivity()).getNumberOfLanes(); laneIdx++) {
            LinearLayout ll = (LinearLayout) llSW.getChildAt(laneIdx);
            if (laneIdx == MAINLANE)
                ((Button) ll.getChildAt(POSITION_BUTTON_START)).setText(getString(R.string.StartAll));
            else {
                ((Button) ll.getChildAt(POSITION_BUTTON_START)).setText(getString(R.string.Start));
                ((Button) ll.getChildAt(POSITION_BUTTON_RESET)).setVisibility(View.VISIBLE);
                ((Button) ll.getChildAt(POSITION_BUTTON_STOP)).setVisibility(View.GONE);
            }
        }
    }

    private OnClickListener onClickClearButton = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Toast t = Toast.makeText(getContext(), getString(R.string.sw_longtoclear), Toast.LENGTH_SHORT);
            t.show();
        }
    };

    private View.OnLongClickListener onLongClickClearButton = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            Button b = (Button) v;
            int laneNumber = (int) b.getTag();
            //            int laneNumber = Integer.valueOf((String) b.getTag());

            if (laneNumber == MAINLANE) {
                //clear all data
                for (Lane lane : _swimState.Lanes) {
                    lane.Laps.clear();
                    lane.StartTime = 0;
                    lane.LastStartTime = 0;
                    lane.isRunning = false;
                }

                //reset top level
                _swimState.isRunning = false;
                _swimState.startTime = 0;

                //Stop timer callbacks
                stopTimer();

                //Display 0 in stopwatch fields
                LinearLayout llSW = (LinearLayout) _rootView.findViewById(R.id.llSW);
                for (int laneIdx = 0; laneIdx < ((MainActivity) getActivity()).getNumberOfLanes(); laneIdx++) {
                    LinearLayout ll = (LinearLayout) llSW.getChildAt(laneIdx);
                    ((EditText) ll.getChildAt(POSITION_TEXTVIEW)).setText(DisplayTimerString(0));
                }

                //Set correct text and show/hide buttons
                resetButtons();
            } else {
                // clear one!
                LinearLayout llSW = (LinearLayout) _rootView.findViewById(R.id.llSW); // Block containing all lanes
                LinearLayout ll = (LinearLayout) llSW.getChildAt(laneNumber); // Block containing one lane
                ((EditText) ll.getChildAt(POSITION_TEXTVIEW)).setText(DisplayTimerString(0)); // Get the EditText and update it
                ((Button) ll.getChildAt(POSITION_BUTTON_START)).setText(getString(R.string.Start)); // Get the EditText and update it
                Lane lane = _swimState.Lanes.get(laneNumber);
                lane.Laps.clear();
                lane.StartTime = 0;
                lane.LastStartTime = 0;
                lane.isRunning = false;
                // reset button text doesn't change so no point in setting it again
                // b.setText(getString(R.string.X));
            }
            return true;
        }
    };

    /**
     * calc the time since last lap save
     * extra function, return latest laptime for 2,5 seconds after saving a lap
     */
    public long getElapsedTime(int laneIdx) {
        long elapsedTime;
        long currentTime = System.currentTimeMillis();

        Lane lane = _swimState.Lanes.get(laneIdx);
        if (lane.isRunning) {
            elapsedTime = currentTime - lane.LastStartTime;
            if (lane.Laps.size() > 0) {

                //code for calculating last laptime
//                long latestLap;
//                if (lane.Laps.size() == 1)
//                    latestLap = lane.Laps.get(0) - lane.StartTime;
//                else
//                    latestLap = lane.Laps.get(lane.Laps.size() - 1);

                // after saving a lap show that lap time for 2,5 seconds, but not if just restarted from stop
                if (elapsedTime < 2500 && (currentTime - lane.StartTime) > 2500) {
                    long latestLap = lane.Laps.get(lane.Laps.size() - 1);
                    return latestLap;
                }
            }
        } else
            elapsedTime = 0;

        return elapsedTime;
    }

    public void refreshLaneNames(ArrayList<String> laneNames) {
        int numberOfLanes = ((MainActivity) getActivity()).getNumberOfLanes();
        LinearLayout parent = (LinearLayout) _rootView.findViewById(R.id.llSW);
        String nameString;
        _laneNames = laneNames;
        for (int laneIdx = MAINLANE; laneIdx < numberOfLanes; laneIdx++) {
            LinearLayout ll_0 = (LinearLayout) parent.getChildAt(laneIdx);
            if (laneIdx == MAINLANE) {
                nameString = getString(R.string.Base);
            } else {
                if (_laneNames != null && _laneNames.size() > laneIdx)
                    if (_laneNames.get(laneIdx).trim().equals(""))
                        nameString = String.format("Lane %s", Integer.toString(laneIdx));
                    else
                        nameString = _laneNames.get(laneIdx);
                else
                    nameString = String.format("Lane %s", Integer.toString(laneIdx));

            }
            ((EditText) ll_0.getChildAt(POSITION_LANE_NAME)).setText(nameString);
        }
    }

    @Override
    public void fragmentBecameVisible(int previousFragment) {
        writeSysOut("===== " + this.getClass().getSimpleName() + "." + new Object() {
        }.getClass().getEnclosingMethod().getName() + " =====");
    }

    @Override
    public void fragmentPageScrolled(int scrollState) {
    }

    @Override
    public void addLane() {
//        writeSysOut("===== " + this.getClass().getSimpleName() + "." + new Object() {
//        }.getClass().getEnclosingMethod().getName() + " =====");
        int laneNumber = ((MainActivity) getActivity()).getNumberOfLanes() - 1;
        Lane lane = new Lane();
        _swimState.Lanes.add(lane);
//        writeSysOut("===== " + this.getClass().getSimpleName() + "." + new Object() {
//        }.getClass().getEnclosingMethod().getName() + " =====  Size of states.Lanes:" + _swimState.Lanes.size());

//        int screenOrientation = getScreenOrientation(getActivity());
        buildRow(laneNumber);
        resizeScreen();
    }
    @Override
    public void removeLane() {
        int laneNumber = ((MainActivity) getActivity()).getNumberOfLanes(); //number of lanes is already -1 from calling method, so no -1 here, this will also make resize work with the new count
        _swimState.Lanes.remove(laneNumber);
        writeSysOut("===== " + this.getClass().getSimpleName() + "." + new Object() {
        }.getClass().getEnclosingMethod().getName() + " ===== Size of states.Lanes:" + _swimState.Lanes.size());
        // Get containing block
        LinearLayout parent = (LinearLayout) _rootView.findViewById(R.id.llSW);
        parent.removeViewAt(laneNumber);
        resizeScreen();
    }


    public void buildFragment() {
        // Build details in fragment
        LinearLayout parent = (LinearLayout) _rootView.findViewById(R.id.llSW);

        // Clear the view to allow to rebuild it
        parent.removeAllViews();
        _laneNames = ((MainActivity)getActivity()).getLaneNames();
        int screenOrientation = getScreenOrientation(getActivity());
        int numberOfLanes = ((MainActivity) getActivity()).getNumberOfLanes();
        for (int laneNumber = 0; laneNumber < numberOfLanes; laneNumber++) {
            buildRow(laneNumber);
        }
        resizeScreen();
    }
    private void buildRow(int laneNumber) {

        if (_mode == MODE_INDIVIDUAL) {
            buildRow_modeIndividual(laneNumber);
        } else {
            buildRow_modeIncremental(laneNumber);
        }
    }

    private void buildRow_modeIndividual(int laneNumber) {


        // Get containing block
        LinearLayout parent = (LinearLayout) _rootView.findViewById(R.id.llSW);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout ll_0 = new LinearLayout(getContext());
        ll_0.setLayoutParams(params);
        ll_0.setGravity(Gravity.CENTER);
        ll_0.setTag(laneNumber);

        writeSysOut("===== " + this.getClass().getSimpleName() + "." + new Object() {
        }.getClass().getEnclosingMethod().getName() + " ===== ");

        String laneString = "";
        String nameString = "";
        String counterString;
        String buttonText1 = "";
        String buttonText2 = "";
        String buttonText3 = "";
        if (laneNumber == MAINLANE) {
            buttonText2 = getString(R.string.ResetAll);
            if (_swimState.isRunning) {
                counterString = "";
                buttonText1 = getString(R.string.StopAll);
            } else {
                counterString = getString(R.string.Base);
                buttonText1 = getString(R.string.StartAll);
            }
        } else {
            //same code in refreshlanenames... so remember to duplicate any code changes
            laneString = Integer.toString(laneNumber);
            if (_laneNames != null && _laneNames.size() > laneNumber) {
//                writeSysOut("lanename='" + _laneNames.get(laneNumber).toString() + "'");
                if (_laneNames.get(laneNumber).trim().equals(""))
                    nameString = String.format("Lane %s", Integer.toString(laneNumber));
                else
                    nameString = _laneNames.get(laneNumber);
            }
            else
                nameString = String.format("Lane %s", Integer.toString(laneNumber));

            counterString = "00:00.0";
            if (_swimState.Lanes.get(laneNumber).isRunning) {
                buttonText1 = getString(R.string.split);
                buttonText2 = getString(R.string.X);
                buttonText3 = getString(R.string.Stop);
            } else {
                buttonText1 = getString(R.string.Start);
                buttonText2 = getString(R.string.X);
                buttonText3 = getString(R.string.Stop);
            }
        }
        EditText et = createCustomEditText(laneString);
        ll_0.addView(et);

        et = createCustomEditText(nameString);
        ll_0.addView(et);

        et = createCustomTimerView(laneNumber, counterString);
        ll_0.addView(et);

        Button btn = createCustomButton(laneNumber, buttonText1);
        if (laneNumber == MAINLANE)
            btn.setOnClickListener(onClickStartStopMainButton);
        else
            btn.setOnClickListener(onClickStartLapButton);

        ll_0.addView(btn);

        btn = createCustomButton(laneNumber, buttonText2);
        btn.setOnClickListener(onClickClearButton);
        btn.setOnLongClickListener(onLongClickClearButton);
        if (laneNumber != MAINLANE)
            btn.setVisibility(getVisibilityForButton(laneNumber, POSITION_BUTTON_RESET) ? View.VISIBLE : View.GONE);

        ll_0.addView(btn);

        if (laneNumber != MAINLANE) {
            btn = createCustomButton(laneNumber, buttonText3);
            btn.setOnClickListener(onClickStopButton);
            btn.setVisibility(getVisibilityForButton(laneNumber, POSITION_BUTTON_STOP) ? View.VISIBLE : View.GONE);
            ll_0.addView(btn);
        }

        parent.addView(ll_0);
    }
    private void buildRow_modeIncremental(int laneNumber) {

        // Get containing block
        LinearLayout parent = (LinearLayout) _rootView.findViewById(R.id.llSW);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout ll_0 = new LinearLayout(getContext());
        ll_0.setLayoutParams(params);
        ll_0.setGravity(Gravity.LEFT);
        ll_0.setTag(laneNumber);

        writeSysOut("===== " + this.getClass().getSimpleName() + "." + new Object() {
        }.getClass().getEnclosingMethod().getName() + " ===== ");

        String laneString;
        String nameString;
        String counterString;
        String buttonText1="";
        String buttonText2="";
        String buttonText3="";
        if (laneNumber == MAINLANE) {
            laneString = "";
            nameString = "";
            counterString = getString(R.string.Base);
            int nextLaneIdx = getNextLaneIdx();
            if (_swimState.Lanes.get(nextLaneIdx).isRunning)
                buttonText1 = String.format(getString(R.string.increment_split), nextLaneIdx);
            else
                buttonText1 = String.format(getString(R.string.increment_start), nextLaneIdx);

            buttonText2 = getString(R.string.increment_pause);
            buttonText3 = getString(R.string.Reset);
        } else {
            //same code in refreshlanenames and in buildrow_indiv so remember to duplicate any code changes
            laneString = Integer.toString(laneNumber);
            if (_laneNames != null && _laneNames.size() > laneNumber) {
                if (_laneNames.get(laneNumber).trim().equals(""))
                    nameString = String.format("Lane %s", Integer.toString(laneNumber));
                else
                    nameString = _laneNames.get(laneNumber);
            }
            else
                nameString = String.format("Lane %s", Integer.toString(laneNumber));

            counterString = "00:00.0";
        }
        EditText et = createCustomEditText(laneString);
        ll_0.addView(et);

        et = createCustomEditText(nameString);
        ll_0.addView(et);

        et = createCustomTimerView(laneNumber, counterString);
        ll_0.addView(et);

        // Only create buttons for first row...
        if (laneNumber == MAINLANE) {
            Button btn = createCustomButton(laneNumber, buttonText1);
            btn.setOnClickListener(onClickStartSplitIncrement);
            btn.setId(R.id.start_split_btn);
            ll_0.addView(btn);

            btn = createCustomButton(laneNumber, buttonText2);
            btn.setOnClickListener(onClickPauseIncrement);
            btn.setId(R.id.pause_btn);
            ll_0.addView(btn);

            btn = createCustomButton(laneNumber, buttonText3);
            btn.setOnClickListener(onClickResetIncrement);
            btn.setOnLongClickListener(onLongClickResetIncrement);
            btn.setVisibility(View.GONE);
            btn.setId(R.id.clear_all_btn);
            ll_0.addView(btn);
        }
        parent.addView(ll_0);
    }
    private OnClickListener onClickStartSplitIncrement = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Button b = (Button) v;

            long currentTime = System.currentTimeMillis();
            int nextLaneIdx;

            writeSysOut("startSplit click");

            //Show and hide buttons
            ((Button) _rootView.findViewById(R.id.clear_all_btn)).setVisibility(View.GONE);
            ((Button) _rootView.findViewById(R.id.pause_btn)).setVisibility(View.VISIBLE);

            _swimState.position++;
            if (_swimState.position >= ((MainActivity)getActivity()).getNumberOfLanes()) {
                _swimState.position = 1;
            }
            nextLaneIdx = getNextLaneIdx();
            Lane lane = _swimState.Lanes.get(_swimState.position);
            // TODO: 2016-04-22 vid omstart efter pause...
            if (!lane.isRunning) {
                //Start lane
                lane.StartTime = currentTime;
                lane.LastStartTime = currentTime;
                lane.isRunning = true;
            } else {
                //record split
                lane.Laps.add(currentTime - lane.LastStartTime);
                lane.LastStartTime = currentTime;
            }
            if (!_swimState.isRunning) {
                lane = _swimState.Lanes.get(MAINLANE);
                lane.StartTime = currentTime;
                lane.LastStartTime = currentTime;
                lane.isRunning = true;
                lane.Laps.clear();

                _swimState.startTime = currentTime;
                //Start timer
                startTimer();

                // isRunning is set in startTimer  _swimState.isRunning = true;

                // Keep screen alive while timers are isRunning
                ((MainActivity)getActivity()).keepScreenOn();
            }
            if (_swimState.Lanes.get(nextLaneIdx).isRunning)
                b.setText(String.format(getString(R.string.increment_split), nextLaneIdx));
            else
                b.setText(String.format(getString(R.string.increment_start), nextLaneIdx));

        }
    };
    private int getNextLaneIdx() {
        int nextLane;
        if (_swimState.position + 1 >= ((MainActivity)getActivity()).getNumberOfLanes()) {
            nextLane = 1;
        } else {
            nextLane = _swimState.position + 1;
        }

        return nextLane;
    }
    private OnClickListener onClickPauseIncrement = new OnClickListener() {
        @Override
        public void onClick(View v) {
            _swimState.isRunning = false;
            _swimState.stopTime = System.currentTimeMillis();
            _timerHandler.removeCallbacksAndMessages(null);
            _stopTime = System.currentTimeMillis();

            for (int laneIdx = 0; laneIdx < _swimState.Lanes.size(); laneIdx++) {
                Lane lane = _swimState.Lanes.get(laneIdx);
                lane.isRunning = false;
            }

            //Show and hide buttons
            Button b = (Button) v;
            b.setVisibility(View.GONE);
            ((Button) _rootView.findViewById(R.id.clear_all_btn)).setVisibility(View.VISIBLE);
            _swimState.position = 0; //Reset to start at first row
            ((Button) _rootView.findViewById(R.id.start_split_btn)).setText(getString(R.string.increment_start, 1));
        }
    };
    private View.OnClickListener onClickResetIncrement = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Toast t = Toast.makeText(getContext(), getString(R.string.sw_longtoclear), Toast.LENGTH_SHORT);
            t.show();
        }
    };
    private View.OnLongClickListener onLongClickResetIncrement = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            //clear all data
            for (Lane lane : _swimState.Lanes) {
                lane.Laps.clear();
                lane.StartTime = 0;
                lane.LastStartTime = 0;
                lane.isRunning = false;
            }

            //reset top level
            _swimState.isRunning = false;
            _swimState.position = 0;

            //Stop timer callbacks already done

            //Display 0 in stopwatch fields
            LinearLayout llSW = (LinearLayout) _rootView.findViewById(R.id.llSW);
            for (int laneIdx = 0; laneIdx < ((MainActivity) getActivity()).getNumberOfLanes(); laneIdx++) {
                LinearLayout ll = (LinearLayout) llSW.getChildAt(laneIdx);
                ((EditText) ll.getChildAt(POSITION_TEXTVIEW)).setText(DisplayTimerString(0));
            }

            //Show and hide buttons
            Button b = (Button) v;
            b.setVisibility(View.GONE);
            ((Button) _rootView.findViewById(R.id.pause_btn)).setVisibility(View.VISIBLE);
            return true;
        }
    };

    public boolean getVisibilityForButton(int laneNumber, int btnIdx) {
        if (_swimState != null && _swimState.Lanes.size() > laneNumber && _swimState.Lanes.get(laneNumber).isRunning) {
            if (btnIdx != POSITION_BUTTON_RESET) return true;
            else return false;
        }
        else {
            if (btnIdx == POSITION_BUTTON_RESET)
                return true;
            else
                return false;
        }
    }

    private void resizeScreen() {
        if (_mode == MODE_INDIVIDUAL)
            resizeScreen_Individual();
        else
            resizeScreen_Incremental();
    }
    private void resizeScreen_Individual() {
        writeSysOut("===== " + this.getClass().getSimpleName() + "." + new Object() {
        }.getClass().getEnclosingMethod().getName() + " =====");

        int NumberOfLanes = ((MainActivity) getActivity()).getNumberOfLanes();
        int buttonTextSize;
        int buttonHeight;
        int textSize;
        int stopwatchViewHeight;
        int timerWidth;
        int timerMaxWidth;
        int laneNameVisibility;
        int laneNameWidth;
        int laneNumberWidth;

        //params for buttons only (use another parameter if there is a need for changing edittexts layoutparams...)
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        LinearLayout.LayoutParams params_ll = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        int screenOrientation = getScreenOrientation(getActivity());
        if (screenOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT || screenOrientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT)
            laneNameVisibility = View.GONE;
        else {
            laneNameVisibility = View.VISIBLE;
        }

        if (screenOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT || screenOrientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT) {
            switch (NumberOfLanes) {
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                case 8:
                case 9:
                case 10:
                    //no need to convert to dp Texts can be typed to dp
                    buttonTextSize = 13;
                    textSize = 38;
                    //px
                    buttonHeight = dpToPx(50);
                    stopwatchViewHeight = dpToPx(40);
                    timerWidth = dpToPx(130);
                    timerMaxWidth = dpToPx(200);
                    laneNumberWidth = dpToPx(40);
                    laneNameWidth = dpToPx(200);
                    params.setMargins(0, -15, 0, -15);
                    params_ll.setMargins(0, 0, 5, 20);
                    break;
                default:
                    //dp
                    buttonTextSize = 12;
                    textSize = 30;
                    //px
                    buttonHeight = dpToPx(40);
                    stopwatchViewHeight = dpToPx(30);
                    timerWidth = dpToPx(115);
                    timerMaxWidth = dpToPx(200);
                    laneNumberWidth = dpToPx(40);
                    laneNameWidth = dpToPx(200);
                    params.setMargins(0, -25, 0, -25);
                    params_ll.setMargins(0, 0, 5, 2);
                    break;
            }
        }
        else {
            switch (NumberOfLanes) {
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                    //no need to convert to dp Texts can be typed to dp
                    buttonTextSize = 13;
                    textSize = 38;
                    //px
                    buttonHeight = dpToPx(50);
                    stopwatchViewHeight = dpToPx(40);
                    timerWidth = dpToPx(130);
                    timerMaxWidth = dpToPx(200);
                    laneNumberWidth = dpToPx(40);
                    laneNameWidth = dpToPx(200);
                    params.setMargins(0, -25, 0, -25);
                    params_ll.setMargins(0, 0, 5, 20);
                    break;
                default:
                    //dp
                    buttonTextSize = 12;
                    textSize = 30;
                    //px
                    buttonHeight = dpToPx(40);
                    stopwatchViewHeight = dpToPx(30);
                    timerWidth = dpToPx(115);
                    timerMaxWidth = dpToPx(200);
                    laneNumberWidth = dpToPx(40);
                    laneNameWidth = dpToPx(200);
                    params.setMargins(0, -25, 0, -25);
                    params_ll.setMargins(0, 0, 5, 2);
                    break;
            }
        }

        // Get containing block
        LinearLayout parent = (LinearLayout) _rootView.findViewById(R.id.llSW);
        for (int laneIdx = MAINLANE; laneIdx < NumberOfLanes; laneIdx++) {
            LinearLayout ll_0 = (LinearLayout) parent.getChildAt(laneIdx);
            ll_0.setLayoutParams(params_ll);

            ((EditText) ll_0.getChildAt(POSITION_LANE_NUMBER)).setHeight(stopwatchViewHeight);
            ((EditText) ll_0.getChildAt(POSITION_LANE_NUMBER)).setWidth(laneNumberWidth);
            ((EditText) ll_0.getChildAt(POSITION_LANE_NUMBER)).setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSize);

            ((EditText) ll_0.getChildAt(POSITION_LANE_NAME)).setVisibility(laneNameVisibility);
            ((EditText) ll_0.getChildAt(POSITION_LANE_NAME)).setHeight(stopwatchViewHeight);
            ((EditText) ll_0.getChildAt(POSITION_LANE_NAME)).setWidth(laneNameWidth);
            ((EditText) ll_0.getChildAt(POSITION_LANE_NAME)).setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSize);
            //((EditText) ll_0.getChildAt(POSITION_LANE_NAME)).setLayoutParams(params);

            ((EditText) ll_0.getChildAt(POSITION_TEXTVIEW)).setHeight(stopwatchViewHeight);
            ((EditText) ll_0.getChildAt(POSITION_TEXTVIEW)).setWidth(timerWidth);
            ((EditText) ll_0.getChildAt(POSITION_TEXTVIEW)).setMaxWidth(timerMaxWidth);
            ((EditText) ll_0.getChildAt(POSITION_TEXTVIEW)).setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSize);

           // if (_mode == MODE_INDIVIDUAL || (_mode == MODE_INCREMENT && laneIdx == MAINLANE)) {
                Button btn = (Button) ll_0.getChildAt(POSITION_BUTTON_START);
                btn.setTextSize(TypedValue.COMPLEX_UNIT_DIP, buttonTextSize);
                btn.setHeight(buttonHeight);
                btn.setLayoutParams(params);

                btn = (Button) ll_0.getChildAt(POSITION_BUTTON_RESET);
                if (btn != null) {
                    btn.setTextSize(TypedValue.COMPLEX_UNIT_DIP, buttonTextSize);
                    btn.setHeight(buttonHeight);
                    btn.setLayoutParams(params);
                }
                btn = (Button) ll_0.getChildAt(POSITION_BUTTON_STOP);
                if (btn != null) {
                    btn.setTextSize(TypedValue.COMPLEX_UNIT_DIP, buttonTextSize);
                    btn.setHeight(buttonHeight);
                    btn.setLayoutParams(params);
                }
           // }
/*
    TypedValue.COMPLEX_UNIT_PX   //Pixels
    TypedValue.COMPLEX_UNIT_SP   //Scaled Pixels
    TypedValue.COMPLEX_UNIT_DIP  //Device Independent Pixels
*/
        }
    }
    private void resizeScreen_Incremental() {
        writeSysOut("===== " + this.getClass().getSimpleName() + "." + new Object() {
        }.getClass().getEnclosingMethod().getName() + " =====");

        int NumberOfLanes = ((MainActivity) getActivity()).getNumberOfLanes();
        int buttonTextSize;
        int buttonHeight;
        int textSize;
        int stopwatchViewHeight;
        int timerWidth;
        int timerMaxWidth;
        int laneNameVisibility;
        int laneNameWidth;
        int laneNumberWidth;

        //params for buttons only (use another parameter if there is a need for changing edittexts layoutparams...)
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        LinearLayout.LayoutParams params_ll = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        int screenOrientation = getScreenOrientation(getActivity());
        if (screenOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT || screenOrientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT)
            laneNameVisibility = View.GONE;
        else {
            laneNameVisibility = View.VISIBLE;
        }

        if (screenOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT || screenOrientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT) {
            switch (NumberOfLanes) {
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                case 8:
                case 9:
                    //no need to convert to dp Texts can be typed to dp
                    buttonTextSize = 13;
                    textSize = 38;
                    //px
                    buttonHeight = dpToPx(50);
                    stopwatchViewHeight = dpToPx(40);
                    timerWidth = dpToPx(130);
                    timerMaxWidth = dpToPx(200);
                    laneNumberWidth = dpToPx(40);
                    laneNameWidth = dpToPx(200);
                    params.setMargins(0, -15, 0, -15);
                    params_ll.setMargins(0, 0, 5, 20);
                    break;
                default:
                    //dp
                    buttonTextSize = 12;
                    textSize = 30;
                    //px
                    buttonHeight = dpToPx(40);
                    stopwatchViewHeight = dpToPx(30);
                    timerWidth = dpToPx(115);
                    timerMaxWidth = dpToPx(200);
                    laneNumberWidth = dpToPx(40);
                    laneNameWidth = dpToPx(200);
                    params.setMargins(0, -25, 0, -25);
                    params_ll.setMargins(0, 0, 5, 2);
                    break;
            }
        }
        else {
            switch (NumberOfLanes) {
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                    //no need to convert to dp Texts can be typed to dp
                    buttonTextSize = 13;
                    textSize = 38;
                    //px
                    buttonHeight = dpToPx(50);
                    stopwatchViewHeight = dpToPx(50);
                    timerWidth = dpToPx(130);
                    timerMaxWidth = dpToPx(200);
                    laneNumberWidth = dpToPx(40);
                    laneNameWidth = dpToPx(200);
                    params.setMargins(0, -25, 0, -25);
                    params_ll.setMargins(0, 0, 5, 20);
                    break;
                default:
                    //dp
                    buttonTextSize = 12;
                    textSize = 30;
                    //px
                    buttonHeight = dpToPx(40);
                    stopwatchViewHeight = dpToPx(30);
                    timerWidth = dpToPx(115);
                    timerMaxWidth = dpToPx(200);
                    laneNumberWidth = dpToPx(40);
                    laneNameWidth = dpToPx(200);
                    params.setMargins(0, -25, 0, -25);
                    params_ll.setMargins(0, 0, 5, 2);
                    break;
            }
        }

        // Get containing block
        LinearLayout parent = (LinearLayout) _rootView.findViewById(R.id.llSW);
        for (int laneIdx = MAINLANE; laneIdx < NumberOfLanes; laneIdx++) {
            LinearLayout ll_0 = (LinearLayout) parent.getChildAt(laneIdx);
            ll_0.setLayoutParams(params_ll);

            ((EditText) ll_0.getChildAt(POSITION_LANE_NUMBER)).setHeight(stopwatchViewHeight);
            ((EditText) ll_0.getChildAt(POSITION_LANE_NUMBER)).setWidth(laneNumberWidth);
            ((EditText) ll_0.getChildAt(POSITION_LANE_NUMBER)).setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSize);

            ((EditText) ll_0.getChildAt(POSITION_LANE_NAME)).setVisibility(laneNameVisibility);
            ((EditText) ll_0.getChildAt(POSITION_LANE_NAME)).setHeight(stopwatchViewHeight);
            ((EditText) ll_0.getChildAt(POSITION_LANE_NAME)).setWidth(laneNameWidth);
            ((EditText) ll_0.getChildAt(POSITION_LANE_NAME)).setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSize);
            //((EditText) ll_0.getChildAt(POSITION_LANE_NAME)).setLayoutParams(params);

            ((EditText) ll_0.getChildAt(POSITION_TEXTVIEW)).setHeight(stopwatchViewHeight);
            ((EditText) ll_0.getChildAt(POSITION_TEXTVIEW)).setWidth(timerWidth);
            ((EditText) ll_0.getChildAt(POSITION_TEXTVIEW)).setMaxWidth(timerMaxWidth);
            ((EditText) ll_0.getChildAt(POSITION_TEXTVIEW)).setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSize);

            if (laneIdx == MAINLANE) {
                Button btn = (Button) ll_0.getChildAt(POSITION_BUTTON_START);
                btn.setTextSize(TypedValue.COMPLEX_UNIT_DIP, buttonTextSize);
                btn.setHeight(buttonHeight);
                btn.setLayoutParams(params);

                btn = (Button) ll_0.getChildAt(POSITION_BUTTON_RESET);
                if (btn != null) {
                    btn.setTextSize(TypedValue.COMPLEX_UNIT_DIP, buttonTextSize);
                    btn.setHeight(buttonHeight);
                    btn.setLayoutParams(params);
                }
                btn = (Button) ll_0.getChildAt(POSITION_BUTTON_STOP);
                if (btn != null) {
                    btn.setTextSize(TypedValue.COMPLEX_UNIT_DIP, buttonTextSize);
                    btn.setHeight(buttonHeight);
                    btn.setLayoutParams(params);
                }
            }/*
    TypedValue.COMPLEX_UNIT_PX   //Pixels
    TypedValue.COMPLEX_UNIT_SP   //Scaled Pixels
    TypedValue.COMPLEX_UNIT_DIP  //Device Independent Pixels
*/
        }
    }

    //region Interfacecode for Interface methods to communicate with the Activity as it is done in onpause
    OnSaveStateListener mCallback;

    // Container Activity must implement this interface
    public interface OnSaveStateListener {
        void onSaveState(SwimState state);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnSaveStateListener) context; //setup mCallback for later use
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnSaveStateListener");
        }
    }
    //endregion
}
