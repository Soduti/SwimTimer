package se.soduti.products.swimtimer;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;

import static se.soduti.products.swimtimer.CommonFunctions.DisplayTimerStringHundreds;
import static se.soduti.products.swimtimer.CommonFunctions.getScreenOrientation;

/**
 * Created by Larsi on 2016-02-17.
 * List all laptimes
 */
public class ResultsFragment extends Fragment implements IFragment {

    private static final int TEXT_SIZE= 18;
    private static final int WIDTH_SIZE= 120;
    private static final int WIDTH_SIZE_LAPNUMBER= 100;


    SwimState _swimState;
    View _rootView;

    public ResultsFragment() {
    }

    /**
     * Returns a new instance of this fragment
     *
     */
    // TODO: 2016-03-15 remove sectionnumber
    public static ResultsFragment newInstance(int sectionNumber) {
        ResultsFragment fragment = new ResultsFragment();
        return fragment;
    }

    public class RowModel {
        public ArrayList<String> Times = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        System.out.println("===== " + this.getClass().getSimpleName() + "." + new Object() {
        }.getClass().getEnclosingMethod().getName() + " =====");

        View rootView = inflater.inflate(R.layout.fragment_results, container, false);
        _rootView = rootView;

        return rootView;
    }

    /**
     * CUSTOMIZE ROWS for listviews
     */
    public class ResultsArrayAdapter extends ArrayAdapter {
        private final Context context;
        private ArrayList<RowModel> rows;

        public ResultsArrayAdapter(Context context, ArrayList<RowModel> values) {
            super(context, R.layout.list_result_item, values);
            this.context = context;
            this.rows = values;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View rowView = inflater.inflate(R.layout.list_result_item, parent, false);

            //int numberOfLanes = ((MainActivity) getActivity()).getNumberOfLanes();
            LinearLayout rowParent = (LinearLayout)rowView.findViewById(R.id.listResultItem);

            LinearLayout.LayoutParams paramsNoWeight = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            LinearLayout.LayoutParams paramsWeight = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
            LinearLayout.LayoutParams params;

            TextView tv = new TextView(getContext());

            // Set lap number into first column
            tv.setText(Integer.toString(position + 1));
            params = paramsNoWeight;
            tv.setWidth(WIDTH_SIZE);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, TEXT_SIZE);
            tv.setLayoutParams(params);
            tv.setGravity(View.TEXT_ALIGNMENT_CENTER);
            rowParent.addView(tv);
            // Add columns
//            System.out.println("GetView position=" + position);
            for (int laneIdx = 0; laneIdx < this.rows.get(position).Times.size(); laneIdx++) {
                tv = new TextView(getContext());
//                System.out.println("GetView laneidx=" + laneIdx);
                // Set lap time
                tv.setText(this.rows.get(position).Times.get(laneIdx));
                params = paramsWeight;
                tv.setWidth(0);
                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, TEXT_SIZE);
                tv.setLayoutParams(params);
                tv.setGravity(View.TEXT_ALIGNMENT_CENTER);
                rowParent.addView(tv);
            }

            return rowView;
        }
    }

    public void populateLists() {
        //System.out.println("===== " + this.getClass().getSimpleName() + "." + new Object() {        }.getClass().getEnclosingMethod().getName());

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
            //System.out.println("===== laneidx=" + laneIdx + " lanestop=" + laneStopForSection);

            // Get a subset of SwimState and only show a limited amount of lanes (4/6/etc)
            //Loop every row and subset the lanes to show for each row

            // Add header
            tl.addView(addHeaderTitles(laneIdx, laneStopForSection));

            // Add rows
            ArrayList<RowModel> rowModelList = loadRowModelList(laneIdx, laneStopForSection);
            for (int lapIdx = 0; lapIdx < rowModelList.size(); lapIdx++) {
                //System.out.println("laps" + lapIdx);
                TableRow tr = new TableRow(this.getContext());

                /*
                Create tablerows to hold TextViews that show lap time
                first column is lap number
                */

                TextView tv = new TextView(this.getContext());
                tv.setText(String.format("%d", lapIdx + 1));
                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, TEXT_SIZE);
                tv.setLayoutParams(paramsNoWeight);
                tv.setGravity(Gravity.LEFT);
                tr.addView(tv);
                // Create TextViews (one per lane) to show lap time, row for row
                int laneStopForRow = rowModelList.get(lapIdx).Times.size();
                for (int idxLane = 0; idxLane < laneStopForRow; idxLane++) {
                    //System.out.println("populatelists row " + lapIdx + " lane " + idxLane);
                    tv = new TextView(this.getContext());
                    tv.setText(String.format("%s", rowModelList.get(lapIdx).Times.get(idxLane)));
                    tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, TEXT_SIZE);
                    tv.setGravity(Gravity.RIGHT);
                    paramsNoWeight.setMargins(0, 0, 20, 0);
                    tv.setLayoutParams(paramsNoWeight);
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
        tv.setGravity(Gravity.LEFT);
        tv.setLayoutParams(paramsNoWeight);
        tv.setLines(1);
        tr.addView(tv);
        for (int laneIdx = laneStart; laneIdx <= laneStop; laneIdx++) {
//            System.out.println("head laneidx=" + laneIdx);
            tv = new TextView(this.getContext());
            if (_swimState.Lanes.get(laneIdx).LaneName.equals(""))
                tv.setText(String.format("%s %d", getString(R.string.Lane), laneIdx));
            else
                tv.setText(_swimState.Lanes.get(laneIdx).LaneName);

            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, TEXT_SIZE);
            tv.setGravity(Gravity.CENTER);
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
        tv.setGravity(Gravity.LEFT);
        tv.setLayoutParams(paramsNoWeight);
        tv.setLines(1);
        tr.addView(tv);
        for (int laneIdx = laneStart; laneIdx <= laneStop; laneIdx++) {
//            System.out.println("===== foot laneidx=" + laneIdx);
            tv = new TextView(getContext());
            tv.setText(DisplayTimerStringHundreds(getTotalTime(laneIdx)));
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, TEXT_SIZE);
            tv.setGravity(Gravity.RIGHT);
            paramsNoWeight.setMargins(0, 0, 20, 0);
            tv.setLayoutParams(paramsNoWeight);
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
        v.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorDarkSoft));
        v.setMinimumHeight(2);
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

    private long getTotalTime(int laneIdx) {
        Lane lane = _swimState.Lanes.get(laneIdx);
        if (lane.Laps.size() > 0)
            return lane.Laps.get(lane.Laps.size() - 1) - lane.StartTime;
        else
            return 0;
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
            for (int laneIdx = laneStart; laneIdx <= laneStop; laneIdx++)
                if (_swimState.Lanes.get(laneIdx).Laps.size() > lapIdx) {
                    rowModel.Times.add(calculateLaptime(laneIdx, lapIdx));
                    //System.out.println("add lane=" + laneIdx + " lap=" + lapIdx);
                } else {
                    rowModel.Times.add("");
                }

            //System.out.println("rowModelList.add(rowModel) lap=" + lapIdx);
            rowModelList.add(rowModel);
        }

        return rowModelList;
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

    // region IFragment
    @Override
    public void fragmentBecameVisible(int previousFragment) {
        //RELOAD LISTS
        System.out.println("===== " + this.getClass().getSimpleName() + "." + new Object(){}.getClass().getEnclosingMethod().getName() + " =====");
        //get the swimstate from activity
        _swimState = ((MainActivity)getActivity()).getSwimState();
        populateLists();
    }

    @Override
    public void fragmentPageScrolled(int scrollState) {
        //System.out.println("fragmentPageScrolled results");
    }

    @Override
    public void addLane() {
        // TODO: 2016-03-18 rewrite fragment? or create another method for refreshing data
    }

    @Override
    public void removeLane() {
        // TODO: 2016-03-18 same as above
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

//    @Override
//    public void fragmentPageScrollStateChanged(int scrollState) {
//        System.out.println("fragmentPageScrollStateChanged results");
//    }

//not needed to save _swimState from info fragment
//    @Override
//    public void onSaveInstanceState(Bundle savedInstanceState) {
//        // Save the SwimState
//        savedInstanceState.putSerializable(SWIMSTATE_KEY, _swimState);
//
//        // Always call the superclass so it can save the view hierarchy _swimState
//        super.onSaveInstanceState(savedInstanceState);
//
//        System.out.println("***** onSAVEinstanceSTATE fragment results *****");
//
//    }
}
/** populate adapter for listview
 public void populateLists() {
 ArrayList<RowModel> rowModelList = loadRowModelList();

 if (rowModelList.size() == 0)
 return;

 LinearLayout ll = (LinearLayout) _rootView.findViewById(R.id.linearLayoutResults);
 // Remove listviews for reload!
 ll.removeAllViews();

 // Prepare adapter with rowdata
 ArrayList<RowModel> rows = new ArrayList<>();

 // Get a subset of rowModelList and only show a limited amount of lanes (4/6/etc)
 //Loop every row and subset the lanes to show for each ListView
 for (int r=0; r<rowModelList.size(); r++) {
 RowModel row = new RowModel();
 int laneMaxForSection = (sectionIdx * maxWidthCount) + currentSectionColumnCount;
 for (int idxLane = blockIdx; idxLane < laneMaxForSection; idxLane++) {
 //if (i < rowModelList.get(r).Times.size()) {
 row.Times.add(rowModelList.get(r).Times.get(idxLane));
 }
 rows.add(row);
 }

 ResultsArrayAdapter resultsArrayAdapter = new ResultsArrayAdapter(this.getContext(), rows);
 ListView lv = new ListView(this.getContext());
 lv.setAdapter(resultsArrayAdapter);

 // rebuild the header with number of lanes... first remove it and then build it up
 LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 if (lv.getHeaderViewsCount() > 0){
 lv.removeHeaderView(_rootView.findViewById(R.id.headerView));
 }
 View header = inflater.inflate(R.layout.list_result_header, null, false);
 addHeaderTitles(header);
 lv.addHeaderView(header);

 // remove footer to update it...and then add it again :)
 if (lv.getFooterViewsCount() > 0) {
 lv.removeFooterView(_rootView.findViewById(R.id.footerView));
 }
 View footer = inflater.inflate(R.layout.list_result_footer, null, false);
 addFooterTotals(footer);
 lv.addFooterView(footer);

 // Add listview to ll
 ll.addView(lv);
 }
 */
