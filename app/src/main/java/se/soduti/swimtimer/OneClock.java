package se.soduti.swimtimer;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import static se.soduti.swimtimer.CommonFunctions.DisplayTimerStringHundredsCondensed;
import static se.soduti.swimtimer.CommonFunctions.DisplayTimerString;
import static se.soduti.swimtimer.CommonFunctions.writeFile;
import static se.soduti.swimtimer.CommonFunctions.writeSysOut;

public class OneClock extends AppCompatActivity {

    boolean _timerRunning = false;
    long _startTime;
    long _stopTime;
    Handler _timerHandler = new Handler();
    ArrayList<Long> _listOfTimes;
    private RecyclerView _recyclerView;
    private ResultsArrayAdapter _adapter;
    private ShareActionProvider _shareActionProvider;
    private static final String LOG_TAG = "SwimTimerLog";
    private static final String KEY_LIST_OF_TIMES = "listoftimes";
    private static final String KEY_TIMER_RUNNING = "timerrunning";
    private static final String KEY_STARTTIME = "starttime";
    private static final String KEY_STOPTIME = "stoptime";
    private static final String KEY_DIALOG_TITLE = "title";
    private static final String KEY_DIALOG_MESSAGE = "message";
    /**
     * Update stopwatch on timer
     */
    Runnable timerStopWatchUpdater = new Runnable() {

        @Override
        public void run() {

            TextView tv = (TextView) findViewById(R.id.one_tvStopWatch);
            tv.setText(DisplayTimerString(System.currentTimeMillis() - _startTime));

            _timerHandler.postDelayed(this, 100);
        }
    };

    @Override
    public void onConfigurationChanged(Configuration config) {
    }
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        writeSysOut("===== " + this.getClass().getSimpleName() + "." + new Object() {
        }.getClass().getEnclosingMethod().getName());

        savedInstanceState.putSerializable(KEY_LIST_OF_TIMES, _listOfTimes);
        savedInstanceState.putLong(KEY_STARTTIME, _startTime);
        savedInstanceState.putLong(KEY_STOPTIME, _stopTime);
        savedInstanceState.putBoolean(KEY_TIMER_RUNNING, _timerRunning);
        // Always call the superclass so it can save the view hierarchy
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onPause() {
        super.onPause();
        writeSysOut("===== " + this.getClass().getSimpleName() + "." + new Object() {
        }.getClass().getEnclosingMethod().getName() + " =====");

        Bundle savedInstanceState = new Bundle();
        savedInstanceState.putSerializable(KEY_LIST_OF_TIMES, _listOfTimes);
        savedInstanceState.putLong(KEY_STARTTIME, _startTime);
        savedInstanceState.putLong(KEY_STOPTIME, _stopTime);
        savedInstanceState.putBoolean(KEY_TIMER_RUNNING, _timerRunning);

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        writeSysOut("===== " + this.getClass().getSimpleName() + "." + new Object() {
        }.getClass().getEnclosingMethod().getName() + " =====");

/*
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            _listOfTimes = (ArrayList<Long>) extras.getSerializable(KEY_LIST_OF_TIMES);
            _startTime = extras.getLong(KEY_STARTTIME);
            _stopTime = extras.getLong(KEY_STOPTIME);
            _timerRunning = extras.getBoolean(KEY_TIMER_RUNNING);
        }
*/
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null && savedInstanceState.containsKey(KEY_LIST_OF_TIMES)) {
            _listOfTimes = (ArrayList<Long>) savedInstanceState.getSerializable(KEY_LIST_OF_TIMES);
            _startTime = savedInstanceState.getLong(KEY_STARTTIME);
            _stopTime = savedInstanceState.getLong(KEY_STOPTIME);
            _timerRunning = savedInstanceState.getBoolean(KEY_TIMER_RUNNING);
        } else {
            _timerRunning = false;
            _listOfTimes = new ArrayList<>();
        }
        setContentView(R.layout.activity_one_clock);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_oneclock);
        setSupportActionBar(toolbar);
        Button bStart = (Button) findViewById(R.id.one_start);
/*
        toolbar.setNavigationIcon(R.drawable.ic_menu_back_white);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
*/
        bStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button b = (Button) v;
                if (!_timerRunning) b.setText(getString(R.string.one_split));
                startOrSplit();
            }
        });
        bStart.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                createNewLap();
                return true;
            }
        });
        Button bClear = (Button) findViewById(R.id.one_clear);
        bClear.setVisibility(View.GONE) ;
        bClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearAll();
            }
        });
        Button bStop = (Button) findViewById(R.id.one_stop);
        bStop.setVisibility(View.GONE) ;
        bStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopTimer();
            }
        });

        if (_timerRunning) {
            //Start timer
            _timerHandler.postDelayed(timerStopWatchUpdater, 0);
            bStart.setText(getString(R.string.one_split));
            bStop.setVisibility(View.VISIBLE);
        }
        if (_listOfTimes.size() > 0) {
            TextView tv = (TextView) findViewById(R.id.one_tvStopWatch);
            //Calculate the time that the stopwatch was stopped, increase the starttime with the diff between stop and now.
            if (_stopTime > _startTime)
                _startTime = _startTime + (System.currentTimeMillis() - _stopTime);
            tv.setText(DisplayTimerString(System.currentTimeMillis() - _startTime));
            tv = (TextView) findViewById(R.id.one_tvShowLatest);
            tv.setText(DisplayTimerString(_listOfTimes.get(_listOfTimes.size()-1)));
            if (_timerRunning) {
                bClear.setVisibility(View.VISIBLE);
            }
        }

        //setup the recyclerview
        _recyclerView = (RecyclerView) findViewById(R.id.one_rlv);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        _recyclerView.setHasFixedSize(true);

        // mandatory to create a layout manager
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        _recyclerView.setLayoutManager(layoutManager);

        _adapter = new ResultsArrayAdapter(_listOfTimes);
        _recyclerView.setAdapter(_adapter);
    }

    private void createNewLap() {
        _listOfTimes.add(0, (long) 0);
        _adapter.notifyItemInserted(0);
        _recyclerView.scrollToPosition(0);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_oneclock, menu);

        MenuItem item = menu.findItem(R.id.menu_item_share);
        // Fetch and store ShareActionProvider
        _shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        if (_shareActionProvider != null) {
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "No recorded laps found.");
            _shareActionProvider.setShareIntent(shareIntent);

        }
        // Use custom history for share dropdown
        //_shareActionProvider.setShareHistoryFileName("custom_share_history.xml");
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        Intent intent;

        switch (id) {
            case R.id.menu_item_reset:
                clearAll();
                return true;
            case R.id.menu_item_switch_activity:
                if (_timerRunning || _listOfTimes.size() > 0) {
                    AlertDialogFragment alertDialogFragment = AlertDialogFragment.newInstance(R.string.close_title, R.string.close_question);
                    alertDialogFragment.show(getSupportFragmentManager(), "dialog");
                } else
                    onBackPressed(); //better to use back so that activity is popped from stack!
//                intent = new Intent(this, MainActivity.class);
//                startActivity(intent);
                return true;
            case R.id.menu_item_save:
//                Toast t = Toast.makeText(this, "Not implemented yet", Toast.LENGTH_SHORT);
//                t.show();
                saveResultsToDisk();
                return true;
            case R.id.menu_item_share:
                // Automatic no code here
                return true;
            case R.id.menu_item_help:
                intent = new Intent(this, Help.class);
                startActivity(intent);
                return true;
            case R.id.menu_item_about:
                intent = new Intent(this, About.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    public static class AlertDialogFragment extends DialogFragment {

        public static AlertDialogFragment newInstance(int title, int message) {
            AlertDialogFragment frag = new AlertDialogFragment();
            Bundle args = new Bundle();
            args.putInt(KEY_DIALOG_TITLE, title);
            args.putInt(KEY_DIALOG_MESSAGE, message);
            frag.setArguments(args);
            return frag;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int title = getArguments().getInt(KEY_DIALOG_TITLE);
            int message = getArguments().getInt(KEY_DIALOG_MESSAGE);

            return new AlertDialog.Builder(getActivity())
                    .setIcon(R.drawable.ic_dialog_alert_holo_light)
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton(R.string.alert_dialog_ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    ((OneClock)getActivity()).doPositiveClick();
//                                    getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, null);
                                }
                            }
                    )
                    .setNegativeButton(R.string.alert_dialog_cancel,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    ((OneClock)getActivity()).doNegativeClick();
//                                    getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_CANCELED, null);
                                }
                            }
                    )
                    .create();
        }
    }

/*
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK)
            onBackPressed();
    }
*/

    public void doPositiveClick() {
        onBackPressed(); //close oneclock
    }
    public void doNegativeClick() {
        //remain in OneClock activity
    }
/*
    public static class YesNoDialog extends DialogFragment
    {
        public YesNoDialog()
        {

        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState)
        {
            Bundle args = getArguments();
            String title = args.getString("title", "");
            String message = args.getString("message", "Message not set");

            return new AlertDialog.Builder(getActivity())
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, null);
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_CANCELED, null);
                        }
                    })
                    .create();
        }
    }
*/
    public void doShare(Intent shareIntent) {
        // When you want to share set the share intent.
        _shareActionProvider.setShareIntent(shareIntent);
    }

    private void saveResultsToDisk() {
        try {
            writeFile(createOutput());
            Toast t = Toast.makeText(this, "File saved to documents", Toast.LENGTH_SHORT);
            t.show();
        } catch (IOException ex) {
            Log.e(LOG_TAG, ex.getMessage());
            Toast t = Toast.makeText(this, String.format("Could not save file to documents folder.\n%s", ex.getMessage()), Toast.LENGTH_SHORT);
            t.show();
        }
    }
/*
Blinking button
                Animation anim = new AlphaAnimation(0.0f, 1.0f);
                anim.setDuration(50); //You can manage the blinking time with this parameter
                anim.setStartOffset(20);
                anim.setRepeatMode(Animation.REVERSE);
                anim.setRepeatCount(Animation.INFINITE);
                b.startAnimation(anim);
*/

    private String createOutput() {
        StringBuilder sb = new StringBuilder();
        TimeZone timezone = TimeZone.getDefault();

        //Formatting a date needs a timezone - otherwise the date get formatted to your system time zone.
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        formatter.setTimeZone(timezone);
        Date now = new Date();
        sb.append(String.format("Swimtimer %s\n\n", formatter.format(now)));
        for (long time : _listOfTimes)
            sb.append(String.format("%s\n", DisplayTimerStringHundredsCondensed(time)));

        return sb.toString();
    }
    private void startOrSplit() {
        long currentTime = System.currentTimeMillis();

        if (!_timerRunning) {
            startTimer(currentTime);

        } else {
            updateList(currentTime);
        }
    }

    private void startTimer(long currentTime) {
        if (findViewById(R.id.one_clear).getVisibility() == View.GONE) {
            _startTime = currentTime;
            _listOfTimes.add((long) 0);
            _adapter.notifyItemInserted(0);
        } else {
            _startTime = _startTime + (System.currentTimeMillis() - _stopTime);
        }
        Button b = (Button) findViewById(R.id.one_stop);
        b.setVisibility(View.VISIBLE);
        _timerRunning = true;
        _timerHandler.postDelayed(timerStopWatchUpdater, 0);
    }

    private void stopTimer() {
        _timerRunning = false;
        _timerHandler.removeCallbacksAndMessages(null);
        _stopTime = System.currentTimeMillis();
        Button b = (Button) findViewById(R.id.one_start);
        b.setText(getString(R.string.Start));
        b = (Button) findViewById(R.id.one_clear);
        b.setVisibility(View.VISIBLE);
        b = (Button) findViewById(R.id.one_stop);
        b.setVisibility(View.GONE);
        if (_listOfTimes.size() > 0) {
            if (_listOfTimes.get(0) != 0) { //avoid double ---- lines
                _listOfTimes.add(0, (long) 0);
                _adapter.notifyItemInserted(0);
                _recyclerView.scrollToPosition(0);
            }
        }
    }
    private void clearAll() {
        Button b = (Button) findViewById(R.id.one_clear);
        b.setVisibility(View.GONE);
        b = (Button) findViewById(R.id.one_stop);
        b.setVisibility(View.GONE);

        TextView tv = (TextView) findViewById(R.id.one_tvStopWatch);
        tv.setText(DisplayTimerString(0));
        tv = (TextView) findViewById(R.id.one_tvShowLatest);
        tv.setText(DisplayTimerString(0));
        _listOfTimes.clear();
        _adapter.notifyDataSetChanged();
    }

    private void updateList(long currentTime) {
        TextView tv = (TextView) findViewById(R.id.one_tvShowLatest);
        tv.setText(DisplayTimerString(currentTime - _startTime));
        _listOfTimes.add(0, currentTime - _startTime);

        _adapter.notifyItemInserted(0);
        _recyclerView.scrollToPosition(0);
    }

    public class ResultsArrayAdapter extends RecyclerView.Adapter<ResultsArrayAdapter.ViewHolder> {
        private ArrayList<Long> items;

        public ResultsArrayAdapter(ArrayList<Long> values) {
            this.items = values;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            private TextView time;

            public ViewHolder(View root) {
                super(root);
                time  = (TextView) root.findViewById(R.id.lvItemText1);
            }
        }
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_result_item, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            writeSysOut(DisplayTimerStringHundredsCondensed(this.items.get(position)));
            if (items.get(position) == 0)
               holder.time.setText("------------");
            else
               holder.time.setText(DisplayTimerStringHundredsCondensed(items.get(position)));
        }

        @Override
        public int getItemCount() {
            return items != null ? items.size() : 0;
        }
    }

}
