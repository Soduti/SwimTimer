package se.soduti.swimtimer;

import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.InputFilter;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.util.ArrayList;

import static se.soduti.swimtimer.CommonFunctions.dpToPx;
import static se.soduti.swimtimer.CommonFunctions.getScreenOrientation;
import static se.soduti.swimtimer.CommonFunctions.writeSysOut;

/**
 * Created by Larsi on 2016-02-17.
 * Lane texts and possibility to set Lane texts
 */
public class InfoFragment extends Fragment implements IFragment {
    private static final String KEY_LANE_NAMES = "lane_names";
    private static final int POSITION_NAME = 0;

    private View _rootView;
    private ArrayList<String> _laneNames;

    public InfoFragment() {
    }

    /**
     * Returns a new instance of this fragment
     */
    public static InfoFragment newInstance() {
        return new InfoFragment();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        writeSysOut("===== " + this.getClass().getSimpleName() + "." + new Object() {
        }.getClass().getEnclosingMethod().getName() + " =====");

        // If in edit mode make sure to save the currently edited line
        if (_rootView != null) {
            SaveLaneNames();

            // Save the names to _swimState
            savedInstanceState.putStringArrayList(KEY_LANE_NAMES, _laneNames);

            // Always call the superclass so it can save the view hierarchy _swimState
            super.onSaveInstanceState(savedInstanceState);

            writeSysOut("===== " + this.getClass().getSimpleName() + "." + new Object() {
            }.getClass().getEnclosingMethod().getName() + " ===== _rootview not null so saving instancestate");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        writeSysOut("===== " + this.getClass().getSimpleName() + "." + new Object() {
        }.getClass().getEnclosingMethod().getName() + " =====");

        super.onCreate(savedInstanceState);

        if (savedInstanceState != null && savedInstanceState.containsKey(KEY_LANE_NAMES)) {
            writeSysOut("===== " + this.getClass().getSimpleName() + "." + new Object() {
            }.getClass().getEnclosingMethod().getName() + " ===== GET NAMES FROM SAVED INSTANCE STATE");
            _laneNames = savedInstanceState.getStringArrayList(KEY_LANE_NAMES);
        } else {
            writeSysOut("===== " + this.getClass().getSimpleName() + "." + new Object() {
            }.getClass().getEnclosingMethod().getName() + " ===== GET NAMES FROM MainActivity");
            _laneNames = ((MainActivity) getActivity()).getLaneNames();
        }

        View rootView = inflater.inflate(R.layout.fragment_info, container, false);
        _rootView = rootView;

        BuildFragment();

        Button btnResetLaneNames = (Button) rootView.findViewById(R.id.btnResetLaneNames);
        btnResetLaneNames.setText(getString(R.string.ResetLaneNames));
        btnResetLaneNames.setOnClickListener(onClickResetLaneNameButton);

        return rootView;
    }

    private void BuildFragment() {
        int numberOfLanes = ((MainActivity) getActivity()).getNumberOfLanes();
        LinearLayout parent = (LinearLayout) _rootView.findViewById(R.id.ll_names);
        parent.removeAllViews();
        // Build lane name fields
        for (int laneNumber = 0; laneNumber < numberOfLanes; laneNumber++) {
            BuildRow(laneNumber);
        }

        ResizeScreen();

/*
// TODO: 2016-03-21  
        Button btnEdit = (Button) _rootView.findViewById(R.id.btnEditLaneNames);
        btnEdit.requestFocus();
*/
/*
        // TODO: 2016-03-21 test on something else since btn _swimState is not saved...
        Button btn = ((Button) _rootView.findViewById(R.id.btnEditLaneNames));
        if (btn.getText() != getString(R.string.EditLaneNamesSave)) { //can be empty then keep it disabled
            EditText et = (EditText) _rootView.findViewWithTag("Lane1");
            et.requestFocus();
        } else
            btn.requestFocus();
*/
    }

    private void BuildRow(int laneNumber) {
        LinearLayout parent = (LinearLayout) _rootView.findViewById(R.id.ll_names);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 5, 2);
        // Build lane name fields

        LinearLayout ll_0 = new LinearLayout(getContext()); //use redundant linelayout for spacing just to match sw fragment layout
        ll_0.setLayoutParams(params);
        ll_0.setGravity(Gravity.CENTER);
        ll_0.setTag(laneNumber);
        ll_0.setFocusableInTouchMode(true); //makes the soft keyboard hide...

        EditText et = new EditText(getContext());
        int maxLength = 10;
        InputFilter[] FilterArray = new InputFilter[1];
        FilterArray[0] = new InputFilter.LengthFilter(maxLength);
        et.setFilters(FilterArray);

        et.setLines(1);
        et.setTag(String.format("Lane%d", laneNumber));
        if (laneNumber == 0)
            et.setHint(getString(R.string.Base));
        else
            et.setHint(String.format("%s %d", getString(R.string.Lane), laneNumber));

        if (_laneNames != null && laneNumber < _laneNames.size()) {
            //writeSysOut("lanename: " + _laneNames.get(laneNumber).toString());
            et.setText(_laneNames.get(laneNumber));
        }

//        Button btn = ((Button) _rootView.findViewById(R.id.btnEditLaneNames));
//        writeSysOut("===== " + this.getClass().getSimpleName() + "." + new Object(){}.getClass().getEnclosingMethod().getName() + " ===== Button text=" + btn.getText());
//        if (btn.getText() != getString(R.string.EditLaneNamesSave)) { //can be empty then keep it disabled
        // TODO: 2016-03-21   et.setEnabled(false);
//            et.setFocusable(false);
//        } else {
//            et.setEnabled(true);
//            et.setFocusable(true);
//        }
        et.setTextColor(Color.WHITE);
        et.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorDarkBackground));
        et.setGravity(Gravity.CENTER | Gravity.RIGHT);
        et.setHintTextColor(Color.WHITE);
        et.setPadding(0, -50, 0, -50);

        params.setMargins(0, 0, 5, 2);
        et.setLayoutParams(params);

        //et.setOnClickListener(onClickEditLaneName);

        ll_0.addView(et);
        parent.addView(ll_0);
    }

    private void ResizeScreen() {
        writeSysOut("===== " + this.getClass().getSimpleName() + "." + new Object() {
        }.getClass().getEnclosingMethod().getName() + " =====");
        int NumberOfLanes = ((MainActivity) getActivity()).getNumberOfLanes();
        int textSize;
        int timerWidth;
        int viewHeight;
        int laneNumberWidth;
        int screenOrientation = getScreenOrientation(getActivity());

        //params for buttons only (use another parameter if there is a need for changing edittexts layoutparams...)
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        LinearLayout.LayoutParams params_ll = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

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
                    textSize = 38;
                    //px
                    viewHeight = dpToPx(40);
                    timerWidth = dpToPx(130);
                    params.setMargins(0, -15, 0, -15);
                    params_ll.setMargins(0, 0, 5, 20);
                    break;
                default:
                    textSize = 30;
                    //px
                    viewHeight = dpToPx(30);
                    timerWidth = dpToPx(115);
                    params.setMargins(0, -25, 0, -25);
                    params_ll.setMargins(0, 0, 5, 2);
                    break;
            }
        } else {
            switch (NumberOfLanes) {
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                    //no need to convert to dp Texts can be typed to dp
                    textSize = 38;
                    //px
                    viewHeight = dpToPx(40);
                    timerWidth = dpToPx(130);
                    params.setMargins(0, -15, 0, -15);
                    params_ll.setMargins(0, 0, 5, 20);
                    break;
                default:
                    textSize = 30;
                    //px
                    viewHeight = dpToPx(30);
                    timerWidth = dpToPx(115);
                    params.setMargins(0, -25, 0, -25);
                    params_ll.setMargins(0, 0, 5, 2);
                    break;
            }
        }

        LinearLayout parent = (LinearLayout) _rootView.findViewById(R.id.ll_names); // Get containing block
        for (int laneIdx = 0; laneIdx < NumberOfLanes; laneIdx++) {
            LinearLayout ll_0 = (LinearLayout) parent.getChildAt(laneIdx); // Get containing linelayout
            ll_0.setLayoutParams(params_ll);
            EditText et = ((EditText) ll_0.getChildAt(POSITION_NAME));
            et.setHeight(viewHeight);
            et.setWidth(timerWidth);
            et.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSize);
        }
    }

    @Override
    public void onPause() {

        super.onPause();
        writeSysOut("===== " + this.getClass().getSimpleName() + "." + new Object() {
        }.getClass().getEnclosingMethod().getName() + " =====");
    }

    private View.OnClickListener onClickResetLaneNameButton = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Button b = (Button) v;
            writeSysOut("===== on click reset lane names =====");

            ResetEditTextViews();

/* // TODO: 2016-03-22 remove? 
            // Also set the edit/save button to edit
            Button btnEdit = (Button) _rootView.findViewById(R.id.btnEditLaneNames);
            btnEdit.setText(getString(R.string.EditLaneNames));
*/

//            int laneNumber = Integer.valueOf((String) b.getTag());
        }
    };

    /*
        private View.OnClickListener onClickEditLaneNameButton = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button b = (Button) v;
                writeSysOut("===== on click edit lane names =====");

                if (b.getText() == getString(R.string.EditLaneNames))
                    b.setText(getString(R.string.EditLaneNamesSave));
                else {
                    SaveLaneNames();
                    b.setText(getString(R.string.EditLaneNames));
                }
                ToggleEditTextViews();

            }
        };
    */
/*
    private View.OnClickListener onClickEditLaneName = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            EditText et = (EditText) v;
            writeSysOut("===== on click lane names =====");

            et.setEnabled(true);
        }
    };
*/
/*

    private void ToggleEditTextViews() {
        LinearLayout parent = (LinearLayout) _rootView.findViewById(R.id.ll_names);
        for (int i = 0; i < ((MainActivity) getActivity()).getNumberOfLanes(); i++) {
            LinearLayout ll_0 = (LinearLayout) parent.getChildAt(i); // Get containing linelayout
            EditText et = (EditText) ll_0.getChildAt(POSITION_NAME);
            et.setEnabled(!et.isEnabled());
//            et.setFocusable(!et.isEnabled());
        }
        EditText et = (EditText) _rootView.findViewWithTag("Lane1");
        if (et.isEnabled()) {
            et.requestFocus();

            InputMethodManager imm = (InputMethodManager) ((MainActivity) getActivity()).getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInputFromInputMethod(_rootView.getWindowToken(), InputMethodManager.SHOW_FORCED);
        } else {
            InputMethodManager imm = (InputMethodManager) ((MainActivity) getActivity()).getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(_rootView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
*/
    private void ResetEditTextViews() {
        LinearLayout parent = (LinearLayout) _rootView.findViewById(R.id.ll_names);
        for (int i = 0; i < ((MainActivity) getActivity()).getNumberOfLanes(); i++) {
            LinearLayout ll_0 = (LinearLayout) parent.getChildAt(i); // Get containing linelayout
            EditText et = (EditText) ll_0.getChildAt(POSITION_NAME);
            et.setText("");
//            et.setEnabled(false);
//            et.setFocusable(false);
        }
        _laneNames = new ArrayList<String>();
    }

    @Override
    public void fragmentBecameVisible(int previousFragment) {
        writeSysOut("===== " + this.getClass().getSimpleName() + "." + new Object() {
        }.getClass().getEnclosingMethod().getName() + " =====");
        LinearLayout parent = (LinearLayout) _rootView.findViewById(R.id.ll_names);
        for (int i = 0; i < ((MainActivity) getActivity()).getNumberOfLanes(); i++) {
            LinearLayout ll_0 = (LinearLayout) parent.getChildAt(i); // Get containing linelayout
            EditText et = (EditText) ll_0.getChildAt(POSITION_NAME);
            et.setEnabled(true); // this will make the soft keyboard available and open the fields for edit
        }

    }

    @Override
    public void fragmentPageScrolled(int scrollState) {
//        writeSysOut("fragmentPageScrolled info");
    }

    @Override
    public void addLane() {
        writeSysOut("===== " + this.getClass().getSimpleName() + "." + new Object() {
        }.getClass().getEnclosingMethod().getName() + " =====");

        if (_rootView == null)
            return;

        int laneNumber = ((MainActivity) getActivity()).getNumberOfLanes() - 1;
        BuildRow(laneNumber);
        ResizeScreen();
    }

    @Override
    public void removeLane() {
        int laneNumber = ((MainActivity) getActivity()).getNumberOfLanes(); //number of lanes is already -1 from calling method, so no -1 here, this will also make resize work with the new count

        if (_rootView == null)
            return;

        if (_laneNames != null && laneNumber < _laneNames.size())
            _laneNames.remove(laneNumber); // Remove the last item
        // Get containing block
        LinearLayout parent = (LinearLayout) _rootView.findViewById(R.id.ll_names);
        parent.removeViewAt(laneNumber);
        ResizeScreen();
    }

    public ArrayList<String> getLaneNames() {
        if (_laneNames == null)
            _laneNames = new ArrayList<String>();

        return _laneNames;
    }
/*
    public void setLaneNames(ArrayList<String> names) {
        _laneNames = names;
    }
*/

    /**
     * Used for call from outside fragment to force close and save names if swiping from this fragment
     */
    public void saveNames() {
        SaveLaneNames();
        LinearLayout parent = (LinearLayout) _rootView.findViewById(R.id.ll_names);
        // Set fields to enabled=false, this will close soft keyboard
        for (int i = 0; i < ((MainActivity) getActivity()).getNumberOfLanes(); i++) {
            LinearLayout ll_0 = (LinearLayout) parent.getChildAt(i);
            EditText et = (EditText) ll_0.getChildAt(POSITION_NAME);
            et.setEnabled(false);
        }
    }

    private void SaveLaneNames() {
        int NumberOfLanes = ((MainActivity) getActivity()).getNumberOfLanes();

        // Clear the old list and add strings from edit fields
        _laneNames = new ArrayList<String>();

        LinearLayout parent = (LinearLayout) _rootView.findViewById(R.id.ll_names); // Get containing block
        for (int laneIdx = 0; laneIdx < NumberOfLanes; laneIdx++) {
            LinearLayout ll_0 = (LinearLayout) parent.getChildAt(laneIdx); // Get containing linelayout
            try {
                EditText et = (EditText) ll_0.getChildAt(POSITION_NAME);
                _laneNames.add(et.getText().toString().trim());
                //writeSysOut("Names:" + ((EditText) ll_0.getChildAt(POSITION_NAME)).getText().toString());
            } catch (Exception e) {
                _laneNames.add("");
                //writeSysOut("Names: extra lane added with space");
            }
        }
    }


/*
    Metod för att loopa alla children i en view
    private void PopulateLaneName(String name, int laneIdx) {
        LinearLayout parent = (LinearLayout)_rootView.findViewById(R.id.linelayout1);
        for(int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            if (child instanceof EditText) {
                //Support for EditText
                EditText et = (EditText) child;
                if (Integer.valueOf((String)et.getTag()) ==  laneIdx) {
                    et.setText(name);
                }
            } else {
                //Support for other controls
            }
        }
    }
*/
}
