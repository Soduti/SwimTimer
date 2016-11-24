package se.soduti.products.swimtimer;

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

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements StopWatchFragment.OnSaveStateListener { //}, ResultsFragment.OnGetSwimStateListener {

    private static final int FRAGMENT_POSITION_INFO = 0;
    private static final int FRAGMENT_POSITION_STOPWATCH = 1;
    private static final int FRAGMENT_POSITION_RESULTS = 2;
    private static final String KEY_NUMBER_OF_LANES = "number_of_lanes";
    private static final String KEY_LANE_NAMES = "lane_names";
    private static final String KEY_SWIMSTATE = "swimstate";
    private static final String LOG_TAG = "SwimTimerLog";

    private int fragmentBackStack = FRAGMENT_POSITION_STOPWATCH;
    private SwimState _swimState;
    private ArrayList<String> _laneNames;
    private int _numberOfLanes = 5; // Lane 0 is maincounter and then Four normal lanes =5 as a standard setup!
    private android.support.v7.widget.ShareActionProvider _shareActionProvider;
    private Intent mShareIntent;

    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The ViewPager that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        System.out.println("===== " + this.getClass().getSimpleName() + "." + new Object() {
        }.getClass().getEnclosingMethod().getName() + " ===== MainActivity killed saving =====");

        // Save the SwimState
        savedInstanceState.putInt(KEY_NUMBER_OF_LANES, _numberOfLanes);
        savedInstanceState.putStringArrayList(KEY_LANE_NAMES, _laneNames);
        savedInstanceState.putSerializable(KEY_SWIMSTATE, _swimState);
        // Always call the superclass so it can save the view hierarchy _swimState
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.out.println("===== " + this.getClass().getSimpleName() + "." + new Object() {
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
        }

        // Toolbar implementation
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        // Set the stopwatch fragment as startup fragment!
        mViewPager.setCurrentItem(FRAGMENT_POSITION_STOPWATCH);

        // region * methods activated by swiping
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(final int i, final float v, final int i2) {
//                System.out.println("===== " + this.getClass().getSimpleName() + "." + new Object(){}.getClass().getEnclosingMethod().getName() + " ***** i=" + i + "  offset=" + v);
/*
                IFragment fragment = (IFragment) mSectionsPagerAdapter.instantiateItem(mViewPager, i);
                if (fragment != null) {
                    fragment.fragmentPageScrolled(i);
                }
*/
            }

            @Override
            public void onPageSelected(final int i) {
                IFragment fragment = (IFragment) mSectionsPagerAdapter.instantiateItem(mViewPager, i);
                if (fragment != null) {
                    fragment.fragmentBecameVisible(fragmentBackStack);
                }
                if (fragmentBackStack == FRAGMENT_POSITION_INFO)
                {
                    InfoFragment infoFragment = (InfoFragment) mSectionsPagerAdapter.instantiateItem(mViewPager, FRAGMENT_POSITION_INFO);
                    infoFragment.saveNames();
                    _laneNames = infoFragment.getLaneNames();
                    StopWatchFragment swFragment = (StopWatchFragment) mSectionsPagerAdapter.instantiateItem(mViewPager, FRAGMENT_POSITION_STOPWATCH);
                    swFragment.refreshLaneNames(_laneNames);
                }
                fragmentBackStack = i;
            }

            @Override
            public void onPageScrollStateChanged(final int i) {
//                System.out.println("===== " + this.getClass().getSimpleName() + "." + new Object(){}.getClass().getEnclosingMethod().getName() + " ***** onPageScrollStateChanged =" + i);
//                SCROLL_STATE_IDLE     0
//                SCROLL_STATE_DRAGGING 1
//                SCROLL_STATE_SETTLING 2
            }
        });
/*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
*/
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
        System.out.println("===== " + this.getClass().getSimpleName() + "." + new Object() {
        }.getClass().getEnclosingMethod().getName());
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
    public void clearScreenOn() {
        System.out.println("===== " + this.getClass().getSimpleName() + "." + new Object() {
        }.getClass().getEnclosingMethod().getName());
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    /**
     * Add one lane to the end of lane list
     * Inform the fragments of the addition
     */
    private void addLane(MenuItem item) {

        //int index = mViewPager.getCurrentItem();
        _numberOfLanes++;

        InfoFragment infoFragment = (InfoFragment) mSectionsPagerAdapter.instantiateItem(mViewPager, FRAGMENT_POSITION_INFO);
        infoFragment.addLane();
        ResultsFragment resultsFragment = (ResultsFragment) mSectionsPagerAdapter.instantiateItem(mViewPager, FRAGMENT_POSITION_RESULTS);
        resultsFragment.addLane();
        StopWatchFragment stopWatchFragment = (StopWatchFragment) mSectionsPagerAdapter.instantiateItem(mViewPager, FRAGMENT_POSITION_STOPWATCH);
        stopWatchFragment.addLane();

    }

    /**
     *  Remove last lane
     *  Inform the fragments of the addition
     */
    private void removeLane(MenuItem item) {
        int index = mViewPager.getCurrentItem();

        // Can't remove more than down to main and one more lane!
        if (_numberOfLanes == 2)
            return;

        _numberOfLanes--;

        InfoFragment infoFragment = (InfoFragment) mSectionsPagerAdapter.instantiateItem(mViewPager, FRAGMENT_POSITION_INFO);
        infoFragment.removeLane();
        ResultsFragment resultsFragment = (ResultsFragment) mSectionsPagerAdapter.instantiateItem(mViewPager, FRAGMENT_POSITION_RESULTS);
        resultsFragment.removeLane();
        StopWatchFragment stopWatchFragment = (StopWatchFragment) mSectionsPagerAdapter.instantiateItem(mViewPager, FRAGMENT_POSITION_STOPWATCH);
        stopWatchFragment.removeLane();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem item = menu.findItem(R.id.menu_item_share);
        Log.d(LOG_TAG, "item=" + item.toString());
        // Fetch and store ShareActionProvider
        _shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        if (_shareActionProvider != null) {
            mShareIntent = new Intent();
            mShareIntent.setAction(Intent.ACTION_SEND);
            mShareIntent.setType("text/plain");
            mShareIntent.putExtra(Intent.EXTRA_TEXT, "No recorded laps found.");
            _shareActionProvider.setShareIntent(mShareIntent);
/*
if emailing, use this
            mShareIntent.setAction(Intent.ACTION_SENDTO);
            mShareIntent.setData(Uri.parse("mailto:")); // only email apps should handle this
            //mShareIntent.setType("message/rfc822"); //Email apps only
            mShareIntent.putExtra(Intent.EXTRA_EMAIL, addresses);
            mShareIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
            mShareIntent.putExtra(Intent.EXTRA_TEXT, "body text" );
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

        switch (id) {
            case R.id.menu_item_add:
                addLane(item);
                return true;
            case R.id.menu_item_remove:
                removeLane(item);
                return true;
            case R.id.menu_item_save:
            case R.id.menu_item_help:
                Toast t = Toast.makeText(this, "Not implemented yet", Toast.LENGTH_SHORT);
                t.show();
                return true;
            case R.id.menu_item_share:
                // Automatic no code here
                return true;
            case R.id.menu_item_about:
                Intent intent = new Intent(this, About.class);
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

    @Override
    public void onSaveState(SwimState state) {
        _swimState = state;
        System.out.println("***** Activity received the State *****");
    }

    public SwimState getSwimState() {
        //called by resultsfragment to request _swimState from stopwatchfragment thru this (activity)
        SwimState state = null;
        // Creating a new instance...
        StopWatchFragment stopWatchFragment = (StopWatchFragment) mSectionsPagerAdapter.instantiateItem(mViewPager, FRAGMENT_POSITION_STOPWATCH);
        if (stopWatchFragment != null) {
            state = stopWatchFragment.getSwimState();
            //Load _swimState with names set Lane1 etc if name is missing for a lane
            ArrayList<String> nameList = getLaneNames();
            for (int i = 0; i < _numberOfLanes; i++) {
                if (nameList.size() > i && !nameList.get(i).equals("")) {
                    state.Lanes.get(i).LaneName = nameList.get(i);
//                    System.out.println("===== " + this.getClass().getSimpleName() + "." + new Object() {
//                    }.getClass().getEnclosingMethod().getName() + " =====" + " Name: " + i + " = " + nameList.get(i));
                } else {
                    state.Lanes.get(i).LaneName = "Lane " + i;
//                    System.out.println("===== " + this.getClass().getSimpleName() + "." + new Object() {
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
            return new ArrayList<String>();
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
            System.out.println("===== " + this.getClass().getSimpleName() + "." + new Object(){}.getClass().getEnclosingMethod().getName() + " =====" + " Initializing/Creating fragments " + position);
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment
            Fragment newFragment = null;
            switch (position) {
                case FRAGMENT_POSITION_INFO:
                    newFragment = InfoFragment.newInstance(position + 1);
                    break;
                case FRAGMENT_POSITION_STOPWATCH:
                    newFragment = StopWatchFragment.newInstance(position + 1);
                    break;
                case FRAGMENT_POSITION_RESULTS:
                    newFragment = ResultsFragment.newInstance(position + 1);
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
