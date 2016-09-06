package se.soduti.swimtimer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

public class Help extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_menu_back_white);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        TextView tv;
        tv = (TextView)findViewById(R.id.help_p1);
        tv.setText(Html.fromHtml(getString(R.string.help_main_text_part_1)));
        tv = (TextView)findViewById(R.id.help_p2);
        tv.setText(Html.fromHtml(getString(R.string.help_main_text_part_2)));
        tv = (TextView)findViewById(R.id.help_p3);
        tv.setText(Html.fromHtml(getString(R.string.help_one)));
        tv = (TextView)findViewById(R.id.help_p4);
        tv.setText(Html.fromHtml(getString(R.string.help_increment)));
        tv = (TextView)findViewById(R.id.help_addlane);
        tv.setText(Html.fromHtml(getString(R.string.help_addlane)));
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //up...
        tv = (TextView)findViewById(R.id.help_share);
        tv.setText(Html.fromHtml(getString(R.string.help_share)));
        tv = (TextView)findViewById(R.id.help_names);
        tv.setText(Html.fromHtml(getString(R.string.help_names)));
    }
}
