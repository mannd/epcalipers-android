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
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import com.ortiz.touch.TouchImageView;

import java.util.ArrayList;


public class MainActivity extends ActionBarActivity implements View.OnClickListener {
    static final String EPS = "EPS";
    private TouchImageView imageView;
    private CalipersView calipersView;
    private Toolbar menuToolbar;
    private Toolbar actionBar;
    private boolean calipersMode;

    // Buttons
    Button addCaliperButton;
    Button calibrateButton;
    Button intervalRateButton;
    Button meanRateButton;
    Button qtcButton;
    Button cameraButton;
    Button selectImageButton;
    Button adjustImageButton;
    Button rotateImageRightButton;
    Button rotateImageLeftButton;
    Button tweakRightButton;
    Button tweakLeftButton;
    Button flipImageButton;
    Button resetImageButton;
    Button backToImageMenuButton;
    Button horizontalCaliperButton;
    Button verticalCaliperButton;
    Button cancelButton;
    Button setCalibrationButton;
    Button clearCalibrationButton;
    Button measureRRButton;
    Button measureQTButton;

    Calibration horizontalCalibration;
    Calibration verticalCalibration;
    // Settings settings;

    double rrIntervalForQTc;
    boolean isFirstRun;

    float sizeDiffWidth;
    float sizeDiffHeight;

    float lastZoomFactor;
    boolean isRotatedImage;

    float portraitWidth;
    float portraitHeight;
    float landscapeWidth;
    float landscapeHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (TouchImageView) findViewById(R.id.imageView);
        calipersView = (CalipersView) findViewById(R.id.caliperView);


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

        // buttons won't flash, but will behave similarly to iOS buttons,
        // alternative is ugly colors.
        button.setTextColor(Color.WHITE);
        button.setBackgroundColor(getResources().getColor(R.color.primary));

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
        calipersView.setEnabled(calipersMode);
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

    private void showHelp() {
        // TODO load help activity
    }

    private void toggleIntervalRate() {
        horizontalCalibration.setDisplayRate(!horizontalCalibration.getDisplayRate());
        // TODO force redisplay
    }

    private void meanRR() {
        if (calipersCount() < 1) {
            showNoCalipersAlert();
            // select main toolbar;
            return;
        }
        Caliper singleHorizontalCaliper = getLoneTimeCaliper();
    }

    private int calipersCount() {
        return calipersView.calipersCount();
    }

    private ArrayList<Caliper> getCalipers() {
        return calipersView.getCalipers();
    }

    private void showNoCalipersAlert() {
        // TODO
    }

    private Caliper getLoneTimeCaliper() {
        Caliper c = null;
        int n = 0;
        if (calipersCount() > 0) {
            for (Caliper caliper : getCalipers()) {
                if (caliper.getDirection() == Caliper.Direction.HORIZONTAL) {
                    c = caliper;
                    n++;
                }
            }
        }
        return (n == 1) ? c : null;
    }

    private void unselectCalipersExcept(Caliper c) {
        // if only one caliper, no others can be selected
        if (calipersCount() > 1) {
            for (Caliper caliper : getCalipers()) {
                if (caliper != c) {
                    calipersView.unselectCaliper(c);
                }
            }
        }
    }

    private void addCaliperWithDirection(Caliper.Direction direction) {
        Caliper c = new Caliper();
        // TODO set up calper per settings
        // c.linewidth = settings.linewidht;
        // etc.
        c.setDirection(direction);
        if (direction == Caliper.Direction.HORIZONTAL) {
            c.setCalibration(horizontalCalibration);
        }
        else {
            c.setCalibration(verticalCalibration);
        }
        // c.setInitialPosition(caliperView bounds);
        // TODO force redisplay
        // back to main toolbar
    }


}


