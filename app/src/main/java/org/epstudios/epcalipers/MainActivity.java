package org.epstudios.epcalipers;

import android.graphics.Color;
import android.graphics.Point;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;

import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.RotateAnimation;
import android.widget.Button;

import com.ortiz.touch.TouchImageView;


public class MainActivity extends ActionBarActivity {
    static final String EPS = "EPS";
    private TouchImageView imageView;
    private CaliperView caliperView;
    private Toolbar menuToolbar;
    private Toolbar actionBar;
    private boolean calipersMode;

    // Buttons
    Button addCaliperButton;
    Button calibrateButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (TouchImageView) findViewById(R.id.imageView);
        caliperView = (CaliperView) findViewById(R.id.caliperView);


        actionBar = (Toolbar)findViewById(R.id.action_bar);
        setSupportActionBar(actionBar);

        menuToolbar = (Toolbar)findViewById(R.id.menu_toolbar);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        Log.d(EPS, "width = " + width + " height = " + height);

        calipersMode = true;
        setMode();

        createButtons();
        createMainToolbar();

    }

    private void createButtons() {
        addCaliperButton = new Button(this);
        createButton(addCaliperButton, getString(R.string.add_caliper_button_title));
        addCaliperButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectCaliperMenu();
            }
        });
        calibrateButton = new Button(this);
        createButton(calibrateButton, getString(R.string.calibrate_button_title));
        calibrateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calibrate();
            }
        });

    }

    private void createButton(Button button, String text) {
        button.setText(text);
        button.setTextColor(Color.WHITE);
    }

    private void createMainToolbar() {
        menuToolbar.addView(addCaliperButton);
        menuToolbar.addView(calibrateButton);
    }

    private void selectCaliperMenu() {
        clearToolbar();
    }

    private void clearToolbar() {
        // remove all buttons
        menuToolbar.removeAllViews();
    }

    private void calibrate() {

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
        setMode();
   }

    private void setMode() {
        imageView.setEnabled(!calipersMode);
        caliperView.setEnabled(calipersMode);
        actionBar.setTitle(calipersMode ? getString(R.string.ep_calipers_title) : getString(R.string.image_mode_title));
    }

    private void changeSettings() {
        // test rotation
        RotateAnimation rotateAnimation = new RotateAnimation(0.0f, 90.0f, RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setDuration(500L);
        rotateAnimation.setFillAfter(true);
        imageView.startAnimation(rotateAnimation);
    }
}


