package se.soduti.swimtimer;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

import static se.soduti.swimtimer.CommonFunctions.createTextResult;
import static se.soduti.swimtimer.CommonFunctions.writeFile;
import static se.soduti.swimtimer.CommonFunctions.writeSysOut;

public class MainActivity extends AppCompatActivity implements StopWatchFragment.OnSaveStateListener { //}, ResultsFragment.OnGetSwimStateListener {

    private static final int FRAGMENT_POSITION_INFO = 0;
    private static final int FRAGMENT_POSITION_STOPWATCH = 1;
    private static final int FRAGMENT_POSITION_RESULTS = 2;
    private static final String KEY_NUMBER_OF_LANES = "number_of_lanes";
    private static final String KEY_LANE_NAMES = "lane_names";
    private static final String KEY_SWIMSTATE = "swimstate";
    private static final String KEY_MODE = "mode";
    private static final String LOG_TAG = "SwimTimerLog";
    private static final int MODE_INCREMENT = 0;
    private static final int MODE_INDIVIDUAL = 1;

    private int _fragmentBackStack = FRAGMENT_POSITION_STOPWATCH;
    private SwimState _swimState;
    private ArrayList<String> _laneNames;
    private int _numberOfLanes = 5; // Lane 0 is maincounter and then Four normal lanes =5 as a standard setup!
    private android.support.v7.widget.ShareActionProvider _shareActionProvider;
    private int _mode = MODE_INDIVIDUAL; //default to individual timing
    private SectionsPagerAdapter _SectionsPagerAdapter;

    /**
     * The ViewPager that will host the section contents.
     */
    private ViewPager _ViewPager;

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        writeSysOut("===== " + this.getClass().getSimpleName() + "." + new Object() {
        }.getClass().getEnclosingMethod().getName() + " ===== MainActivity killed saving =====");

        // Save the SwimState
        savedInstanceState.putInt(KEY_NUMBER_OF_LANES, _numberOfLanes);
        savedInstanceState.putStringArrayList(KEY_LANE_NAMES, _laneNames);
        savedInstanceState.putSerializable(KEY_SWIMSTATE, _swimState);
        savedInstanceState.putInt(KEY_MODE, _mode);
        // Always call the superclass so it can save the view hierarchy
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        writeSysOut("===== " + this.getClass().getSimpleName() + "." + new Object() {
        }.getClass().getEnclosingMethod().getName() + " =====");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(KEY_NUMBER_OF_LANES)) {
                _numberOfLanes = savedInstanceState.getInt(KEY_NUMBER_OF_LANES);
            }
            if (savedInstanceState.containsKey(KEY_SWIMSTATE)) {
                _swimState = (SwimState)savedInstanceState.getSerializable(KEY_SWIMSTATE);
            }
            if (savedInstanceState.containsKey(KEY_LANE_NAMES)) {
                _laneNames = savedInstanceState.getStringArrayList(KEY_LANE_NAMES);
            }
            if (savedInstanceState.containsKey(KEY_MODE)) {
                _mode = savedInstanceState.getInt(KEY_MODE);
            }
        }

        // Toolbar implementation
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        _SectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        _ViewPager = (ViewPager) findViewById(R.id.container);
        _ViewPager.setAdapter(_SectionsPagerAdapter);
        // Set the stopwatch fragment as startup fragment!
        _ViewPager.setCurrentItem(FRAGMENT_POSITION_STOPWATCH);

        // region * methods activated by swiping
        _ViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(final int i, final float v, final int i2) {
//                writeSysOut("===== " + this.getClass().getSimpleName() + "." + new Object(){}.getClass().getEnclosingMethod().getName() + " ***** i=" + i + "  offset=" + v);
/*
                IFragment fragment = (IFragment) _SectionsPagerAdapter.instantiateItem(_ViewPager, i);
                if (fragment != null) {
                    fragment.fragmentPageScrolled(i);
                }
*/
            }

            @Override
            public void onPageSelected(final int i) {
                IFragment fragment = (IFragment) _SectionsPagerAdapter.instantiateItem(_ViewPager, i);
                if (fragment != null) {
                    fragment.fragmentBecameVisible(_fragmentBackStack);
                }
                if (_fragmentBackStack == FRAGMENT_POSITION_INFO) {
                    InfoFragment infoFragment = (InfoFragment) _SectionsPagerAdapter.instantiateItem(_ViewPager, FRAGMENT_POSITION_INFO);
                    infoFragment.saveNames();
                    _laneNames = infoFragment.getLaneNames();
                    StopWatchFragment swFragment = (StopWatchFragment) _SectionsPagerAdapter.instantiateItem(_ViewPager, FRAGMENT_POSITION_STOPWATCH);
                    swFragment.refreshLaneNames(_laneNames);
                }
                _fragmentBackStack = i;
            }

            @Override
            public void onPageScrollStateChanged(final int i) {
//                writeSysOut("===== " + this.getClass().getSimpleName() + "." + new Object(){}.getClass().getEnclosingMethod().getName() + " ***** onPageScrollStateChanged =" + i);
//                SCROLL_STATE_IDLE     0
//                SCROLL_STATE_DRAGGING 1
//                SCROLL_STATE_SETTLING 2
            }
        });
     }

    // endregion
    public void composeEmail(String[] addresses, String subject) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, addresses);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, subject);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }
    public void keepScreenOn() {
        writeSysOut("===== " + this.getClass().getSimpleName() + "." + new Object() {
        }.getClass().getEnclosingMethod().getName());
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
    public void clearScreenOn() {
        writeSysOut("===== " + this.getClass().getSimpleName() + "." + new Object() {
        }.getClass().getEnclosingMethod().getName());
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    /**
     * Add one lane to the end of lane list
     * Inform the fragments of the addition
     */
    private void addLane() {

        //int index = _ViewPager.getCurrentItem();
        _numberOfLanes++;

        InfoFragment infoFragment = (InfoFragment) _SectionsPagerAdapter.instantiateItem(_ViewPager, FRAGMENT_POSITION_INFO);
        infoFragment.addLane();
        ResultsFragment resultsFragment = (ResultsFragment) _SectionsPagerAdapter.instantiateItem(_ViewPager, FRAGMENT_POSITION_RESULTS);
        resultsFragment.addLane();
        StopWatchFragment stopWatchFragment = (StopWatchFragment) _SectionsPagerAdapter.instantiateItem(_ViewPager, FRAGMENT_POSITION_STOPWATCH);
        stopWatchFragment.addLane();

    }

    /**
     *  Remove last lane
     *  Inform the fragments of the addition
     */
    private void removeLane() {

        // Can't remove more than down to main and one more lane!
        if (_numberOfLanes == 2)
            return;

        _numberOfLanes--;

        InfoFragment infoFragment = (InfoFragment) _SectionsPagerAdapter.instantiateItem(_ViewPager, FRAGMENT_POSITION_INFO);
        infoFragment.removeLane();
        ResultsFragment resultsFragment = (ResultsFragment) _SectionsPagerAdapter.instantiateItem(_ViewPager, FRAGMENT_POSITION_RESULTS);
        resultsFragment.removeLane();
        StopWatchFragment stopWatchFragment = (StopWatchFragment) _SectionsPagerAdapter.instantiateItem(_ViewPager, FRAGMENT_POSITION_STOPWATCH);
        stopWatchFragment.removeLane();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        //Set correct mode
        if (_mode == MODE_INDIVIDUAL) {
            (menu.findItem(R.id.menu_item_switch_activity)).setTitle(getString(R.string.mode_switch_increment));
        } else {
            (menu.findItem(R.id.menu_item_switch_activity)).setTitle(getString(R.string.mode_switch_individual));
        }

        // Fetch and store ShareActionProvider
        MenuItem item = menu.findItem(R.id.menu_item_share);
        Intent shareIntent;
        _shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        if (_shareActionProvider != null) {
            shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "No recorded laps found.");
            _shareActionProvider.setShareIntent(shareIntent);
/*
if emailing, use this
            shareIntent.setAction(Intent.ACTION_SENDTO);
            shareIntent.setData(Uri.parse("mailto:")); // only email apps should handle this
            //shareIntent.setType("message/rfc822"); //Email apps only
            shareIntent.putExtra(Intent.EXTRA_EMAIL, addresses);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
            shareIntent.putExtra(Intent.EXTRA_TEXT, "body text" );
*/
        }
        // Use custom history for share dropdown
        //_shareActionProvider.setShareHistoryFileName("custom_share_history.xml");
        return true;
    }
    public void doShare(Intent shareIntent) {
        // When you want to share set the share intent.
        _shareActionProvider.setShareIntent(shareIntent);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        Intent intent;

        switch (id) {
            case R.id.menu_item_switch_activity:
                intent = new Intent(this, OneClock.class);
                startActivity(intent);
                return true;
            case R.id.menu_item_add:
                addLane();
                return true;
            case R.id.menu_item_remove:
                removeLane();
                return true;
            case R.id.menu_item_save:
//                Toast t = Toast.makeText(this, "Not implemented yet", Toast.LENGTH_SHORT);
//                t.show();
                saveResultsToDisk();
                return true;
            case R.id.menu_item_mode:
                if (_mode == MODE_INCREMENT) {
                    item.setTitle(R.string.action_mode_increment);
                    _mode = MODE_INDIVIDUAL;
                    Toast t = Toast.makeText(this, getString(R.string.mode_switch_individual), Toast.LENGTH_SHORT);
                    t.show();
                } else {
                    item.setTitle(R.string.action_mode_standard);
                    _mode = MODE_INCREMENT;
                    Toast t = Toast.makeText(this, getString(R.string.mode_switch_increment), Toast.LENGTH_SHORT);
                    t.show();
                }
                // Tell stopwatch to switch mode
                changeMode();

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
/*
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
*/

        return super.onOptionsItemSelected(item);
    }
    private void changeMode() {
        //call stopWatch fragment and change mode
        StopWatchFragment stopWatchFragment = (StopWatchFragment) _SectionsPagerAdapter.instantiateItem(_ViewPager, FRAGMENT_POSITION_STOPWATCH);
        if (stopWatchFragment != null) {
            stopWatchFragment.setMode(_mode);
        }
    }
    public int getMode() {
        return _mode;
    }
    private void saveResultsToDisk() {
        try {
            writeFile(createTextResult(getSwimState()));
            Toast t = Toast.makeText(this, "File saved to documents", Toast.LENGTH_SHORT);
            t.show();
        } catch (IOException ex) {
            Log.e(LOG_TAG, ex.getMessage());
                Toast t = Toast.makeText(this, String.format("Could not save file to documents folder.\n%s", ex.getMessage()), Toast.LENGTH_SHORT);
                t.show();
        }
    }
    @Override
    public void onSaveState(SwimState state) {
        _swimState = state;
        writeSysOut("***** Activity received the State *****");
    }

    public SwimState getSwimState() {
        //called by resultsfragment to request _swimState from stopwatchfragment thru this (activity)
        SwimState state = null;
        // Creating a new instance...
        StopWatchFragment stopWatchFragment = (StopWatchFragment) _SectionsPagerAdapter.instantiateItem(_ViewPager, FRAGMENT_POSITION_STOPWATCH);
        if (stopWatchFragment != null) {
            state = stopWatchFragment.getSwimState();
            //Load _swimState with names set Lane1 etc if name is missing for a lane
            ArrayList<String> nameList = getLaneNames();
            for (int i = 0; i < _numberOfLanes; i++) {
                if (nameList.size() > i && !nameList.get(i).equals("")) {
                    state.Lanes.get(i).LaneName = nameList.get(i);
//                    writeSysOut("===== " + this.getClass().getSimpleName() + "." + new Object() {
//                    }.getClass().getEnclosingMethod().getName() + " =====" + " Name: " + i + " = " + nameList.get(i));
                } else {
                    state.Lanes.get(i).LaneName = "Lane " + i;
//                    writeSysOut("===== " + this.getClass().getSimpleName() + "." + new Object() {
//                    }.getClass().getEnclosingMethod().getName() + " =====" + " Name: " + i);
                }
            }
        }

        return state;
        // alternative solution - push _swimState to fragment
        //resultsFragment.setSwimState(_swimState);
    }

    public ArrayList<String> getLaneNames() {
        if (_laneNames == null)
            return new ArrayList<>();
        else
            return _laneNames;
    }

    public int getNumberOfLanes() {
        return _numberOfLanes;
    }

    /**
     * A FragmentPagerAdapter that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            writeSysOut("===== " + this.getClass().getSimpleName() + "." + new Object(){}.getClass().getEnclosingMethod().getName() + " =====" + " Initializing/Creating fragments " + position);
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment
            Fragment newFragment = null;
            switch (position) {
                case FRAGMENT_POSITION_INFO:
                    newFragment = InfoFragment.newInstance();
                    break;
                case FRAGMENT_POSITION_STOPWATCH:
                    newFragment = StopWatchFragment.newInstance();
                    break;
                case FRAGMENT_POSITION_RESULTS:
                    newFragment = ResultsFragment.newInstance();
                    break;
            }
            return newFragment;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }
    }
}
