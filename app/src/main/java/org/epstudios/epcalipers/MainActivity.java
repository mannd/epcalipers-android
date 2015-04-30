package org.epstudios.epcalipers;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ScaleDrawable;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;

import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.ortiz.touch.TouchImageView;

import java.io.File;
import java.util.ArrayList;


public class MainActivity extends ActionBarActivity implements View.OnClickListener {
    private static final String EPS = "EPS";
    private static final int RESULT_LOAD_IMAGE = 1;
    private static final int RESULT_CAPTURE_IMAGE = 2;
    private TouchImageView imageView;
    private CalipersView calipersView;
    private Toolbar menuToolbar;
    private Toolbar actionBar;
    private boolean calipersMode;

    RelativeLayout layout;

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

        actionBar = (Toolbar) findViewById(R.id.action_bar);
        setSupportActionBar(actionBar);

        menuToolbar = (Toolbar) findViewById(R.id.menu_toolbar);

        imageView.setScaleType(ImageView.ScaleType.CENTER);
//        imageView.setMaxZoom(3.0f);
//        imageView.setMinZoom(0.25f);
//        imageView.setZoom(1.0f);
        lastZoomFactor = imageView.getCurrentZoom();


        createButtons();

        horizontalCalibration = new Calibration(Caliper.Direction.HORIZONTAL);
        verticalCalibration = new Calibration(Caliper.Direction.VERTICAL);

        rrIntervalForQTc = 0.0;

        calipersMode = true;
        setMode();

        layout = (RelativeLayout)findViewById(R.id.activity_main_id);
        ViewTreeObserver viewTreeObserver = layout.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @SuppressLint("NewApi")
            @SuppressWarnings("deprecation")
            @Override
            public void onGlobalLayout() {
                int androidVersion = Build.VERSION.SDK_INT;
                if (androidVersion >= Build.VERSION_CODES.JELLY_BEAN) {
                    layout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    layout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
                //scaleImageForImageView();
                addCaliperWithDirection(Caliper.Direction.HORIZONTAL);
            }
        });

    }

    private void scaleImageForImageView() {
        float ratio = 1.0f;
        Drawable image = imageView.getDrawable();
        float imageWidth = image.getIntrinsicWidth();
        float imageHeight = image.getIntrinsicHeight();
        float actionBarHeight = actionBar.getHeight();
        float toolbarHeight = menuToolbar.getHeight();
        float statusBarHeight = getStatusBarHeight();
        Pair<Integer, Integer> screenDimensions = getScreenDimensions();
        float screenWidth = (float) screenDimensions.first;
        float screenHeight = (float) screenDimensions.second;
        float verticalSpace = statusBarHeight + actionBarHeight + toolbarHeight;

        float portraitWidth = Math.min(screenHeight, screenWidth);
        float landscapeWidth = Math.max(screenHeight, screenWidth);
        float portraitHeight = Math.max(screenHeight, screenWidth) - verticalSpace;
        float landscapeHeight = Math.min(screenHeight, screenWidth) - verticalSpace;

        Log.d(EPS, "ActionBar height = " + actionBarHeight + " Toolbar height = " +
                toolbarHeight + " StatusBar height = " + statusBarHeight);
        Log.d(EPS, "ImageView height = " + imageView.getHeight());
        Log.d(EPS, "Screen height = " + screenHeight);

        if (imageWidth > imageHeight) {
            ratio = portraitWidth / imageWidth;
        }
        else {
            ratio = landscapeHeight / imageHeight;
        }
        Bitmap bitmap = ((BitmapDrawable)image).getBitmap();
        int bitmapWidth = bitmap.getWidth();
        int bitmapHeight = bitmap.getHeight();
        Log.d(EPS, "imageWidth = " + imageWidth + " imageHeight = " + imageHeight);
        Log.d(EPS, "bitmapWidth = " + bitmapWidth + " bitmapHeight = " +
                bitmapHeight);
        Matrix matrix = new Matrix();
        matrix.postScale(1.0f - ratio, 1.0f - ratio);
        Log.d(EPS, "ratio = " + ratio);
        Bitmap scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmapWidth,
                bitmapHeight, matrix, true);
        BitmapDrawable result = new BitmapDrawable(scaledBitmap);
        imageView.setImageDrawable(result);


    }

    private Pair<Integer, Integer> getScreenDimensions () {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int height = size.y;
        int width = size.x;
        return new Pair<Integer, Integer>(width, height);
    }

    private float getStatusBarHeight() {
        Rect rect = new Rect();
        Window window = getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rect);
        int statusBarHeight = rect.top;
        return statusBarHeight;
    }

    // handle rotation


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("calipersMode", calipersMode);
        // TODO all the others
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        calipersMode = savedInstanceState.getBoolean("calipersMode");
        // TODO more stuff
        setMode();
        calipersView.invalidate();
    }

    @Override
    public void onClick(View v) {
        if (v == addCaliperButton) {
            selectAddCaliperMenu();
        } else if (v == cancelAddCaliperButton) {
            selectMainMenu();
        } else if (v == adjustImageButton) {
            selectAdjustImageMenu();
        } else if (v == backToImageMenuButton) {
            selectImageMenu();
        } else if (v == calibrateButton) {
            selectCalibrationMenu();
        } else if (v == doneCalibrationButton) {
            selectMainMenu();
        } else if (v == rotateImageLeftButton) {
            rotateImage(-90.0f);
        } else if (v == rotateImageRightButton) {
            rotateImage(90.0f);
        } else if (v == tweakImageLeftButton) {
            rotateImage(-1.0f);
        } else if (v == tweakImageRightButton) {
            rotateImage(1.0f);
        } else if (v == resetImageButton) {
            resetImage();
        } else if (v == backToImageMenuButton) {
            selectImageMenu();
        } else if (v == horizontalCaliperButton) {
            addCaliperWithDirection(Caliper.Direction.HORIZONTAL);
        } else if (v == verticalCaliperButton) {
            addCaliperWithDirection(Caliper.Direction.VERTICAL);
        } else if (v == selectImageButton) {
            selectImageFromGallery();
        } else if (v == cameraButton) {
            takePhoto();
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
       // button.setBackgroundColor(getResources().getColor(R.color.primary));
        button.getBackground().setColorFilter(getResources()
                .getColor(R.color.primary), PorterDuff.Mode.CLEAR);
       // button.setBackgroundResource(0);
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
        } else {
            actionBar.setTitle(getString(R.string.image_mode_title));
            selectImageMenu();
        }
    }

    private void changeSettings() {
      // TODO
    }

    private void selectImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, RESULT_LOAD_IMAGE);
    }

    private void takePhoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, RESULT_CAPTURE_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();


            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            Log.d(EPS, "picturePath = " + picturePath);
            cursor.close();

            Bitmap bitmap = BitmapFactory.decodeFile(picturePath);

            imageView.setImageBitmap(bitmap);

        }
        if (resultCode == RESULT_CAPTURE_IMAGE && resultCode == RESULT_OK && null != data) {
            imageView.setImageBitmap((Bitmap) data.getExtras().get("data"));
        }
    }


    private void rotateImage(float degrees) {
        imageView.rotateImage(degrees);
    }

    private void resetImage() {
        imageView.resetImage();
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
        addCaliperWithDirectionAtRect(direction, new Rect(0, 0, calipersView.getWidth(),
                calipersView.getHeight()));
        calipersView.invalidate();
        selectMainMenu();
    }

    private void addCaliperWithDirectionAtRect(Caliper.Direction direction,
                                               Rect rect) {
        Caliper c = new Caliper();
        // TODO set up caliper per settings
        // c.linewidth = settings.linewidht;
        // etc.
        c.setDirection(direction);
        if (direction == Caliper.Direction.HORIZONTAL) {
            c.setCalibration(horizontalCalibration);
        } else {
            c.setCalibration(verticalCalibration);
        }
        c.setInitialPosition(rect);
        getCalipers().add(c);
    }
}



