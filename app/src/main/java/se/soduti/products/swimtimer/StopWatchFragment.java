package se.soduti.products.swimtimer;

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

import java.util.ArrayList;

import static se.soduti.products.swimtimer.CommonFunctions.DisplayTimerString;
import static se.soduti.products.swimtimer.CommonFunctions.DisplayTimerStringHundreds;
import static se.soduti.products.swimtimer.CommonFunctions.dpToPx;
import static se.soduti.products.swimtimer.CommonFunctions.getScreenOrientation;


/**
 * Created by Larsi on 2016-02-17.
 * Handle stopwatch counters
 */
public class StopWatchFragment extends Fragment implements IFragment {
    private static final String KEY_SWIMSTATE = "swimstate";
    private static final int MAINLANE = 0;
    private static final int LANE1 = 1;
    private static final int POSITION_LANE_NUMBER = 0;
    private static final int POSITION_LANE_NAME = 1;
    private static final int POSITION_TEXTVIEW = 2;
    private static final int POSITION_BUTTON_START = 3;
    private static final int POSITION_BUTTON_RESET = 4;
    private static final int POSITION_BUTTON_STOP = 5;


    public StopWatchFragment() {
    }

    SwimState _swimState;
    ArrayList<String> _laneNames;
    View _rootView;
    Handler _timerHandler = new Handler();

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
                if (_swimState.Lanes.get(laneNumber).Running)
                    et.setText(DisplayTimerString(GetElapsedTime(laneNumber)));
                //else
                //    et.setText(DisplayTimerString(GetTotalTime(laneNumber)));
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
            // Show total time for all lanes
            LinearLayout llSW = (LinearLayout) _rootView.findViewById(R.id.llSW);
            for (int laneIdx = MAINLANE; laneIdx < ((MainActivity) getActivity()).getNumberOfLanes(); laneIdx++) {
                LinearLayout ll = (LinearLayout) llSW.getChildAt(laneIdx);
                EditText et = (EditText) ll.getChildAt(POSITION_TEXTVIEW);
                if (laneIdx == MAINLANE) {
                    et.setText(DisplayTimerString(0)); //reset this one for clarity
                } else {
                    et.setText(DisplayTimerString(GetTotalTime(laneIdx)));
                }
            }
            _swimState.running = false;
            _swimState.startTime = 0;
        }
    };

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        System.out.println("===== " + this.getClass().getSimpleName() + "." + new Object() {
        }.getClass().getEnclosingMethod().getName() + " =====  onSAVEinstanceSTATE Stopwatch *****");
        // Save the SwimState
        savedInstanceState.putSerializable(KEY_SWIMSTATE, _swimState);
        // Always call the superclass so it can save the view hierarchy _swimState
        super.onSaveInstanceState(savedInstanceState);

        StopTimer();

    }

    @Override
    public void onPause() {

        // TODO: 2016-03-17 maybe not necessary to save _swimState to main, main is asking for _swimState when other fragments are asking for it...
        //Send _swimState to activity!
        super.onPause();
        mCallback.onSaveState(_swimState);
        System.out.println("===== " + this.getClass().getSimpleName() + "." + new Object() {
        }.getClass().getEnclosingMethod().getName() + " ===== Save _swimState to activity ");
    }

    @Override
    public void onResume() {
        super.onResume();

        System.out.println("===== " + this.getClass().getSimpleName() + "." + new Object() {
        }.getClass().getEnclosingMethod().getName() + " =====");

        if (_swimState.running) {
            System.out.println("===== " + this.getClass().getSimpleName() + "." + new Object() {
            }.getClass().getEnclosingMethod().getName() + " ==== _swimState is running ");
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
    public static StopWatchFragment newInstance(int sectionNumber) {
        StopWatchFragment fragment = new StopWatchFragment();

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_stopwatch, container, false);

        _rootView = rootView;

        System.out.println("===== " + this.getClass().getSimpleName() + "." + new Object() {
        }.getClass().getEnclosingMethod().getName() + " =====");

        // Standard code for restoring _swimState
        super.onCreate(savedInstanceState); // Always call the superclass first

        // Check whether we're recreating a previously destroyed instance
        if (savedInstanceState != null) {
            // Restore SwimState
            _swimState = (SwimState) savedInstanceState.getSerializable(KEY_SWIMSTATE);
            System.out.println("===== " + this.getClass().getSimpleName() + "." + new Object() {
            }.getClass().getEnclosingMethod().getName() + " instance saved");
        } else {
            System.out.println("===== " + this.getClass().getSimpleName() + "." + new Object() {
            }.getClass().getEnclosingMethod().getName() + " NO instance saved");
            // Initialize members with default values for a new instance
            _swimState = new SwimState();
            _swimState.Lanes = new ArrayList<Lane>();
            for (int i = 0; i < ((MainActivity) getActivity()).getNumberOfLanes(); i++) {
                Lane lane = new Lane();
                _swimState.Lanes.add(lane);
            }
        }

        BuildFragment();

        if (_swimState.running) {
            ForceStartTimer(); //restart timer when recreating activity

            LinearLayout llSW = (LinearLayout) _rootView.findViewById(R.id.llSW);
            for (int i = MAINLANE; i < ((MainActivity) getActivity()).getNumberOfLanes(); i++) {
                LinearLayout ll = (LinearLayout) llSW.getChildAt(i);
                Button btn = (Button) ll.getChildAt(POSITION_BUTTON_START);
                int laneNumber = (int) btn.getTag();
                if (laneNumber == MAINLANE)
                    btn.setText(getString(R.string.StopAll));
                else {
                    if (_swimState.Lanes.get(laneNumber).Running)
                        btn.setText(getString(R.string.Lap));
                    else
                        btn.setText(getString(R.string.Start));
                }

            }
        } else {
            // Show total time for all lanes except for main which is reset to zero
            // Do not use ShowTotals() as this method shows text and delays... not pretty on startup
            // Show total time for all lanes
            LinearLayout llSW = (LinearLayout) _rootView.findViewById(R.id.llSW);
            for (int laneIdx = MAINLANE; laneIdx < ((MainActivity) getActivity()).getNumberOfLanes(); laneIdx++) {
                LinearLayout ll = (LinearLayout) llSW.getChildAt(laneIdx);
                EditText et = (EditText) ll.getChildAt(POSITION_TEXTVIEW);
                if (laneIdx == MAINLANE) {
                    et.setText(DisplayTimerString(0)); //reset this one for clarity
                } else {
                    et.setText(DisplayTimerString(GetTotalTime(laneIdx)));
                }
            }
        }

        return rootView;
    }

    private EditText CreateCustomTimerView(int tag, String viewText) {
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
        et.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorDarkSoft));
        et.setMaxWidth(500);
        et.setPadding(0, -50, 0, -50);
//use in info fragment        tv.setHintTextColor(Color.WHITE);
//        tv.setGravity(Gravity.CENTER);

        return et;
    }

    private EditText CreateCustomEditText(String viewText) {
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
        et.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorDarkSoft));
        et.setPadding(0, -50, 0, -50);
//use in info fragment        tv.setHintTextColor(Color.WHITE);
//        tv.setGravity(Gravity.CENTER);

        return et;
    }

    private Button CreateCustomButton(int tag, String buttonText) {
        Button btn = new Button(getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        //params.setMargins(0, -10, 0, -10); set in resizescreen
        btn.setTag(tag);
        btn.setMaxWidth(50);
        btn.setText(buttonText);
        //btn.setPadding(10, 0, 10, 0);
        btn.setMinWidth(0);
        btn.setMinHeight(0);
        btn.setLayoutParams(params);

        return btn;
    }

    private long GetTotalTime(int laneIdx) {
        Lane lane = _swimState.Lanes.get(laneIdx);
        if (lane.Laps.size() > 0)
            return lane.Laps.get(lane.Laps.size() - 1) - lane.StartTime;
        else
            return 0;
    }

    /**
     * Start or stop all lanes
     */
    private OnClickListener onClickStartStopMainButton = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Button b = (Button) v;
            int laneNumber = (int) b.getTag();

            long currentTime = System.currentTimeMillis();
            if (!_swimState.running) {
                // Start all lanes!
                for (Lane lane : _swimState.Lanes) {
                    lane.StartTime = currentTime;
                    lane.Running = true;
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

                // Keep screen alive while timers are running
                ((MainActivity)getActivity()).keepScreenOn();
                System.out.println("===== " + this.getClass().getSimpleName() + "." + new Object() {
                }.getClass().getEnclosingMethod().getName() + " ===== START ALL button pressed!");

                StartTimer();
            } else {
                //Stop StopWatch!
                // tv.setText(DisplayTimerString(GetElapsedTime(laneNumber)));

                //Special case stop all!
                StopTimer();
                ResetButtons();
                StopAllLanes();
                ShowTotals();

                // No need to keep app alive if no timers are running
                ((MainActivity)getActivity()).clearScreenOn();

                _swimState.running = false;
                createShareIntent();
                System.out.println("===== " + this.getClass().getSimpleName() + "." + new Object() {
                }.getClass().getEnclosingMethod().getName() + " ===== STOP ALL button pressed!");
            }
        }
    };
    private void createShareIntent() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, createTextResult());

        ((MainActivity)getActivity()).doShare(intent);
    }

    private String createTextResult() {
        StringBuilder sb = new StringBuilder();

//// TODO: 2016-04-01          sb.append(String.format("%s %", datetime header));
        for (int laneIdx = MAINLANE + 1; laneIdx < _swimState.Lanes.size(); laneIdx++) {
            loadLaneNamesIntoSwimState();
            sb.append(String.format("%s,", _swimState.Lanes.get(laneIdx).LaneName));
            for (int lapIdx = 0; lapIdx < _swimState.Lanes.get(laneIdx).Laps.size(); lapIdx++) {
                sb.append(String.format("%s,", calculateLaptime(laneIdx, lapIdx)));
            }
            sb.append(String.format("Total %s\n", DisplayTimerStringHundreds(getTotalTime(laneIdx))));
        }

        return sb.toString();
    }

    // TODO: 2016-04-01 same code in different files 
    private long getTotalTime(int laneIdx) {
        Lane lane = _swimState.Lanes.get(laneIdx);
        if (lane.Laps.size() > 0)
            return lane.Laps.get(lane.Laps.size() - 1) - lane.StartTime;
        else
            return 0;
    }

    private void loadLaneNamesIntoSwimState() {
        String nameString;
        for (int laneIdx = MAINLANE + 1; laneIdx < _swimState.Lanes.size(); laneIdx++) {
            if (_laneNames != null && _laneNames.size() > laneIdx)
                if (_laneNames.get(laneIdx).trim().equals(""))
                    nameString = String.format("Lane %s", Integer.toString(laneIdx));
                else
                    nameString = _laneNames.get(laneIdx);
            else
                nameString = String.format("Lane %s", Integer.toString(laneIdx));

            _swimState.Lanes.get(laneIdx).LaneName = nameString;
        }
    }
        
    private String calculateLaptime(int laneIdx, int lapIdx) {
        String newString;
        Lane lane = _swimState.Lanes.get(laneIdx);
        if (lane.Laps.size() == 0)
            return "No laps";
        else {
            //code for calculating last laptime
            long lapTime;
            if (lane.Laps.size() == 1 || lapIdx == 0)
                lapTime = lane.Laps.get(lapIdx) - lane.StartTime;
            else
                lapTime = lane.Laps.get(lapIdx) - lane.Laps.get(lapIdx - 1);

            newString = DisplayTimerStringHundreds(lapTime);
        }
        return newString;
    }

    /**
     * Start lane or if running already record lap time
     */
    private OnClickListener onClickLapButton = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Button b = (Button) v;
            int laneNumber = (int) b.getTag();

            long currentTime = System.currentTimeMillis();
            LinearLayout llSW = (LinearLayout) _rootView.findViewById(R.id.llSW);
            LinearLayout ll = (LinearLayout) llSW.getChildAt(laneNumber);
            if (_swimState.Lanes.get(laneNumber).Running) {
                //display elapsed time between start or last lap
                System.out.println("LANE " + laneNumber + " ==LAP== button pressed!");

                ((EditText) ll.getChildAt(POSITION_TEXTVIEW)).setText(DisplayTimerString(GetElapsedTime(laneNumber)));
                _swimState.Lanes.get(laneNumber).Laps.add(currentTime);
                b.setText(getString(R.string.Lap));
            } else {
                //Start specific lane and master lane from scratch!
                System.out.println("LANE " + laneNumber + " ==START== button pressed!");
                Lane lane = _swimState.Lanes.get(laneNumber);
                lane.StartTime = currentTime;
                lane.Running = true;
                lane.Laps.clear();
                b.setText(getString(R.string.Lap));
                ((Button) ll.getChildAt(POSITION_BUTTON_RESET)).setVisibility(View.GONE);
                ((Button) ll.getChildAt(POSITION_BUTTON_STOP)).setVisibility(View.VISIBLE);

                Lane mainLane = _swimState.Lanes.get(MAINLANE);
                if (!mainLane.Running) {
                    _swimState.startTime = currentTime;
                    mainLane.StartTime = currentTime;
                    mainLane.Running = true;
                    mainLane.Laps.clear();
                    ll = (LinearLayout) llSW.getChildAt(MAINLANE); //find block for lane number
                    ((Button) ll.getChildAt(POSITION_BUTTON_START)).setText(getString(R.string.StopAll)); //set button to "Stop all"
                }
            }
            StartTimer();
        }
    };

    /**
     * Start lane or if running already record lap time
     */
    private OnClickListener onClickStopButton = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Button b = (Button) v;
            int laneNumber = (int) b.getTag();

            long currentTime = System.currentTimeMillis();
            if (_swimState.Lanes.get(laneNumber).Running) {
                System.out.println("LANE " + laneNumber + " ==STOP== button pressed!");

                //display elapsed time between start or last lap
                LinearLayout llSW = (LinearLayout) _rootView.findViewById(R.id.llSW);
                LinearLayout ll = (LinearLayout) llSW.getChildAt(laneNumber);
                //laptime // TODO: 2016-03-29 laptime wait and the n total then wit and then the total time is shown ((EditText) ll.getChildAt(POSITION_TEXTVIEW)).setText(DisplayTimerString(GetElapsedTime(laneNumber)));
                _swimState.Lanes.get(laneNumber).Laps.add(currentTime);
                _swimState.Lanes.get(laneNumber).Running = false;
                ShowTotalForLane(laneNumber);

                ((Button) ll.getChildAt(POSITION_BUTTON_START)).setText(getString(R.string.Start));
                ((Button) ll.getChildAt(POSITION_BUTTON_RESET)).setVisibility(View.VISIBLE);
                ((Button) ll.getChildAt(POSITION_BUTTON_STOP)).setVisibility(View.GONE);
            }
        }
    };

    private void StopTimer() {
        _timerHandler.removeCallbacksAndMessages(null);
        System.out.println("===== " + this.getClass().getSimpleName() + "." + new Object() {
        }.getClass().getEnclosingMethod().getName() + " ===== STOP TIMER !!!!!");
    }

    private void StartTimer() {
        if (!_swimState.running) {
            // This should only happen once, started or not started for all lanes
            _timerHandler.postDelayed(timerStopWatchUpdater, 0);
            _swimState.running = true;
            System.out.println("===== " + this.getClass().getSimpleName() + "." + new Object() {
            }.getClass().getEnclosingMethod().getName() + " ===== TIMER STARTED !!!!!");
        }
    }

    private void ForceStartTimer() {
        _timerHandler.postDelayed(timerStopWatchUpdater, 0);
        System.out.println("===== " + this.getClass().getSimpleName() + "." + new Object() {
        }.getClass().getEnclosingMethod().getName() + " ===== TIMER RESTARTED !!!!!");
    }

    public void StopAllLanes() {
        // Store total time in mainlane
        _swimState.Lanes.get(MAINLANE).Laps.add(System.currentTimeMillis());

        _swimState.running = false;
        for (Lane lane : _swimState.Lanes) {
            lane.Running = false;
        }
    }

    public void ShowTotalForLane(int laneNumber) {

        // Set timers to "Total"
        LinearLayout llSW = (LinearLayout) _rootView.findViewById(R.id.llSW);
        LinearLayout ll = (LinearLayout) llSW.getChildAt(laneNumber);
        ((EditText) ll.getChildAt(POSITION_TEXTVIEW)).setText(DisplayTimerString(GetTotalTime(laneNumber))); //.setText(getString(R.string.Total));
        //_timerHandler.postDelayed(this.timerRunnableShowTotalForLane, 500);
    }

    public void ShowTotals() {

        // Set all timers to "Total"
        LinearLayout llSW = (LinearLayout) _rootView.findViewById(R.id.llSW);
        for (int laneIdx = MAINLANE; laneIdx < ((MainActivity) getActivity()).getNumberOfLanes(); laneIdx++) {
            LinearLayout ll = (LinearLayout) llSW.getChildAt(laneIdx);
            if (laneIdx == MAINLANE)
                ((EditText) ll.getChildAt(POSITION_TEXTVIEW)).setText(getString(R.string.Reset));
            else
                ((EditText) ll.getChildAt(POSITION_TEXTVIEW)).setText(getString(R.string.Total));
        }

        _timerHandler.postDelayed(this.timerRunnableShowTotals, 500);
    }

    public void ResetButtons() {
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
            Button b = (Button) v;
            int laneNumber = (int) b.getTag();
            //            int laneNumber = Integer.valueOf((String) b.getTag());

            if (laneNumber == MAINLANE) {
                //clear all
                for (Lane lane : _swimState.Lanes) {
                    lane.Laps.clear();
                    lane.StartTime = 0;
                    lane.Running = false;
                }

                _swimState.running = false;
                _swimState.startTime = 0;

                StopTimer();
                StopAllLanes();


                LinearLayout llSW = (LinearLayout) _rootView.findViewById(R.id.llSW);
                for (int laneIdx = 0; laneIdx < ((MainActivity) getActivity()).getNumberOfLanes(); laneIdx++) {
                    LinearLayout ll = (LinearLayout) llSW.getChildAt(laneIdx);
                    ((EditText) ll.getChildAt(POSITION_TEXTVIEW)).setText(DisplayTimerString(0));
                }

                ResetButtons();
            } else {
                // clear one!
                LinearLayout llSW = (LinearLayout) _rootView.findViewById(R.id.llSW); // Block containing all lanes
                LinearLayout ll = (LinearLayout) llSW.getChildAt(laneNumber); // Block containing one lane
                ((EditText) ll.getChildAt(POSITION_TEXTVIEW)).setText(DisplayTimerString(0)); // Get the EditText and update it
                ((Button) ll.getChildAt(POSITION_BUTTON_START)).setText(getString(R.string.Start)); // Get the EditText and update it
                Lane lane = _swimState.Lanes.get(laneNumber);
                lane.Laps.clear();
                lane.StartTime = 0;
                lane.Running = false;
                // reset button text doesn't change so no point in setting it again
                // b.setText(getString(R.string.X));
            }
        }
    };

    /**
     * calc the time since last lap save
     * extra function, return latest laptime for 2,5 seconds after saving a lap
     */
    public long GetElapsedTime(int laneIdx) {
        long elapsedTime;

        //        System.out.println("*** lane: " + lane + " IsRunning=" + _swimState.Lanes[lane].Running + " ***");
        Lane lane = _swimState.Lanes.get(laneIdx);
        if (lane.Running) {
            if (lane.Laps.size() == 0)
                elapsedTime = System.currentTimeMillis() - lane.StartTime;
            else {
                elapsedTime = System.currentTimeMillis() - lane.Laps.get(lane.Laps.size() - 1);

                //code for calculating last laptime
                long latestLap;
                if (lane.Laps.size() == 1)
                    latestLap = lane.Laps.get(0) - lane.StartTime;
                else
                    latestLap = lane.Laps.get(lane.Laps.size() - 1) - lane.Laps.get(lane.Laps.size() - 2);

                // after saving a lap show that lap time for 2,5 seconds
                if (elapsedTime < 2500)
                    return latestLap;
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
        System.out.println("===== " + this.getClass().getSimpleName() + "." + new Object() {
        }.getClass().getEnclosingMethod().getName() + " =====");
    }

    @Override
    public void fragmentPageScrolled(int scrollState) {
    }

    @Override
    public void addLane() {
//        System.out.println("===== " + this.getClass().getSimpleName() + "." + new Object() {
//        }.getClass().getEnclosingMethod().getName() + " =====");
        int laneNumber = ((MainActivity) getActivity()).getNumberOfLanes() - 1;
        Lane lane = new Lane();
        _swimState.Lanes.add(lane);
//        System.out.println("===== " + this.getClass().getSimpleName() + "." + new Object() {
//        }.getClass().getEnclosingMethod().getName() + " =====  Size of states.Lanes:" + _swimState.Lanes.size());

        int screenOrientation = getScreenOrientation(getActivity());
        BuildRow(laneNumber, screenOrientation);
        ResizeScreen();
    }

    public void BuildFragment() {
        // Build details in fragment
        LinearLayout parent = (LinearLayout) _rootView.findViewById(R.id.llSW);

        // Clear the view to allow to rebuild it
        parent.removeAllViews();
        _laneNames = ((MainActivity)getActivity()).getLaneNames();
        int screenOrientation = getScreenOrientation(getActivity());
        int numberOfLanes = ((MainActivity) getActivity()).getNumberOfLanes();
        for (int laneNumber = 0; laneNumber < numberOfLanes; laneNumber++) {
            BuildRow(laneNumber, screenOrientation);
        }
        ResizeScreen();
    }

    private void BuildRow(int laneNumber, int screenOrientation) {
        // Get containing block
        LinearLayout parent = (LinearLayout) _rootView.findViewById(R.id.llSW);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout ll_0 = new LinearLayout(getContext());
        ll_0.setLayoutParams(params);
        ll_0.setGravity(Gravity.CENTER);
        ll_0.setTag(laneNumber);

//        System.out.println("===== " + this.getClass().getSimpleName() + "." + new Object() {
//        }.getClass().getEnclosingMethod().getName() + " ===== ");

        String laneString;
        String nameString;
        String counterString;
        String buttonText1;
        String buttonText2;
        String buttonText3;
        if (laneNumber == MAINLANE) {
            laneString = "";
            nameString = "";
            counterString = getString(R.string.Base);
            buttonText1 = getString(R.string.StartAll);
            buttonText2 = getString(R.string.ResetAll);
            buttonText3 = getString(R.string.Stop);
        } else {
            //same code in refreshlanenames... so remember to duplicate any code changes
            laneString = Integer.toString(laneNumber);
            if (_laneNames != null && _laneNames.size() > laneNumber) {
//                System.out.println("lanename='" + _laneNames.get(laneNumber).toString() + "'");
                if (_laneNames.get(laneNumber).trim().equals(""))
                    nameString = String.format("Lane %s", Integer.toString(laneNumber));
                else
                    nameString = _laneNames.get(laneNumber);
            }
            else
                nameString = String.format("Lane %s", Integer.toString(laneNumber));

            counterString = "00:00.0";
            buttonText1 = getString(R.string.Start);
            buttonText2 = getString(R.string.X);
            buttonText3 = getString(R.string.Stop);
        }
        EditText et = CreateCustomEditText(laneString);
        ll_0.addView(et);

        et = CreateCustomEditText(nameString);
        ll_0.addView(et);

        et = CreateCustomTimerView(laneNumber, counterString);
        ll_0.addView(et);

        Button btn = CreateCustomButton(laneNumber, buttonText1);
        if (laneNumber == MAINLANE)
            btn.setOnClickListener(onClickStartStopMainButton);
        else
            btn.setOnClickListener(onClickLapButton);

        ll_0.addView(btn);

        btn = CreateCustomButton(laneNumber, buttonText2);
        btn.setOnClickListener(onClickClearButton);
        if (laneNumber != MAINLANE)
            btn.setVisibility(setVisibility(laneNumber, POSITION_BUTTON_RESET) ? View.VISIBLE : View.GONE);

        ll_0.addView(btn);

        if (laneNumber != MAINLANE) {
            btn = CreateCustomButton(laneNumber, buttonText3);
            btn.setOnClickListener(onClickStopButton);
            btn.setVisibility(setVisibility(laneNumber, POSITION_BUTTON_STOP) ? View.VISIBLE : View.GONE);
            ll_0.addView(btn);
        }

        parent.addView(ll_0);
    }

    public boolean setVisibility(int laneNumber, int btnIdx) {
        if (_swimState != null && _swimState.Lanes.size() > laneNumber && _swimState.Lanes.get(laneNumber).Running) {
            if (btnIdx == POSITION_BUTTON_RESET)
                return false;
            else
                return true;

        }
        else {
            if (btnIdx == POSITION_BUTTON_RESET)
                return true;
            else
                return false;
        }
    }

    @Override
    public void removeLane() {
        int laneNumber = ((MainActivity) getActivity()).getNumberOfLanes(); //number of lanes is already -1 from calling method, so no -1 here, this will also make resize work with the new count
        _swimState.Lanes.remove(laneNumber);
        System.out.println("===== " + this.getClass().getSimpleName() + "." + new Object() {
        }.getClass().getEnclosingMethod().getName() + " ===== Size of states.Lanes:" + _swimState.Lanes.size());
        // Get containing block
        LinearLayout parent = (LinearLayout) _rootView.findViewById(R.id.llSW);
        parent.removeViewAt(laneNumber);
        ResizeScreen();
    }

    private void ResizeScreen() {
        System.out.println("===== " + this.getClass().getSimpleName() + "." + new Object() {
        }.getClass().getEnclosingMethod().getName() + " =====");

        int NumberOfLanes = ((MainActivity) getActivity()).getNumberOfLanes();
        int buttonTextSize;
        int buttonHeight;
        int textSize;
        int viewHeight;
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
                    viewHeight = dpToPx(40);
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
                    viewHeight = dpToPx(30);
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
                    viewHeight = dpToPx(40);
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
                    viewHeight = dpToPx(30);
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

            ((EditText) ll_0.getChildAt(POSITION_LANE_NUMBER)).setHeight(viewHeight);
            ((EditText) ll_0.getChildAt(POSITION_LANE_NUMBER)).setWidth(laneNumberWidth);
            ((EditText) ll_0.getChildAt(POSITION_LANE_NUMBER)).setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSize);

            ((EditText) ll_0.getChildAt(POSITION_LANE_NAME)).setVisibility(laneNameVisibility);
            ((EditText) ll_0.getChildAt(POSITION_LANE_NAME)).setHeight(viewHeight);
            ((EditText) ll_0.getChildAt(POSITION_LANE_NAME)).setWidth(laneNameWidth);
            ((EditText) ll_0.getChildAt(POSITION_LANE_NAME)).setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSize);
            //((EditText) ll_0.getChildAt(POSITION_LANE_NAME)).setLayoutParams(params);

            ((EditText) ll_0.getChildAt(POSITION_TEXTVIEW)).setHeight(viewHeight);
            ((EditText) ll_0.getChildAt(POSITION_TEXTVIEW)).setWidth(timerWidth);
            ((EditText) ll_0.getChildAt(POSITION_TEXTVIEW)).setMaxWidth(timerMaxWidth);
            ((EditText) ll_0.getChildAt(POSITION_TEXTVIEW)).setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSize);

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
/*
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
