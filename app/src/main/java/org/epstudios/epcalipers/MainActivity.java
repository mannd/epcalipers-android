package org.epstudios.epcalipers;

import android.graphics.Point;
import android.graphics.PorterDuff;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;

import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ToggleButton;

import com.ortiz.touch.TouchImageView;


public class MainActivity extends ActionBarActivity {
    private TouchImageView imageView;
    private View calipersView;
    private Toolbar menuToolbar;
    private Toolbar actionBar;
    private boolean calipersMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (TouchImageView) findViewById(R.id.img);
        calipersView = findViewById(R.id.calipersView);


        actionBar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(actionBar);

        menuToolbar = (Toolbar)findViewById(R.id.menu_toolbar);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        Log.d("EPS", "width = " + width + " height = " + height);

        calipersMode = true;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            changeSettings();
            return true;
        }
        if (id == R.id.action_switch) {
            toggleMode();
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    private void toggleMode() {
        calipersMode = !calipersMode;
        if (calipersMode) {
            actionBar.setTitle("EP Calipers");

        }
        else
            actionBar.setTitle("Image Mode");
    }

    private void changeSettings() {

    }
}


