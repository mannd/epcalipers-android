package org.epstudios.epcalipers;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;

import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.Pair;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import uk.co.senab.photoview.PhotoViewAttacher;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainActivity extends ActionBarActivity implements View.OnClickListener {
    private static final String EPS = "EPS";
    private static final int RESULT_LOAD_IMAGE = 1;
    private static final int RESULT_CAPTURE_IMAGE = 2;
    private ImageView imageView;
    private CalipersView calipersView;
    private Toolbar menuToolbar;
    private Toolbar actionBar;
    private boolean calipersMode;
    private PhotoViewAttacher attacher;
    private String currentPhotoPath;

    private static boolean firstRun = true;

    private RelativeLayout layout;

    // Buttons
    private Button addCaliperButton;
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

    private double rrIntervalForQTc;

    float sizeDiffWidth;
    float sizeDiffHeight;

    boolean isRotatedImage;

    float portraitWidth;
    float portraitHeight;
    float landscapeWidth;
    float landscapeHeight;

    private String dialogResult;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            ;
        }
        Log.d(EPS, "onCreate");

        setContentView(R.layout.activity_main);

        imageView = (ImageView) findViewById(R.id.imageView);
        attacher = new PhotoViewAttacher(imageView);
        attacher.setScaleType(ImageView.ScaleType.FIT_CENTER);
        // We need to use MatrixChangeListener and not ScaleChangeListener
        // since the former only fires when scale has completely changed and
        // the latter fires while the scale is changing, so is inaccurate.
        attacher.setOnMatrixChangeListener(new MatrixChangeListener());

        calipersView = (CalipersView) findViewById(R.id.caliperView);

        actionBar = (Toolbar) findViewById(R.id.action_bar);
        setSupportActionBar(actionBar);

        menuToolbar = (Toolbar) findViewById(R.id.menu_toolbar);

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
                scaleImageForImageView();
                addCaliperWithDirection(Caliper.Direction.HORIZONTAL);
                if (true)
                    return;
                attacher = new PhotoViewAttacher(imageView);
                attacher.setScaleType(ImageView.ScaleType.CENTER);
                attacher.setMinimumScale(0.5f);
                attacher.setMaximumScale(3.0f);
                attacher.setMediumScale(1.0f);
                if (firstRun) {
                    scaleImageForImageView();
                    firstRun = false;
                }
                if (savedInstanceState != null) {
                    attacher.setScale(savedInstanceState.getFloat("scale"));
                }
                Log.d(EPS, "attacher scale = " + attacher.getScale());

                // TODO use saved scale to fix calibration after rotation
            }
        });

    }

    private void scaleImageForImageView() {
        if (true)
            return;
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
        float ratio;
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
        matrix.postScale(ratio, ratio);
        Log.d(EPS, "ratio = " + ratio);
        Bitmap scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmapWidth,
                bitmapHeight, matrix, true);
        BitmapDrawable result = new BitmapDrawable(getResources(), scaledBitmap);
        imageView.setImageDrawable(result);
        attacher.update();
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
        return rect.top;
    }

    // handle rotation


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.d(EPS, "onSaveInstanceState");
        super.onSaveInstanceState(outState);
        outState.putBoolean("calipersMode", calipersMode);
        outState.putFloat("scale", attacher.getScale());
        // TODO all the others

    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.d(EPS, "onRestoreInstanceState");
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
            setupCalibration();
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
        } else if (v == horizontalCaliperButton) {
            addCaliperWithDirection(Caliper.Direction.HORIZONTAL);
        } else if (v == verticalCaliperButton) {
            addCaliperWithDirection(Caliper.Direction.VERTICAL);
        } else if (v == selectImageButton) {
            selectImageFromGallery();
        } else if (v == cameraButton) {
            takePhoto();
        } else if (v == setCalibrationButton) {
            setCalibration();
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
        cameraButton.setEnabled(getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA));
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
        ArrayList<Button> buttons = new ArrayList<>();
        buttons.add(addCaliperButton);
        buttons.add(calibrateButton);
        buttons.add(intervalRateButton);
        buttons.add(meanRateButton);
        buttons.add(qtcButton);
        mainMenu = createMenu(buttons);
    }

    private void createImageMenu() {
        ArrayList<Button> buttons = new ArrayList<>();
        buttons.add(cameraButton);
        buttons.add(selectImageButton);
        buttons.add(adjustImageButton);
        imageMenu = createMenu(buttons);
    }

    private void createAddCaliperMenu() {
        ArrayList<Button> buttons = new ArrayList<>();
        buttons.add(horizontalCaliperButton);
        buttons.add(verticalCaliperButton);
        buttons.add(cancelAddCaliperButton);
        addCaliperMenu = createMenu(buttons);
    }

    private void createAdjustImageMenu() {
        ArrayList<Button> buttons = new ArrayList<>();
        buttons.add(rotateImageLeftButton);
        buttons.add(rotateImageRightButton);
        buttons.add(tweakImageLeftButton);
        buttons.add(tweakImageRightButton);
        buttons.add(resetImageButton);
        buttons.add(backToImageMenuButton);
        adjustImageMenu = createMenu(buttons);
    }

    private void createCalibrationMenu() {
        ArrayList<Button> buttons = new ArrayList<>();
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
        calipersView.setLocked(false);
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
            getSupportActionBar().setTitle(getString(R.string.ep_calipers_title));
            selectMainMenu();
        } else {
            getSupportActionBar().setTitle(getString(R.string.image_mode_title));
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
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // make sure device has a camera
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // TODO toast it?
            }
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, RESULT_CAPTURE_IMAGE);
            }
        }
        // TODO else warning dialog, no camera?
    }


    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        currentPhotoPath = image.getAbsolutePath();
       // galleryAddPic();
        return image;
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
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

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(picturePath, options);
            int imageHeight = options.outHeight;
            int imageWidth = options.outWidth;
            Log.d(EPS, "image=" + imageWidth + "x" + imageHeight);
            int targetWidth = imageView.getWidth();
            int targetHeight = imageView.getHeight();
            options.inSampleSize = calculateInSampleSize(options, targetWidth, targetHeight);
            options.inJustDecodeBounds = false;
            Bitmap bitmap = BitmapFactory.decodeFile(picturePath, options);
            imageHeight = bitmap.getHeight();
            imageWidth = bitmap.getWidth();
            Log.d(EPS, "image=" + imageWidth + "x" + imageHeight);

            imageView.setImageBitmap(bitmap);
            attacher.update();


        }
        if (requestCode == RESULT_CAPTURE_IMAGE && resultCode == RESULT_OK) {
            int targetWidth = imageView.getWidth();
            int targetHeight = imageView.getHeight();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(currentPhotoPath, options);
            int imageWidth = options.outWidth;
            int imageHeight = options.outHeight;

//            int scaleFactor = Math.min(imageWidth / targetWidth,
//                    imageHeight / targetHeight);
             int scaleFactor = calculateInSampleSize(options, targetWidth,
              targetHeight);
            Log.d(EPS, "scaleFactor=" + scaleFactor);
            options.inJustDecodeBounds = false;
            options.inSampleSize = scaleFactor;
            Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath, options);
            imageView.setImageBitmap(bitmap);
            attacher.update();
        }
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }


    private void rotateImage(float degrees) {
        attacher.setRotationBy(degrees);

        //imageView.rotateImage(degrees);
    }

    private void resetImage() {
        attacher.setRotationTo(0f);
        //imageView.resetImage();
    }

    private void showHelp() {
        startActivity(new Intent(this, Help.class));
    }

    private void about() {
        startActivity(new Intent(this, About.class));
    }

    private void meanRR() {
        if (calipersCount() < 1) {
            noCalipersAlert();
            // select main toolbar;
            return;
        }
        Caliper singleHorizontalCaliper = getLoneTimeCaliper();
    }

    private void toggleIntervalRate() {
        horizontalCalibration.setDisplayRate(!horizontalCalibration.getDisplayRate());
        calipersView.invalidate();
    }

    private int calipersCount() {
        return calipersView.calipersCount();
    }

    private ArrayList<Caliper> getCalipers() {
        return calipersView.getCalipers();
    }

    private void showNoTimeCaliperSelectedAlert() {
        showSimpleAlert(R.string.no_time_caliper_selected_title,
                R.string.no_time_caliper_selected_message);
    }

    private boolean noTimeCaliperSelected() {
        return calipersCount() < 1 ||
                calipersView.noCaliperIsSelected() ||
                calipersView.activeCaliper().getDirection() == Caliper.Direction.VERTICAL;
    }

    private void noCaliperSelectedAlert() {
        showSimpleAlert(R.string.no_caliper_selected_alert_title,
                R.string.no_caliper_selected_alert_message);
    }

    private void setupCalibration() {
        if (calipersCount() < 1) {
            noCalipersAlert();
        }
        else {
            selectCalibrationMenu();
            calipersView.selectCaliperIfNoneSelected();
            calipersView.setLocked(true);
        }
    }

    private void setCalibration() {
        if (calipersCount() < 1) {
            noCalipersAlert();
            selectMainMenu();
            return;
        }
        if (calipersView.noCaliperIsSelected()) {
            noCaliperSelectedAlert();
            return;
        }
        Caliper c = calipersView.activeCaliper();
        if (c == null) {
            return; // shouldn't happen, but if it does...
        }
        String example;
        if (c.getDirection() == Caliper.Direction.VERTICAL) {
            example = getString(R.string.example_amplitude_measurement);
        }
        else {
            example = getString(R.string.example_time_measurement);
        }
        String message = String.format(getString(R.string.calibration_dialog_message),
                example);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.calibrate_dialog_title));
        builder.setMessage(message);

        final EditText input = new EditText(this);
        input.setLines(1);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint(getString(R.string.calibration_dialog_hint));
        input.setSelection(0);
        // TODO set default/last value

        builder.setView(input);

        builder.setPositiveButton(getString(R.string.ok_title), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialogResult = input.getText().toString();
                processCalibration();
            }
        });
        builder.setNegativeButton(getString(R.string.cancel_title), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private class CalibrationResult {
        public boolean success;
        public float value;
        public String units;

        CalibrationResult() {
            success = false;
            value = 0.0f;
            units = "";
        }
    }

    private void processCalibration() {
        Log.d(EPS, "process calibration...");
        if (dialogResult.length() < 1) {
            return;
        }
        CalibrationResult calibrationResult = processCalibrationString(dialogResult);
        if (!calibrationResult.success) {
            return;
        }
        Caliper c = calipersView.activeCaliper();
        if (c == null || c.getValueInPoints() <= 0) {
            return;
        }
        Calibration cal;
        if (c.getDirection() == Caliper.Direction.HORIZONTAL) {
            cal = horizontalCalibration;
        }
        else {
            cal = verticalCalibration;
        }
        cal.setCalibrationString(dialogResult);
        cal.setUnits(calibrationResult.units);
        if (cal.getDirection() == Caliper.Direction.HORIZONTAL && cal.canDisplayRate()) {
            cal.setDisplayRate(false);
        }
        cal.setOriginalZoom(attacher.getScale());
        cal.setOriginalCalFactor(calibrationResult.value / c.getValueInPoints());
        cal.setCurrentZoom(cal.getOriginalZoom());
        cal.setCalibrated(true);
        calipersView.invalidate();
        selectMainMenu();
    }

    // Guarantees outCalFactor is non-negative, non-zero.
    // outUnits can be zero-length string.
    private CalibrationResult processCalibrationString(String in) {
        CalibrationResult calibrationResult = new CalibrationResult();
        if (in.length() < 1) {
            return calibrationResult;
        }
        List<String> chunks = parse(in);
        Log.d(EPS, "chunks = " + chunks);
        if (chunks.size() < 1) {
            return calibrationResult;
        }
        NumberFormat format = NumberFormat.getInstance();
        try {
            Number number = format.parse(chunks.get(0));
            calibrationResult.value = number.floatValue();
        } catch (Exception ex) {
            Log.d(EPS, "exception = " + ex.toString());
            return calibrationResult;
        }
        if (chunks.size() > 1) {
            calibrationResult.units = chunks.get(1);
        }
        // all calibration values must be positive
        calibrationResult.value = Math.abs(calibrationResult.value);
        // check for other badness
        if (calibrationResult.value <= 0.0f) {
            return calibrationResult;
        }
        calibrationResult.success = true;
        return calibrationResult;
    }

    static final Pattern VALID_PATTERN = Pattern.compile("[.,0-9]+|[a-zA-Z]+");

    private List<String> parse(String toParse) {
        List<String> chunks = new LinkedList<>();
        Matcher matcher = VALID_PATTERN.matcher(toParse);
        while (matcher.find()) {
            chunks.add( matcher.group() );
        }
        return chunks;
    }


    private void noCalipersAlert() {
        showSimpleAlert(R.string.no_calipers_alert_title,
                R.string.no_calipers_alert_message);
    }

    private void showSimpleAlert(int title, int message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("OK", null);
        AlertDialog dialog = builder.create();
        dialog.show();

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

    private void matrixChangedAction() {
        Log.d(EPS, "Matrix changed, scale = " + attacher.getScale());
        adjustCalibrationForScale(attacher.getScale());
        calipersView.invalidate();
    }

    private void adjustCalibrationForScale(float scale) {
        horizontalCalibration.setCurrentZoom(scale);
        verticalCalibration.setCurrentZoom(scale);
    }

    private class MatrixChangeListener implements PhotoViewAttacher.OnMatrixChangedListener {

        @Override
        public void onMatrixChanged(RectF rect) {
            matrixChangedAction();

        }
    }

}



