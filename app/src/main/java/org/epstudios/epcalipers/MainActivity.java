package org.epstudios.epcalipers;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.os.Build;
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
import android.widget.ActionMenuView;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.ortiz.touch.TouchImageView;


public class MainActivity extends ActionBarActivity implements View.OnClickListener {
    static final String EPS = "EPS";
    private TouchImageView imageView;
    private CaliperView caliperView;
    private Toolbar menuToolbar;
    private Toolbar actionBar;
    private boolean calipersMode;

    // Buttons
    Button addCaliperButton;
    Button calibrateButton;
    Button intervalRateButton;
    Button meanRateButton;
    Button qtcButton;

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

    @Override
    public void onClick(View v) {
        if (v == addCaliperButton) {
            selectCaliperMenu();
        }
        else if (v == calibrateButton) {
            calibrate();;
        }
    }

    private void createButtons() {
        addCaliperButton = new Button(this);
        createButton(addCaliperButton, getString(R.string.add_caliper_button_title));
        calibrateButton = new Button(this);
        createButton(calibrateButton, getString(R.string.calibrate_button_title));
        intervalRateButton = new Button(this);
        createButton(intervalRateButton, "I/R");
        meanRateButton = new Button(this);
        createButton(meanRateButton, "mRR");
        qtcButton = new Button(this);
        createButton(qtcButton, "QTc");
    }

    private void createButton(Button button, String text) {
        button.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        button.setText(text);
        //button.setTextColor(Color.WHITE);


        //button.setTextColor(Color.WHITE);
    }

    private void createMainToolbar() {
        HorizontalScrollView scrollView = new HorizontalScrollView(this);
        HorizontalScrollView.LayoutParams layoutParams = new HorizontalScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        scrollView.setLayoutParams(layoutParams);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setLayoutParams(layoutParams);
        layout.addView(addCaliperButton);
        layout.addView(calibrateButton);
        layout.addView(intervalRateButton);
        layout.addView(meanRateButton);
        layout.addView(qtcButton);
        scrollView.addView(layout);
        menuToolbar.addView(scrollView);

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


