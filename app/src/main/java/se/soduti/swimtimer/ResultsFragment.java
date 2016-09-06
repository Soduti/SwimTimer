package se.soduti.swimtimer;

import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;

import static se.soduti.swimtimer.CommonFunctions.DisplayTimerStringHundreds;
import static se.soduti.swimtimer.CommonFunctions.calculateLaptime;
import static se.soduti.swimtimer.CommonFunctions.getScreenOrientation;
import static se.soduti.swimtimer.CommonFunctions.getTotalTime;
import static se.soduti.swimtimer.CommonFunctions.writeSysOut;

/**
 * Created by Larsi on 2016-02-17.
 * List all laptimes
 */
public class ResultsFragment extends Fragment implements IFragment {

    private static final int TEXT_SIZE= 18;

    SwimState _swimState;
    View _rootView;

    public ResultsFragment() {
    }

    /**
     * Returns a new instance of this fragment
     *
     */
    public static ResultsFragment newInstance() {
        return new ResultsFragment();
    }

    public class RowModel {
        public ArrayList<String> Times = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        writeSysOut("===== " + this.getClass().getSimpleName() + "." + new Object() {
        }.getClass().getEnclosingMethod().getName() + " =====");

        View rootView = inflater.inflate(R.layout.fragment_results, container, false);
        _rootView = rootView;

        return rootView;
    }

    public void populateLists() {
        //writeSysOut("===== " + this.getClass().getSimpleName() + "." + new Object() {        }.getClass().getEnclosingMethod().getName());

        // Use screen width function to create a dynamic list with 4/6/8/10 items on each line
        int numberOfLanes = ((MainActivity) getActivity()).getNumberOfLanes();
        int maxWidthCount = getOrientationWidth();

        // Clear view before reloading it!
        LinearLayout parent = (LinearLayout) _rootView.findViewById(R.id.linearLayoutResults);
        parent.removeAllViews();

        TableLayout tl = new TableLayout(this.getContext());
        TableLayout.LayoutParams paramsTable =new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT);
        TableRow.LayoutParams paramsNoWeight =new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT);
        /*TableRow.LayoutParams paramsWeight =new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT, 1.0f);*/

        paramsTable.setMargins(5, 0, 10, 0);

        tl.setLayoutParams(paramsTable);
        tl.setStretchAllColumns(true);
        tl.setColumnStretchable(0, false);

        int sectionIdx = 0;
        for (int laneIdx = 1; laneIdx < numberOfLanes; laneIdx += maxWidthCount) {

            int currentSectionColumnCount; // Handle when not even number of columns compared to maxWidthCount
            if ((sectionIdx * maxWidthCount) + maxWidthCount > (numberOfLanes - 1))
                currentSectionColumnCount = (numberOfLanes - 1) - (sectionIdx * maxWidthCount);
            else {
                currentSectionColumnCount = maxWidthCount;
            }
            int laneStopForSection = (sectionIdx * maxWidthCount) + currentSectionColumnCount;
            //writeSysOut("===== laneidx=" + laneIdx + " lanestop=" + laneStopForSection);

            // Get a subset of SwimState and only show a limited amount of lanes (4/6/etc)
            //Loop every row and subset the lanes to show for each row

            // Add header
            tl.addView(addHeaderTitles(laneIdx, laneStopForSection));

            // Add rows
            ArrayList<RowModel> rowModelList = loadRowModelList(laneIdx, laneStopForSection);
            for (int lapIdx = 0; lapIdx < rowModelList.size(); lapIdx++) {
                //writeSysOut("laps" + lapIdx);
                TableRow tr = new TableRow(this.getContext());

                /*
                Create tablerows to hold TextViews that show lap time
                first column is lap number
                */

                TextView tv = new TextView(this.getContext());
                tv.setText(String.format("%d", lapIdx + 1));
                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, TEXT_SIZE);
                tv.setLayoutParams(paramsNoWeight);
                tv.setGravity(Gravity.START);
                tv.setTextColor(Color.WHITE);
                tr.addView(tv);
                // Create TextViews (one per lane) to show lap time, row for row
                int laneStopForRow = rowModelList.get(lapIdx).Times.size();
                for (int idxLane = 0; idxLane < laneStopForRow; idxLane++) {
                    //writeSysOut("populatelists row " + lapIdx + " lane " + idxLane);
                    tv = new TextView(this.getContext());
                    tv.setText(String.format("%s", rowModelList.get(lapIdx).Times.get(idxLane)));
                    tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, TEXT_SIZE);
                    tv.setGravity(Gravity.END);
                    paramsNoWeight.setMargins(0, 0, 20, 0);
                    tv.setLayoutParams(paramsNoWeight);
                    tv.setTextColor(Color.WHITE);
                    tr.addView(tv);
                }
                tl.addView(tr);

            }
            // Add footer totals
            tl.addView(addFooterTotals(laneIdx, laneStopForSection));
            if (laneStopForSection != numberOfLanes - 1) {
                tl.addView(addMargin());
                tl.addView(addLine());
                tl.addView(addMargin());
            }
            sectionIdx++;
        }
        parent.addView(tl);
    }
    private TableRow addHeaderTitles(int laneStart, int laneStop) {
        TableRow tr = new TableRow(this.getContext());
        TableRow.LayoutParams paramsNoWeight =new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT);
        // First column is for lap numbers
        TextView tv = new TextView(this.getContext());
        tv.setText(getString(R.string.lap));
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, TEXT_SIZE);
        tv.setGravity(Gravity.START);
        paramsNoWeight.setMargins(0, 0, 0, 0);
        tv.setLayoutParams(paramsNoWeight);
        tv.setTextColor(Color.WHITE);
        tv.setLines(1);
        tr.addView(tv);
        for (int laneIdx = laneStart; laneIdx <= laneStop; laneIdx++) {
//            writeSysOut("head laneidx=" + laneIdx);
            tv = new TextView(this.getContext());
            tv.setTextColor(Color.WHITE);

            if (_swimState.Lanes.get(laneIdx).LaneName.equals(""))
                tv.setText(String.format("%s %d", getString(R.string.Lane), laneIdx));
            else
                tv.setText(_swimState.Lanes.get(laneIdx).LaneName);

            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, TEXT_SIZE);
            tv.setGravity(Gravity.END);
            paramsNoWeight.setMargins(0, 0, 20, 0);
            tv.setLayoutParams(paramsNoWeight);
            tr.addView(tv);
        }
        return tr;
    }
    private TableRow addFooterTotals(int laneStart, int laneStop) {
        TableRow tr = new TableRow(this.getContext());
        TableRow.LayoutParams paramsNoWeight =new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT);
        // First column is for text "Tot"
        TextView tv = new TextView(this.getContext());
        tv.setText(getString(R.string.Tot));
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, TEXT_SIZE);
        tv.setGravity(Gravity.START);
        paramsNoWeight.setMargins(0, 0, 0, 0);
        tv.setLayoutParams(paramsNoWeight);
        tv.setLines(1);
        tv.setTextColor(Color.WHITE);
        tr.addView(tv);
        for (int laneIdx = laneStart; laneIdx <= laneStop; laneIdx++) {
//            writeSysOut("===== foot laneidx=" + laneIdx);
            tv = new TextView(getContext());
            tv.setText(DisplayTimerStringHundreds(getTotalTime(_swimState.Lanes.get(laneIdx))));
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, TEXT_SIZE);
            tv.setGravity(Gravity.END);
            paramsNoWeight.setMargins(0, 0, 20, 0);
            tv.setLayoutParams(paramsNoWeight);
            tv.setTextColor(Color.WHITE);
            tr.addView(tv);
        }
        return tr;
    }
    private View addMargin() {
        View v = new View(this.getContext());
        v.setMinimumHeight(20);
        return v;
    }
    private View addLine() {
        View v = new View(this.getContext());
        v.setBackgroundColor(Color.WHITE);
        v.setMinimumHeight(5);
        return v;
    }

    private int getOrientationWidth() {

        // TODO: 2016-03-28 Calculate screen width!
        // Use fontsize to decide how many items to display on each row
/*
        DisplayMetrics dm = new DisplayMetrics();
        ((MainActivity)getActivity()).getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
*/

        int rotation = getScreenOrientation(getActivity());
        switch(rotation) {
            case ActivityInfo.SCREEN_ORIENTATION_PORTRAIT:
            case ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT:
                return 4;
            case ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE:
            case ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE:
                return 6;
            default:
                return 4;
        }
    }

    private ArrayList<RowModel> loadRowModelList(int laneStart, int laneStop) {
        ArrayList<RowModel> rowModelList = new ArrayList<>();
        int highest_count = 0;
        for (int laneIdx = laneStart; laneIdx <= laneStop; laneIdx++)
           if (_swimState.Lanes.get(laneIdx).Laps.size() > highest_count)
               highest_count = _swimState.Lanes.get(laneIdx).Laps.size();

        // Loop all lanes and calculate lap times and save to lapModel
        for (int lapIdx = 0; lapIdx < highest_count; lapIdx++) {
            RowModel rowModel = new RowModel();
            for (int laneIdx = laneStart; laneIdx <= laneStop; laneIdx++) {
                Lane lane = _swimState.Lanes.get(laneIdx);
                if (lane.Laps.size() > lapIdx) {
                    rowModel.Times.add(calculateLaptime(lane, lapIdx));
                    //writeSysOut("add lane=" + laneIdx + " lap=" + lapIdx);
                } else {
                    rowModel.Times.add("");
                }
            }
            //writeSysOut("rowModelList.add(rowModel) lap=" + lapIdx);
            rowModelList.add(rowModel);
        }

        return rowModelList;
    }

    // region IFragment
    @Override
    public void fragmentBecameVisible(int previousFragment) {
        //RELOAD LISTS
        writeSysOut("===== " + this.getClass().getSimpleName() + "." + new Object() {
        }.getClass().getEnclosingMethod().getName() + " =====");
        //get the swimstate from activity
        _swimState = ((MainActivity)getActivity()).getSwimState();
        populateLists();
    }

    @Override
    public void fragmentPageScrolled(int scrollState) {
        //writeSysOut("fragmentPageScrolled results");
    }

    @Override
    public void addLane() {
        // data refreshed on loadscreen
    }

    @Override
    public void removeLane() {
        // data refreshed on loadscreen
    }
    //endregion

/*
    //region Interface code for Interface methods to communicate with the Activity
    OnGetSwimStateListener mCallback;

    // Container Activity must implement this interface
    public interface OnGetSwimStateListener {
        void getSwimState(ResultsFragment fragment);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnGetSwimStateListener) context; //setup mCallback for later use
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnGetSwimStateListener");
        }
    }
    //endregion
*/

}
