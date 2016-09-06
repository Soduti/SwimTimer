package se.soduti.swimtimer;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

//import com.facebook.FacebookSdk;

public class About extends AppCompatActivity {

    private final String _swishPackageName = "se.bankgirot.swish";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        writeSysOut("===== " + this.getClass().getSimpleName() + "." + new Object() {
//        }.getClass().getEnclosingMethod().getName() + " =====");

        super.onCreate(savedInstanceState);
//        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_about);

        TextView tv = (TextView) findViewById(R.id.about_version);
        tv.setText(String.format(getString(R.string.version), BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE));

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_menu_back_white);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        ImageButton ib = (ImageButton) findViewById(R.id.swish_image);
        ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSwishAppInstalled(getBaseContext(), _swishPackageName)) {
                    Intent launchIntent = getPackageManager().getLaunchIntentForPackage(_swishPackageName);
                    startActivity(launchIntent);
                }
            }
        });
/*
        ImageButton ib = (ImageButton) findViewById(R.id.fb_image);
        ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_SEND, Uri.parse("http:www.facebook.com"));
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_TEXT, "Swimtimer is great!");
                    startActivity(intent);
            }
        });
*/
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true); //this will show a back button but it does not behave as "back" on android but "up"
    }
    protected boolean isSwishAppInstalled(Context context, String SwishPackageName) {
        boolean isSwishInstalled = false;
        try {
            context.getPackageManager().getApplicationInfo(SwishPackageName, 0);
            isSwishInstalled = true;
        } catch (PackageManager.NameNotFoundException e) {
            Toast t = Toast.makeText(this, "Swish not present on your device", Toast.LENGTH_SHORT);
            t.show();
        }
        return isSwishInstalled ;
    }
}
