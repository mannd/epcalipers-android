package org.epstudios.epcalipers;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;

import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
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

    private GestureDetectorCompat gestureDetector;

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
    Button tweakImageRightButton;
    Button tweakImageLeftButton;
    Button flipImageButton;
    Button resetImageButton;
    Button backToImageMenuButton;
    Button horizontalCaliperButton;
    Button verticalCaliperButton;
    Button cancelAddCaliperButton;
    Button setCalibrationButton;
    Button clearCalibrationButton;
    Button doneCalibrationButton;
    Button measureRRButton;
    Button measureQTButton;

    HorizontalScrollView mainMenu;
    HorizontalScrollView imageMenu;
    HorizontalScrollView addCaliperMenu;
    HorizontalScrollView adjustImageMenu;
    HorizontalScrollView calibrationMenu;

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

        createButtons();

        calipersMode = true;
        setMode();

        gestureDetector = new GestureDetectorCompat(this, new MyGestureListener());

    }

    @Override
    public void onClick(View v) {
        if (v == addCaliperButton) {
            selectAddCaliperMenu();
        }
        else if (v == cancelAddCaliperButton) {
            selectMainMenu();
        }
        else if (v == adjustImageButton) {
            selectAdjustImageMenu();
        }
        else if (v == backToImageMenuButton) {
            selectImageMenu();
        }
        else if (v == calibrateButton) {
            selectCalibrationMenu();
        }
        else if (v == doneCalibrationButton) {
            selectMainMenu();
        }
    }

    private void createButtons() {
        // Main/Caliper menu
        addCaliperButton = createButton(getString(R.string.add_caliper_button_title));
        calibrateButton = createButton(getString(R.string.calibrate_button_title));
        intervalRateButton = createButton(getString(R.string.interval_rate_button_title));
        meanRateButton = createButton(getString(R.string.mean_rate_button_title));
        qtcButton = createButton(getString(R.string.qtc_button_title));
        // Image menu
        cameraButton = createButton(getString(R.string.camera_button_title));
        selectImageButton = createButton(getString(R.string.select_image_button_title));
        adjustImageButton = createButton(getString(R.string.adjust_image_button_title));
        horizontalCaliperButton = createButton(getString(R.string.horizontal_caliper_button_title));
        verticalCaliperButton = createButton(getString(R.string.vertical_caliper_button_title));
        cancelAddCaliperButton = createButton(getString(R.string.cancel_button_title));
        // Add Caliper menu
        horizontalCaliperButton = createButton(getString(R.string.horizontal_caliper_button_title));
        verticalCaliperButton = createButton(getString(R.string.vertical_caliper_button_title));
        cancelAddCaliperButton = createButton(getString(R.string.cancel_button_title));
        // Adjust Image menu
        rotateImageRightButton = createButton(getString(R.string.rotate_image_right_button_title));
        rotateImageLeftButton = createButton(getString(R.string.rotate_image_left_button_title));
        tweakImageRightButton = createButton(getString(R.string.tweak_image_right_button_title));
        tweakImageLeftButton = createButton(getString(R.string.tweak_image_left_button_title));
        flipImageButton = createButton(getString(R.string.flip_image_button_title));
        resetImageButton = createButton(getString(R.string.reset_image_button_title));
        backToImageMenuButton = createButton(getString(R.string.done_button_title));
        // Calibration menu
        setCalibrationButton = createButton(getString(R.string.set_calibration_button_title));
        clearCalibrationButton = createButton(getString(R.string.clear_calibration_button_title));
        doneCalibrationButton = createButton(getString(R.string.done_button_title));

    }

    private Button createButton(String text) {
        Button button = new Button(this);
        button.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        button.setText(text);

        // buttons won't flash, but will behave similarly to iOS buttons,
        // alternative is ugly colors.
        button.setTextColor(Color.WHITE);
        button.setBackgroundColor(getResources().getColor(R.color.primary));
        button.setOnClickListener(this);
        return button;
    }

    private void createMainMenu() {
        ArrayList<Button> buttons = new ArrayList<Button>();
        buttons.add(addCaliperButton);
        buttons.add(calibrateButton);
        buttons.add(intervalRateButton);
        buttons.add(meanRateButton);
        buttons.add(qtcButton);
        mainMenu = createMenu(buttons);
    }

    private void createImageMenu() {
        ArrayList<Button> buttons = new ArrayList<Button>();
        buttons.add(cameraButton);
        buttons.add(selectImageButton);
        buttons.add(adjustImageButton);
        imageMenu = createMenu(buttons);
    }

    private void createAddCaliperMenu() {
        ArrayList<Button> buttons = new ArrayList<Button>();
        buttons.add(horizontalCaliperButton);
        buttons.add(verticalCaliperButton);
        buttons.add(cancelAddCaliperButton);
        addCaliperMenu = createMenu(buttons);
    }

    private void createAdjustImageMenu() {
        ArrayList<Button> buttons = new ArrayList<Button>();
        buttons.add(rotateImageLeftButton);
        buttons.add(rotateImageRightButton);
        buttons.add(tweakImageLeftButton);
        buttons.add(tweakImageRightButton);
        buttons.add(flipImageButton);
        buttons.add(resetImageButton);
        buttons.add(backToImageMenuButton);
        adjustImageMenu = createMenu(buttons);
    }

    private void createCalibrationMenu() {
        ArrayList<Button> buttons = new ArrayList<Button>();
        buttons.add(setCalibrationButton);
        buttons.add(clearCalibrationButton);
        buttons.add(doneCalibrationButton);
        calibrationMenu = createMenu(buttons);
    }

    private void selectMainMenu() {
        if (mainMenu == null) {
            createMainMenu();
        }
        selectMenu(mainMenu);
    }

    private void selectImageMenu() {
        if (imageMenu == null) {
            createImageMenu();
        }
        selectMenu(imageMenu);
    }

    private void selectAddCaliperMenu() {
        if (addCaliperMenu == null) {
            createAddCaliperMenu();
        }
        selectMenu(addCaliperMenu);
    }

    private void selectAdjustImageMenu() {
        if (adjustImageMenu == null) {
            createAdjustImageMenu();
        }
        selectMenu(adjustImageMenu);
    }

    private void selectCalibrationMenu() {
        if (calibrationMenu == null) {
            createCalibrationMenu();
        }
        selectMenu(calibrationMenu);
    }

    private void selectMenu(HorizontalScrollView menu) {
        clearToolbar();
        menuToolbar.addView(menu);
    }

    private HorizontalScrollView createMenu(ArrayList<Button> buttons) {
        HorizontalScrollView scrollView = new HorizontalScrollView(this);
        HorizontalScrollView.LayoutParams layoutParams = new HorizontalScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        scrollView.setLayoutParams(layoutParams);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setLayoutParams(layoutParams);
        for (Button button : buttons) {
            layout.addView(button);
        }
        scrollView.addView(layout);
        return scrollView;
    }

    private void clearToolbar() {
        // remove all buttons
        menuToolbar.removeAllViews();
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
        if (id == R.id.help) {
            showHelp();
            return true;
        }
        if (id == R.id.about) {
            about();
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
        if (calipersMode) {
            actionBar.setTitle(getString(R.string.ep_calipers_title));
            selectMainMenu();
        }
        else {
            actionBar.setTitle(getString(R.string.image_mode_title));
            selectImageMenu();
        }
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
        startActivity(new Intent(this, Help.class));
    }

    private void about() {
        startActivity(new Intent(this, About.class));
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

    // Gestures
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent event) {
            // must be implemented and return true;
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent event) {
            Log.d(EPS, "onSingleTapUp: " + event.toString());
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent event) {
            Log.d(EPS, "onSingleTapConfirmed: " + event.toString());
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
                                float distanceY) {
            Log.d(EPS, "onScroll: " + e1.toString() + e2.toString());
            return true;
        }
    }




}


