package org.epstudios.epcalipers;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import androidx.annotation.NonNull;

import com.github.chrisbanes.photoview.OnMatrixChangedListener;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.material.navigation.NavigationView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.util.Pair;
import android.view.ActionMode;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.epstudios.epcalipers.QtcCalculator.QtcFormula;
import org.vudroid.core.DecodeServiceBase;
import org.vudroid.pdfdroid.codec.PdfContext;
import org.vudroid.pdfdroid.codec.PdfPage;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.epstudios.epcalipers.MyPreferenceFragment.ALL;
import static org.epstudios.epcalipers.MyPreferenceFragment.BAZETT;
import static org.epstudios.epcalipers.MyPreferenceFragment.FRAMINGHAM;
import static org.epstudios.epcalipers.MyPreferenceFragment.FRIDERICIA;
import static org.epstudios.epcalipers.MyPreferenceFragment.HODGES;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    // TODO: regex below includes Cyrillic and must be updated with new alphabets
    @SuppressWarnings("HardCodedStringLiteral")
    private static final String calibrationRegex = "[.,0-9]+|[a-zA-ZА-яЁё]+";
    private static final Pattern VALID_PATTERN = Pattern.compile(calibrationRegex);
    private static final String LF = "\n";
    private static final int RESULT_LOAD_IMAGE = 1;
    private static final int RESULT_CAPTURE_IMAGE = 2;
    private static final int DEFAULT_LINE_WIDTH = 2;

    // new permissions for Android >= 6.0
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 100;
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 101;
    private static final int MY_PERMISSIONS_REQUEST_STARTUP_IMAGE = 102;
    private static final int MY_PERMISSIONS_REQUEST_STARTUP_PDF = 103;
    private static final int MY_PERMISSIONS_REQUEST_STORE_BITMAP = 104;
    private static final int MY_PERMISSIONS_REQUEST_STARTUP_SENT_IMAGE = 105;

    // Store version information
    private Version version;

    // OnSharedPreferenceListener must be a strong reference
    // as otherwise it is a weak reference and will be garbage collected, thus
    // making it stop working.
    // See http://stackoverflow.com/questions/2542938/sharedpreferences-onsharedpreferencechangelistener-not-being-called-consistently
    SharedPreferences.OnSharedPreferenceChangeListener onSharedPreferenceChangeListener;

    // Lots of buttons
    private Button calibrateButton;
    private Button intervalRateButton;
    private Button meanRateButton;
    private Button qtcButton;
    private Button colorDoneButton;
    private Button tweakDoneButton;
    private Button previousPageButton;
    private Button nextPageButton;
    private Button gotoPageButton;
    private Button pdfDoneButton;
    private Button rotateImageRightButton;
    private Button rotateImageLeftButton;
    private Button tweakImageRightButton;
    private Button tweakImageLeftButton;
    private Button microTweakImageRightButton;
    private Button microTweakImageLeftButton;
    private Button resetImageButton;
    private Button rotateDoneButton;
    private Button horizontalCaliperButton;
    private Button verticalCaliperButton;
    private Button angleCaliperButton;
    private Button cancelAddCaliperButton;
    private Button setCalibrationButton;
    private Button clearCalibrationButton;
    private Button doneCalibrationButton;
    private Button measureRRButton;
    private Button cancelQTcButton;
    private Button measureQTButton;
    private Button cancelQTcMeasurementButton;
    private Button leftButton;
    private Button rightButton;
    private Button microLeftButton;
    private Button microRightButton;
    private Button upButton;
    private Button downButton;
    private Button microUpButton;
    private Button microDownButton;
    private Button microDoneButton;
    private TextView microTextView;
    // Toolbar menus
    private HorizontalScrollView mainMenu;
    private HorizontalScrollView pdfMenu;
    private HorizontalScrollView addCaliperMenu;
    private HorizontalScrollView rotateImageMenu;
    private HorizontalScrollView calibrationMenu;
    private HorizontalScrollView qtcStep1Menu;
    private HorizontalScrollView qtcStep2Menu;
    private HorizontalScrollView colorMenu;
    private HorizontalScrollView tweakMenu;
    private HorizontalScrollView microMovementMenu;
    // Calibration
    private Calibration horizontalCalibration;
    private Calibration verticalCalibration;
    // Views
    private PhotoView imageView;
    private CalipersView calipersView;
    private Toolbar menuToolbar;
    private Toolbar actionBar;
    private NavigationView navigationView;
    // Side menu items
    private MenuItem lockImageMenuItem;
    private String currentPhotoPath;
    private FrameLayout layout;
    private DrawerLayout drawerLayout;

    private double rrIntervalForQTc;
    private boolean showStartImage;
    private boolean roundMsecRate;
    private boolean autoPositionText;
    private boolean inQtc = false;
    private int currentCaliperColor;
    private int currentHighlightColor;
    private int currentLineWidth;
    private String defaultTimeCalibration;
    private String defaultAmplitudeCalibration;
    private String dialogResult;
    private int shortAnimationDuration;
    private boolean noSavedInstance;
    private float totalRotation;
    private boolean externalImageLoad;
    private Bitmap externalImageBitmap;
    private Uri currentPdfUri;
    private int numberOfPdfPages;
    private int currentPdfPageNumber;
    private boolean useLargeFont;
    private boolean imageIsLocked = false;

    private float smallFontSize;
    private float largeFontSize;

    private Bitmap previousBitmap = null;

    private List<Button> upDownButtons;
    private List<Button> rightLeftButtons;

    private Map<String, QtcFormula> qtcFormulaMap;
    private QtcFormula qtcFormulaPreference = QtcFormula.qtcBzt;

    private HashMap<String, Caliper.TextPosition> textPositionMap;

    /// TODO: get some consistent defaults here
    private Caliper.TextPosition timeCaliperTextPositionPreference = Caliper.TextPosition.CenterBelow;
    private Caliper.TextPosition amplitudeCaliperTextPositionPreference = Caliper.TextPosition.Left;

    private Deque<ToolbarMenu> toolbarMenuDeque = new ArrayDeque<ToolbarMenu>();

    private enum ToolbarMenu {
        Main,
        AddCaliper,
        Calibration,
        QTc1,
        QTc2,
        Rotate,
        PDF,
        Color,
        Tweak,
        Move
    }

    public ActionMode getCurrentActionMode() {
        return currentActionMode;
    }

    private ActionMode currentActionMode;

    private final ActionMode.Callback imageCallBack = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            currentActionMode = mode;
            mode.setTitle(R.string.image_actions_title);
            getMenuInflater().inflate(R.menu.image_context_menu, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            MenuItem pdfMenuItem = menu.findItem(R.id.menu_pdf);
            pdfMenuItem.setVisible(currentPdfUri != null && numberOfPdfPages > 0);
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_rotate:
                    selectRotateImageMenu();
                    mode.finish();
                    return true;
                case R.id.menu_pdf:
                    selectPDFMenu();
                    mode.finish();
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            currentActionMode = null;
        }
    };

    public final ActionMode.Callback calipersActionCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            currentActionMode = mode;
            mode.setTitle(R.string.caliper_actions_title);
            getMenuInflater().inflate(R.menu.caliper_context_menu, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            MenuItem marchingMenuItem = menu.findItem(R.id.menu_march);
            marchingMenuItem.setVisible(calipersView.getTouchedCaliper().isTimeCaliper());
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_color:
                    selectColorMenu();
                    calipersView.setTweakingOrColoring(true);
                    mode.finish();
                    return true;
                case R.id.menu_tweak:
                    selectTweakMenu();
                    calipersView.setTweakingOrColoring(true);
                    mode.finish();
                    return true;
                case R.id.menu_march:
                    toggleMarchingCalipers();
                    mode.finish();
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            currentActionMode = null;
        }
    };

    /// TODO: make false for release
    // NB: we don't provide quick start dialogs anymore, so keep this false.
    private final boolean force_first_run = false;

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

    // Convert dp to pixels utility
    private float dpToPixel(float dp) {
        float density = getResources().getDisplayMetrics().density;
        float pixel = dp * density;
        return pixel;
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EPSLog.log("onCreate");

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        noSavedInstance = (savedInstanceState == null);

        smallFontSize = getResources().getDimension(R.dimen.small_font_size);
        largeFontSize = getResources().getDimension(R.dimen.large_font_size);
        EPSLog.log("Small font size = " + smallFontSize + " large font size = " + largeFontSize);

        setContentView(R.layout.activity_main);
        findViewById(R.id.loadingPanel).setVisibility(View.GONE);

        navigationView = findViewById(R.id.nav_view);
        drawerLayout = findViewById(R.id.activity_main_id);
        Menu menuNav = navigationView.getMenu();
        MenuItem cameraMenuItem = menuNav.findItem(R.id.nav_camera);
        // Disable camera button if no camera present
        cameraMenuItem.setEnabled(getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA));
        lockImageMenuItem = menuNav.findItem(R.id.nav_lock_image);
        // Make navigation (hamburger) menu do things.
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                        menuItem.setChecked(true);

                        int id = menuItem.getItemId();
                        switch(id) {
                            case R.id.nav_camera:
                                takePhoto();
                                // Reset to main menu with new image
                                selectMainMenu();
                                break;
                            case R.id.nav_image:
                                selectImageFromGallery();
                                // Reset to main menu with new image
                                selectMainMenu();
                                break;
                            case R.id.nav_lock_image:
                                lockImage();
                                break;
                            case R.id.nav_sample_ecg:
                                loadSampleEcg();
                                // Reset to main menu with new image
                                selectMainMenu();
                                break;
                            case R.id.nav_about:
                                about();
                                break;
                            case R.id.nav_help:
                                showHelp();
                                break;
                            case R.id.nav_preferences:
                                changeSettings();
                                break;
                        }
                        drawerLayout.closeDrawers();
                        return true;
                    }
                }
        );


        externalImageLoad = false;
        currentPdfUri = null;
        numberOfPdfPages = 0;
        currentPdfPageNumber = 0;
        useLargeFont = false;
        currentCaliperColor = R.color.default_caliper_color;
        currentHighlightColor = R.color.default_highlight_color;
        currentLineWidth = DEFAULT_LINE_WIDTH;

        // QTc formulas
        qtcFormulaMap = new HashMap<>();
        qtcFormulaMap.put(BAZETT, QtcFormula.qtcBzt);
        qtcFormulaMap.put(FRAMINGHAM, QtcFormula.qtcFrm);
        qtcFormulaMap.put(HODGES, QtcFormula.qtcHdg);
        qtcFormulaMap.put(FRIDERICIA, QtcFormula.qtcFrd);
        qtcFormulaMap.put(ALL, QtcFormula.qtcAll);

        // Caliper text positions
        textPositionMap = new HashMap<>();
        textPositionMap.put("centerAbove", Caliper.TextPosition.CenterAbove);
        textPositionMap.put("centerBelow", Caliper.TextPosition.CenterBelow);
        textPositionMap.put("left", Caliper.TextPosition.Left);
        textPositionMap.put("right", Caliper.TextPosition.Right);
        textPositionMap.put("top", Caliper.TextPosition.Top);
        textPositionMap.put("bottom", Caliper.TextPosition.Bottom);

        loadSettings();

        imageView = findViewById(R.id.imageView);
        imageView.setEnabled(true);
        if (!showStartImage && noSavedInstance) {
            imageView.setVisibility(View.INVISIBLE);
        }
        imageView.setScaleType(ImageView.ScaleType.CENTER);
        float max_zoom = 10.0f;
        imageView.setMaximumScale(max_zoom);
        imageView.setMinimumScale(0.3f);
        // We need to use MatrixChangeListener and not ScaleChangeListener
        // since the former only fires when scale has completely changed and
        // the latter fires while the scale is changing, so is inaccurate.
        imageView.setOnMatrixChangeListener(new MatrixChangeListener());
        imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (currentActionMode != null) { // || calipersView.isTweakingOrColoring()) {
                    return false;
                }
                startActionMode(imageCallBack);
                return true;
            }
        });

        if (noSavedInstance && Intent.ACTION_SEND.equals(action) && type != null) {
            if (type.startsWith(getString(R.string.image_type))) {
                handleSentImage();
            }
        } else if (noSavedInstance && Intent.ACTION_VIEW.equals(action) && type != null) {
            if (type.equals(getString(R.string.application_pdf_type))) {
                handlePDF();
            }
            if (type.startsWith(getString(R.string.image_type))) {
                handleImage();
            }
        }


        calipersView = findViewById(R.id.caliperView);
        calipersView.setMainActivity(this);


        shortAnimationDuration = getResources().getInteger(
                android.R.integer.config_shortAnimTime);

        // Set up action bar up top.  Note that icon on left for hamburger menu.
        actionBar = findViewById(R.id.action_bar);
        setSupportActionBar(actionBar);
        androidx.appcompat.app.ActionBar supportActionBar = Objects.requireNonNull(getSupportActionBar(),
                "Actionbar must not be null!");
        supportActionBar.setDisplayHomeAsUpEnabled(true);
        supportActionBar.setHomeAsUpIndicator(R.drawable.ic_menu);

        // Menu toolbar is on the bottom.
        menuToolbar = findViewById(R.id.menu_toolbar);

        // Create the myriad of buttons including tooltips as supported in
        // Lollipop and beyond.
        createButtons();

        horizontalCalibration = new Calibration(Caliper.Direction.HORIZONTAL, this);
        verticalCalibration = new Calibration(Caliper.Direction.VERTICAL, this);

        rrIntervalForQTc = 0.0;

        totalRotation = 0.0f;

        selectMainMenu();

        // entry point to load external pics/pdfs
        if (externalImageLoad) {
            updateImageView(externalImageBitmap);
            externalImageLoad = false;
        }

        onSharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                // show start image only has effect with restart
                Objects.requireNonNull(sharedPreferences, "Shared preferences must not be null!");
                if (key.equals(getString(R.string.show_start_image_key))) {
                    return;
                }
                if (key.equals(getString(R.string.time_calibration_key))) {
                    defaultTimeCalibration = sharedPreferences.getString(key,
                            getString(R.string.default_time_calibration_value));
                    horizontalCalibration.setCalibrationString(defaultTimeCalibration);
                    return; // no need to invalidate calipersView.
                }
                if (key.equals(getString(R.string.amplitude_calibration_key))) {
                    defaultAmplitudeCalibration = sharedPreferences.getString(key,
                            getString(R.string.default_amplitude_calibration_value));
                    verticalCalibration.setCalibrationString(defaultAmplitudeCalibration);
                    return; // no need to invalidate calipersView.
                }
                if (key.equals(getString(R.string.qtc_formula_key))) {
                    String qtcFormulaName = sharedPreferences.getString(key,
                            getString(R.string.qtc_formula_value));
                    qtcFormulaPreference = qtcFormulaMap.get(qtcFormulaName);
                    return;  // no need to invalidate calipersView
                }
                if (key.equals(getString(R.string.new_caliper_color_key))) {
                    currentCaliperColor = sharedPreferences.getInt(key,
                            R.color.default_caliper_color);
                    return;
                }
                if (key.equals(getString(R.string.new_highlight_color_key))) {
                    currentHighlightColor = sharedPreferences.getInt(key,
                            R.color.default_highlight_color);
                    for (Caliper c : calipersView.getCalipers()) {
                        c.setSelectedColor(currentHighlightColor);
                        if (c.isSelected()) {
                            c.setColor(currentHighlightColor);
                        }
                    }
                    return;
                }
                if (key.equals(getString(R.string.line_width_key))) {
                    try {
                        int lineWidth = Integer.parseInt(sharedPreferences.getString(key,
                                getString(R.string.default_line_width)));
                        currentLineWidth = lineWidth;
                        for (Caliper c : calipersView.getCalipers()) {
                            setLineWidth(c, lineWidth);
                        }
                    } catch (NumberFormatException ex) {
                        currentLineWidth = DEFAULT_LINE_WIDTH;
                        for (Caliper c : calipersView.getCalipers()) {
                            setLineWidth(c, currentLineWidth);
                        }
                        return;
                    }
                }
                if (key.equals(getString(R.string.use_large_font_key))) {
                    useLargeFont = sharedPreferences.getBoolean(key, false);
                    for (Caliper c : calipersView.getCalipers()) {
                        c.setFontSize(useLargeFont ? largeFontSize : smallFontSize);
                    }
                }
                if (key.equals(getString(R.string.round_msec_rate_key))) {
                    roundMsecRate = sharedPreferences.getBoolean(key, true);
                    for (Caliper c : calipersView.getCalipers()) {
                        c.setRoundMsecRate(roundMsecRate);
                    }
                }
                if (key.equals(getString(R.string.auto_position_text_key))) {
                    autoPositionText = sharedPreferences.getBoolean(key, true);
                    for (Caliper c : calipersView.getCalipers()) {
                        c.setAutoPositionText(autoPositionText);
                    }
                }
                if (key.equals(getString(R.string.time_caliper_text_position_key))) {
                    String timeCaliperTextPositionName = sharedPreferences.getString(key,
                            getString(R.string.time_caliper_text_position_value));
                    timeCaliperTextPositionPreference = textPositionMap.get(timeCaliperTextPositionName);
                    for (Caliper c : calipersView.getCalipers()) {
                        if (c.getDirection() == Caliper.Direction.HORIZONTAL) {
                            c.setTextPosition(timeCaliperTextPositionPreference);
                        }
                    }
                }
                if (key.equals(getString(R.string.amplitude_caliper_text_position_key))) {
                    String amplitudeCaliperTextPositionName = sharedPreferences.getString(key,
                            getString(R.string.amplitude_caliper_text_position_value));
                    amplitudeCaliperTextPositionPreference = textPositionMap.get(amplitudeCaliperTextPositionName);
                    for (Caliper c : calipersView.getCalipers()) {
                        if (c.getDirection() == Caliper.Direction.VERTICAL) {
                            c.setTextPosition(amplitudeCaliperTextPositionPreference);
                        }
                    }
                }
                calipersView.invalidate();
            }
        };

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);

        PackageInfo packageInfo = null;
        try {
            packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        int versionCode = packageInfo.versionCode;
        String versionName = packageInfo.versionName;
        EPSLog.log("VersionCode = " + versionCode);
        EPSLog.log("VersionName = " + versionName);
        version = new Version(this, prefs, versionName, versionCode);
        if (version.isNewInstallation() || version.isUpgrade()) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(getString(R.string.app_version_key), version.getVersionName());
            editor.apply();
        }

        layout = findViewById(R.id.frame_layout);
        ViewTreeObserver viewTreeObserver = layout.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @SuppressLint("NewApi")
            @SuppressWarnings("deprecation")
            @Override
            public void onGlobalLayout() {
                int androidVersion = Build.VERSION.SDK_INT;
                if (androidVersion >= Build.VERSION_CODES.JELLY_BEAN) {
                    layout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    layout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }

                if (noSavedInstance) {
                    addCaliperWithDirection(Caliper.Direction.HORIZONTAL);
                    scaleImageForImageView();
                }
                // else adjust the caliper positions, now that calipersView is created
                else {
                    for (Caliper c : calipersView.getCalipers()) {
                        float maxX = c.getDirection() == Caliper.Direction.HORIZONTAL
                                ? calipersView.getWidth()
                                : calipersView.getHeight();
                        float maxY = c.getDirection() == Caliper.Direction.HORIZONTAL
                                ? calipersView.getHeight()
                                : calipersView.getWidth();
                        c.setBar1Position(untransformCoordinate(c.getBar1Position(), maxX));
                        c.setBar2Position(untransformCoordinate(c.getBar2Position(), maxX));
                        c.setCrossbarPosition(untransformCoordinate(c.getCrossbarPosition(), maxY));
                    }
                    calipersView.invalidate();
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            rotateImageView();
                        }
                    }, 1000);

                }
            }

            private void rotateImageView() {
                imageView.setRotationBy(totalRotation);
            }
        });

        /// TODO: Reactivate this and make sure this works with version updates.
        // NB: we no longer provide quick start messages, so don't update them.
        //noinspection ConstantConditions
        if (force_first_run || getFirstRun(prefs)) {
            setRunned(prefs);
            // We no longer show update dialog after app updated.
//            AlertDialog.Builder builder = new AlertDialog.Builder(this);
//            builder.setTitle(getString(R.string.quick_start_title));
//            builder.setMessage(getString(R.string.quick_start_message));
//            builder.setPositiveButton(getString(R.string.ok_title), null);
//            builder.show();
        }

        if (externalImageLoad) {
            startActivity(intent);
        }
    }

    private void handleImage() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_STARTUP_IMAGE);
        }
        else {
            proceedToHandleImage();
        }
    }

    private void handleSentImage() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_STARTUP_SENT_IMAGE);
        }
        else {
            proceedToHandleSentImage();
        }
    }

    private void proceedToHandleImage() {
        try {
            Uri imageUri = getIntent().getData();
            if (imageUri != null) {
                externalImageLoad = true;
                externalImageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            }
        }
        catch (java.io.IOException e) {
            showFileErrorAlert();
        }
    }

    private void proceedToHandleSentImage() {
        try {
            Uri imageUri = getIntent().getParcelableExtra(Intent.EXTRA_STREAM);
            if (imageUri != null) {
                externalImageLoad = true;
                externalImageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            }
        }
        catch (java.io.IOException e) {
            showFileErrorAlert();
        }
    }

    // from http://stackoverflow.com/questions/10698360/how-to-convert-a-pdf-page-to-an-image-in-android
    private void handlePDF() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_STARTUP_PDF);
        }
        else {
            proceedToHandlePDF();
        }
    }

    private void proceedToHandlePDF() {
        Uri pdfUri = getIntent().getData();
        if (pdfUri != null) {
            UriPage uriPage = new UriPage();
            uriPage.uri = pdfUri;
            uriPage.pageNumber = 0;
            loadPDFAsynchronously(uriPage);
        }
    }

    private void loadPDFAsynchronously(UriPage uriPage) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            new NougatAsyncLoadPDF(this).execute(uriPage);
        }
        else {
            new AsyncLoadPDF(this).execute(uriPage);
        }
    }

    private Uri getTempUri(Uri uri) {
        try {
            InputStream is = getContentResolver().openInputStream(uri);
            ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

            // this is storage overwritten on each iteration with bytes
            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];

            // we need to know how may bytes were read to write them to the byteBuffer
            int len;
            if (is == null) {
                return null;
            }
            while ((len = is.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }
            byte[] bytes = byteBuffer.toByteArray();
            File file = createTmpPdfFile();
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            bos.write(bytes);
            bos.flush();
            bos.close();
            return Uri.fromFile(file);
        }
        catch (java.io.IOException e) {
            return null;
        }
    }

    private class UriPage {
        Uri uri;
        int pageNumber;
    }

    @TargetApi(25)
    private static class NougatAsyncLoadPDF extends AsyncTask<UriPage, Void, Bitmap> {
        private final WeakReference<MainActivity> activityWeakReference;

        private boolean isNewPdf;
        private String exceptionMessage = "";

        NougatAsyncLoadPDF(MainActivity context) {
            activityWeakReference = new WeakReference<>(context);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            activityWeakReference.get().findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
            Toast toast = Toast.makeText(activityWeakReference.get(), R.string.opening_pdf_message, Toast.LENGTH_SHORT);
            toast.show();

        }

        @Override
        protected Bitmap doInBackground(UriPage... params) {
            UriPage uriPage = params[0];
            Uri pdfUri = uriPage.uri;
            if (pdfUri == null) {
                if (activityWeakReference.get().currentPdfUri == null) {
                    // can't do anything if all is null
                    return null;
                }
                // use currently opened PDF
                pdfUri = activityWeakReference.get().currentPdfUri;
                isNewPdf = false;
            }
            else {
                // change Uri to a real file path
                pdfUri = activityWeakReference.get().getTempUri(pdfUri);
                // if getTempUri returns null then exception was thrown
                if (pdfUri == null) {
                    return null;
                }
                // close old currentPdfUri if possible
                if (activityWeakReference.get().currentPdfUri != null) {
                    File file = new File(activityWeakReference.get().currentPdfUri.getPath());
                    if (file.exists()) {
                        //noinspection ResultOfMethodCallIgnored
                        file.delete();
                    }
                }
                // retain PDF Uri for future page changes
                activityWeakReference.get().currentPdfUri = pdfUri;
                isNewPdf = true;
            }
            try {
                File file = new File(pdfUri.getPath());
                ParcelFileDescriptor fd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
                PdfRenderer renderer = new PdfRenderer(fd);
                activityWeakReference.get().numberOfPdfPages = renderer.getPageCount();
                activityWeakReference.get().currentPdfPageNumber = uriPage.pageNumber;
                PdfRenderer.Page page = renderer.openPage(activityWeakReference.get().currentPdfPageNumber);

                int width = page.getWidth();
                int height = page.getHeight();

                Bitmap bitmap = Bitmap.createBitmap(width * 3, height * 3, Bitmap.Config.ARGB_4444);


                Matrix matrix = new Matrix();
                matrix.postScale(3, 3);

                page.render(bitmap, null, matrix, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                page.close();
                renderer.close();
                fd.close();
                return bitmap;
            } catch (java.io.IOException e) {
                // catch out of memory errors and just don't load rather than crash
                exceptionMessage = e.getMessage();
                return null;
            }
        }

        protected void onPostExecute(Bitmap bitmap) {
            MainActivity activity = activityWeakReference.get();
            if (activity == null || activity.isFinishing()) {
                return;
            }
            if (bitmap != null) {
                // Set pdf bitmap directly, scaling screws it up
                activityWeakReference.get().imageView.setImageBitmap(bitmap);
                // must set visibility as imageview will be hidden if started with sample ecg hidden
                activityWeakReference.get().imageView.setVisibility(View.VISIBLE);
                activityWeakReference.get().imageView.setScale(activityWeakReference.get().imageView.getMinimumScale());
                if (isNewPdf) {
                    activityWeakReference.get().clearCalibration();
                }
            }
            else {
                Toast toast = Toast.makeText(activityWeakReference.get(), activityWeakReference.get().getString(R.string.pdf_error_message) +
                        LF + exceptionMessage, Toast.LENGTH_SHORT);
                toast.show();
            }
            activityWeakReference.get().findViewById(R.id.loadingPanel).setVisibility(View.GONE);
        }
    }


    private static class AsyncLoadPDF extends AsyncTask<UriPage,
            Void, Bitmap> {
        private boolean isNewPdf;
        private String exceptionMessage = "";

        private final WeakReference<MainActivity> activityWeakReference;


        AsyncLoadPDF(MainActivity context) {
            activityWeakReference = new WeakReference<>(context);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            activityWeakReference.get().findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
            Toast toast = Toast.makeText(activityWeakReference.get(), R.string.opening_pdf_message, Toast.LENGTH_SHORT);
            toast.show();

        }

        @Override
        protected Bitmap doInBackground(UriPage... params) {
            DecodeServiceBase decodeService = new DecodeServiceBase(new PdfContext());
            decodeService.setContentResolver(activityWeakReference.get().getContentResolver());
            UriPage uriPage = params[0];
            Uri pdfUri = uriPage.uri;
            if (pdfUri == null) {
                if (activityWeakReference.get().currentPdfUri == null) {
                    // can't do anything if all is null
                    return null;
                }
                // use currently opened PDF
                pdfUri = activityWeakReference.get().currentPdfUri;
                isNewPdf = false;
            }
            else {
                // change Uri to a real file path
                pdfUri = activityWeakReference.get().getTempUri(pdfUri);
                // if getTempUri returns null then exception was thrown
                if (pdfUri == null) {
                    return null;
                }
                // close old currentPdfUri if possible
                if (activityWeakReference.get().currentPdfUri != null) {
                    File file = new File(activityWeakReference.get().currentPdfUri.getPath());
                    if (file.exists()) {
                        //noinspection ResultOfMethodCallIgnored
                        file.delete();
                    }
                }
                // retain PDF Uri for future page changes
                activityWeakReference.get().currentPdfUri = pdfUri;
                isNewPdf = true;
            }
            // Not clear if this code can throw out of memory exception --
            // not well documented, so we'll be careful and catch any exception.
            try {
                decodeService.open(pdfUri);
                activityWeakReference.get().numberOfPdfPages = decodeService.getPageCount();
                activityWeakReference.get().currentPdfPageNumber = uriPage.pageNumber;
                PdfPage page = (PdfPage) decodeService.getPage(activityWeakReference.get().currentPdfPageNumber);

                int width = page.getWidth();
                int height = page.getHeight();

                Matrix matrix = new Matrix();
                matrix.preTranslate(0, height);
                matrix.preScale(1, -1);
                matrix.postScale(3, 3);

                Bitmap bitmap = page.render(new Rect(0, 0, width * 3, height * 3), matrix);
                page.recycle();
                decodeService.recycle();
                return bitmap;
            } catch (Exception e) {
                // catch out of memory errors and just don't load rather than crash
                exceptionMessage = e.getMessage();
                return null;
            }
        }

        protected void onPostExecute(Bitmap bitmap) {
            MainActivity activity = activityWeakReference.get();
            if (activity == null || activity.isFinishing()) {
                return;
            }
            if (bitmap != null) {
                // Set pdf bitmap directly, scaling screws it up
                activityWeakReference.get().imageView.setImageBitmap(bitmap);
                // must set visibility as imageview will be hidden if started with sample ecg hidden
                activityWeakReference.get().imageView.setVisibility(View.VISIBLE);
                activityWeakReference.get().imageView.setScale(activityWeakReference.get().imageView.getMinimumScale());
                if (isNewPdf) {
                    activityWeakReference.get().clearCalibration();
                }
            }
            else {
                Toast toast = Toast.makeText(activityWeakReference.get(), activityWeakReference.get().getString(R.string.pdf_error_message) +
                        LF + exceptionMessage, Toast.LENGTH_SHORT);
                toast.show();
            }
            activityWeakReference.get().findViewById(R.id.loadingPanel).setVisibility(View.GONE);
        }
    }

    private void resetPdf() {
        currentPdfUri = null;
        numberOfPdfPages = 0;
        currentPdfPageNumber = 0;
        enablePageButtons(false);
    }

    private void showPreviousPage() {
        if (currentPdfUri == null) {
           return;
        }
        if (--currentPdfPageNumber < 0) {
            currentPdfPageNumber = 0;
        }
        enablePageButtons(true);
        UriPage uriPage = new UriPage();
        uriPage.uri = null;
        uriPage.pageNumber = currentPdfPageNumber;
        loadPDFAsynchronously(uriPage);
    }

    private void showNextPage() {
        if (currentPdfUri == null) {
            return;
        }
        if (++currentPdfPageNumber > numberOfPdfPages - 1) {
            currentPdfPageNumber = numberOfPdfPages - 1;
        }
        enablePageButtons(true);
        UriPage uriPage = new UriPage();
        uriPage.uri = null;
        uriPage.pageNumber = currentPdfPageNumber;
        loadPDFAsynchronously(uriPage);
    }

    private void gotoPage() {
        if (currentPdfUri == null) {
            return;
        }
        // get page number from dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.go_to_page_title);
        final EditText input = new EditText(this);
        input.setLines(1);
        input.setMaxLines(1);
        input.setSingleLine(true);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint(R.string.page_number_hint);
        input.setSelection(0);
        builder.setView(input);
        builder.setPositiveButton(getString(R.string.ok_title), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialogResult = input.getText().toString();
                int pageNumber;
                try {
                    pageNumber = Integer.parseInt(dialogResult);
                } catch (NumberFormatException ex) {
                    dialog.cancel();
                    return;
                }
                if (pageNumber > numberOfPdfPages) {
                    pageNumber = numberOfPdfPages;
                }
                if (pageNumber < 1) {
                    pageNumber = 1;
                }
                currentPdfPageNumber = pageNumber - 1;
                enablePageButtons(true);
                UriPage uriPage = new UriPage();
                uriPage.uri = null;
                uriPage.pageNumber = currentPdfPageNumber;
                loadPDFAsynchronously(uriPage);
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

    public boolean getFirstRun(SharedPreferences prefs) {
      return prefs.getBoolean("firstRun" + version.getVersionName(), true);
    }

    public void setRunned(SharedPreferences prefs) {
        SharedPreferences.Editor edit = prefs.edit();
        edit.putBoolean("firstRun" + version.getVersionName(), false);
        edit.apply();
    }


    void loadSettings() {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(this);
        showStartImage = sharedPreferences.getBoolean(
                getString(R.string.show_start_image_key), true);
        roundMsecRate = sharedPreferences.getBoolean(getString(R.string.round_msec_rate_key), true);
        defaultTimeCalibration = sharedPreferences.getString(getString(
                R.string.time_calibration_key), getString(R.string.default_time_calibration_value));
        defaultAmplitudeCalibration = sharedPreferences.getString(
                getString(R.string.amplitude_calibration_key), getString(R.string.default_amplitude_calibration_value));
        useLargeFont = sharedPreferences.getBoolean(getString(R.string.use_large_font_key), false);
        String qtcFormulaName = sharedPreferences.getString(getString(R.string.qtc_formula_key),
                getString(R.string.qtc_formula_value));
        qtcFormulaPreference = qtcFormulaMap.get(qtcFormulaName);
        String timeCaliperTextPositionName = sharedPreferences.getString(getString(R.string.time_caliper_text_position_key),
                getString(R.string.time_caliper_text_position_value));

        autoPositionText = sharedPreferences.getBoolean(getString(R.string.auto_position_text_key), true);
        timeCaliperTextPositionPreference = textPositionMap.get(timeCaliperTextPositionName);
        String amplitudeCaliperTextPositionName = sharedPreferences.getString(getString(R.string.amplitude_caliper_text_position_key),
                getString(R.string.amplitude_caliper_text_position_value));
        amplitudeCaliperTextPositionPreference = textPositionMap.get(amplitudeCaliperTextPositionName);
        currentCaliperColor = sharedPreferences.getInt(getString(R.string.new_caliper_color_key),
                R.color.default_caliper_color);
        currentHighlightColor = sharedPreferences.getInt(getString(R.string.new_highlight_color_key),
                R.color.default_highlight_color);
        try {
            currentLineWidth = Integer.parseInt(sharedPreferences.getString(getString(R.string.line_width_key),
                    Integer.valueOf(DEFAULT_LINE_WIDTH).toString()));
        } catch (NumberFormatException ex) {
            currentLineWidth = DEFAULT_LINE_WIDTH;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        EPSLog.log("onResume");
    }

    private void scaleImageForImageView() {
        Drawable image = imageView.getDrawable();
        if (image == null) {
            return;
        }
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
//        float landscapeWidth = Math.max(screenHeight, screenWidth);
//        float portraitHeight = Math.max(screenHeight, screenWidth) - verticalSpace;
        float landscapeHeight = Math.min(screenHeight, screenWidth) - verticalSpace;
        float ratio;
        if (imageWidth > imageHeight) {
            ratio = portraitWidth / imageWidth;
        }
        else {
            ratio = landscapeHeight / imageHeight;
        }
        Bitmap bitmap = ((BitmapDrawable)image).getBitmap();
        if (bitmap == null) {
            return;
        }
        int bitmapWidth = bitmap.getWidth();
        int bitmapHeight = bitmap.getHeight();
        Matrix matrix = new Matrix();
        matrix.postScale(ratio, ratio);
        Bitmap scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmapWidth,
                bitmapHeight, matrix, true);
        BitmapDrawable result = new BitmapDrawable(getResources(), scaledBitmap);

        imageView.setImageDrawable(result);
    }

    private Pair<Integer, Integer> getScreenDimensions () {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int height = size.y;
        int width = size.x;
        return new Pair<>(width, height);
    }

    private float getStatusBarHeight() {
        Rect rect = new Rect();
        Window window = getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rect);
        return rect.top;
    }

    private void proceedToStoreBitmap() {
        Bitmap imageBitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        // for efficiency, don't bother writing the bitmap to a file if it hasn't changed
        if (imageBitmap != null && !imageBitmap.sameAs(previousBitmap)) {
            storeBitmapToTempFile(imageBitmap);
        }
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        EPSLog.log("onSaveInstanceState");
        super.onSaveInstanceState(outState);
        outState.putFloat(getString(R.string.imageview_scale_key), imageView.getScale());
        outState.putFloat(getString(R.string.total_rotation_key), totalRotation);
        outState.putBoolean(getString(R.string.image_locked_key), imageIsLocked);
        outState.putBoolean(getString(R.string.multipage_pdf_key), numberOfPdfPages > 0);
        outState.putBoolean(getString(R.string.a_caliper_is_marching_key), calipersView.isACaliperIsMarching());

        // To avoid FAILED BINDER TRANSACTION issue (which is ignored up until Android 24,
        // save to temp file instead of storing bitmap in bundle.
        // See http://stackoverflow.com/questions/36007540/failed-binder-transaction-in-android
        //outState.putParcelable("Image", ((BitmapDrawable) imageView.getDrawable()).getBitmap());

        // also check permissions first
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_STORE_BITMAP);

        }
        else {
            proceedToStoreBitmap();
        }

        // Calibration
        // must use getRawUnits here, otherwise original calibration units are lost
        outState.putString(getString(R.string.hcal_units_key), horizontalCalibration.getRawUnits());
        outState.putString(getString(R.string.hcal_string_key), horizontalCalibration.getCalibrationString());
        outState.putBoolean(getString(R.string.hcal_display_rate_key), horizontalCalibration.getDisplayRate());
        outState.putFloat(getString(R.string.hcal_original_zoom_key), horizontalCalibration.getOriginalZoom());
        outState.putFloat(getString(R.string.hcal_current_zoom_key), horizontalCalibration.getCurrentZoom());
        outState.putBoolean(getString(R.string.hcal_is_calibrated_key), horizontalCalibration.isCalibrated());
        outState.putFloat(getString(R.string.hcal_original_cal_factor_key), horizontalCalibration.getOriginalCalFactor());

        outState.putString(getString(R.string.vcal_units_key), verticalCalibration.getRawUnits());
        outState.putString(getString(R.string.vcal_string_key), verticalCalibration.getCalibrationString());
        outState.putBoolean(getString(R.string.vcal_display_rate_key), verticalCalibration.getDisplayRate());
        outState.putFloat(getString(R.string.vcal_original_zoom_key), verticalCalibration.getOriginalZoom());
        outState.putFloat(getString(R.string.vcal_current_zoom_key), verticalCalibration.getCurrentZoom());
        outState.putBoolean(getString(R.string.vcal_is_calibrated_key), verticalCalibration.isCalibrated());
        outState.putFloat(getString(R.string.vcal_original_cal_factor_key), verticalCalibration.getOriginalCalFactor());
        // save calipers
        for (int i = 0; i < calipersCount(); i++) {
            Caliper c = calipersView.getCalipers().get(i);
            outState.putString(i + getString(R.string.caliper_direction_key),
                    c.getDirection() == Caliper.Direction.HORIZONTAL ?
                            getString(R.string.horizontal_direction) : getString(R.string.vertical_direction));
            // maxX normalizes bar and crossbar positions regardless of caliper direction,
            // i.e. X is direction for bars and Y is direction for crossbars.
            float maxX = c.getDirection() == Caliper.Direction.HORIZONTAL
                    ? calipersView.getWidth()
                    : calipersView.getHeight();
            float maxY = c.getDirection() == Caliper.Direction.HORIZONTAL
                    ? calipersView.getHeight()
                    : calipersView.getWidth();
            outState.putFloat(i + getString(R.string.caliper_bar1_position_key),
                    transformCoordinate(c.getBar1Position(), maxX));
            outState.putFloat(i + getString(R.string.caliper_bar2_position_key),
                    transformCoordinate(c.getBar2Position(), maxX));
            outState.putFloat(i + getString(R.string.caliper_crossbar_position_key),
                    transformCoordinate(c.getCrossbarPosition(), maxY));
            outState.putBoolean(i + getString(R.string.caliper_selected_key), c.isSelected());
            outState.putBoolean(i + getString(R.string.is_angle_caliper_key), c.isAngleCaliper());
            outState.putInt(i + getString(R.string.unselected_color_restore_key), c.getUnselectedColor());
            outState.putBoolean(i + getString(R.string.marching_caliper_restore_key), c.isMarching());

            if (c.isAngleCaliper()) {
                outState.putDouble(i + getString(R.string.angle_b1_key), ((AngleCaliper)c).getBar1Angle());
                outState.putDouble(i + getString(R.string.angle_b2_key), ((AngleCaliper)c).getBar2Angle());
            }
            else {
                outState.putDouble(i + getString(R.string.angle_b1_key), 0.0);
                outState.putDouble(i + getString(R.string.angle_b2_key), 0.0);
            }
        }
        outState.putInt(getString(R.string.calipers_count_key), calipersCount());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        EPSLog.log("onRestoreInstanceState");
        imageIsLocked = savedInstanceState.getBoolean(getString(R.string.image_locked_key));
        lockImage(imageIsLocked);
        calipersView.setACaliperIsMarching(savedInstanceState.getBoolean(getString(R.string.a_caliper_is_marching_key)));

        boolean isMultipagePdf = savedInstanceState.getBoolean(getString(R.string.multipage_pdf_key));

        // Bitmap now passed via temporary file
        //Bitmap image = savedInstanceState.getParcelable("Image");
        Bitmap image = getBitmapFromTempFile();
        imageView.setImageBitmap(image);
        previousBitmap = image;

        totalRotation = savedInstanceState.getFloat(getString(R.string.total_rotation_key));

        float scale = Math.max(savedInstanceState.getFloat(getString(R.string.imageview_scale_key)), imageView.getMinimumScale());
        scale = Math.min(scale, imageView.getMaximumScale());
        imageView.setScale(scale, true);

        // Calibration
        horizontalCalibration.setUnits(savedInstanceState.getString(getString(R.string.hcal_units_key)));
        horizontalCalibration.setCalibrationString(savedInstanceState.getString(getString(R.string.hcal_string_key)));
        horizontalCalibration.setDisplayRate(savedInstanceState.getBoolean(getString(R.string.hcal_display_rate_key)));
        horizontalCalibration.setOriginalZoom(savedInstanceState.getFloat(getString(R.string.hcal_original_zoom_key)));
        horizontalCalibration.setCurrentZoom(savedInstanceState.getFloat(getString(R.string.hcal_current_zoom_key)));
        horizontalCalibration.setCalibrated(savedInstanceState.getBoolean(getString(R.string.hcal_is_calibrated_key)));
        horizontalCalibration.setOriginalCalFactor(savedInstanceState.getFloat(getString(R.string.hcal_original_cal_factor_key)));

        verticalCalibration.setUnits(savedInstanceState.getString(getString(R.string.vcal_units_key)));
        verticalCalibration.setCalibrationString(savedInstanceState.getString(getString(R.string.vcal_string_key)));
        verticalCalibration.setDisplayRate(savedInstanceState.getBoolean(getString(R.string.vcal_display_rate_key)));
        verticalCalibration.setOriginalZoom(savedInstanceState.getFloat(getString(R.string.vcal_original_zoom_key)));
        verticalCalibration.setCurrentZoom(savedInstanceState.getFloat(getString(R.string.vcal_current_zoom_key)));
        verticalCalibration.setCalibrated(savedInstanceState.getBoolean(getString(R.string.vcal_is_calibrated_key)));
        verticalCalibration.setOriginalCalFactor(savedInstanceState.getFloat(getString(R.string.vcal_original_cal_factor_key)));

        // restore calipers
        int calipersCount = savedInstanceState.getInt(getString(R.string.calipers_count_key));
        for (int i = 0; i < calipersCount; i++) {
            String directionString = savedInstanceState.getString(i + getString(R.string.caliper_direction_key));
            boolean isAngleCaliper = savedInstanceState.getBoolean(i + getString(R.string.is_angle_caliper_key));
            boolean isMarching = savedInstanceState.getBoolean(i + getString(R.string.marching_caliper_restore_key));
            if (directionString == null) {
                // something very wrong, give up on restoring calipers
                return;
            }
            int unselectedColor = savedInstanceState.getInt(i + getString(R.string.unselected_color_restore_key));
            Caliper.Direction direction = directionString.equals(getString(R.string.horizontal_direction)) ?
                    Caliper.Direction.HORIZONTAL : Caliper.Direction.VERTICAL;
            float bar1Position = savedInstanceState.getFloat(i + getString(R.string.caliper_bar1_position_key));
            float bar2Position = savedInstanceState.getFloat(i + getString(R.string.caliper_bar2_position_key));
            double bar1Angle = savedInstanceState.getDouble(i + getString(R.string.angle_b1_key));
            double bar2Angle = savedInstanceState.getDouble(i + getString(R.string.angle_b2_key));
            float crossbarPosition = savedInstanceState.getFloat(i + getString(R.string.caliper_crossbar_position_key));
            boolean selected = savedInstanceState.getBoolean(i + getString(R.string.caliper_selected_key));

            Caliper c;
            if (isAngleCaliper) {
                c = new AngleCaliper();
                ((AngleCaliper)c).setVerticalCalibration(verticalCalibration);
            }
            else {
                c = new Caliper();
            }
            c.setDirection(direction);
            c.setxOffset(getResources().getDimension(R.dimen.caliper_text_offset));
            c.setyOffset(getResources().getDimension(R.dimen.caliper_text_offset));
            c.setBar1Position(bar1Position);
            c.setBar2Position(bar2Position);
            c.setCrossbarPosition(crossbarPosition);
            c.setSelected(selected);
            c.setUnselectedColor(unselectedColor);
            c.setSelectedColor(currentHighlightColor);
            c.setColor(c.isSelected() ? currentHighlightColor : unselectedColor);
            setLineWidth(c, currentLineWidth);
            c.setFontSize(useLargeFont ? largeFontSize : smallFontSize);
            c.setRoundMsecRate(roundMsecRate);
            c.setAutoPositionText(autoPositionText);
            c.setMarching(isMarching);
            if (c.getDirection() == Caliper.Direction.HORIZONTAL) {
                c.setCalibration(horizontalCalibration);
                c.setTextPosition(timeCaliperTextPositionPreference);
            }
            else {
                c.setCalibration(verticalCalibration);
                c.setTextPosition(amplitudeCaliperTextPositionPreference);
            }
            if (c.isAngleCaliper()) {
                ((AngleCaliper)c).setVerticalCalibration(verticalCalibration);
                ((AngleCaliper)c).setBar1Angle(bar1Angle);
                ((AngleCaliper)c).setBar2Angle(bar2Angle);
            }


            calipersView.getCalipers().add(c);

        }
        if (isMultipagePdf) {
            Toast toast = Toast.makeText(this, R.string.multipage_pdf_warning,
                    Toast.LENGTH_LONG);
            toast.show();
        }
    }

    private void storeBitmapToTempFile(Bitmap bitmap) {
        try {
            File file = new File(getCacheDir() + getString(R.string.temp_bitmap_file_name));
            FileOutputStream fOut = new FileOutputStream(file);

            bitmap.compress(Bitmap.CompressFormat.PNG, 85, fOut);
            fOut.flush();
            fOut.close();
        }
        catch (java.io.IOException ex) {
            Toast toast = Toast.makeText(this, R.string.temp_image_file_warning, Toast.LENGTH_SHORT);
            toast.show();
            EPSLog.log("Could not store temp file");
        }
    }

    private Bitmap getBitmapFromTempFile() {

            String path = getCacheDir() + getString(R.string.temp_bitmap_file_name);
            return BitmapFactory.decodeFile(path);
    }

    // return coordinate position ratio will be between 0 and 1.0
    private float transformCoordinate(float coord, float maxDim) {
        return coord / maxDim;
    }

    private float untransformCoordinate(float ratio, float maxDim) {
        return ratio * maxDim;
    }

    @Override
    public void onClick(View v) {
        if (v == cancelAddCaliperButton) {
            returnFromAddCaliperMenu();
        } else if (v == doneCalibrationButton
                || v == cancelQTcButton
                || v == cancelQTcMeasurementButton) {
            selectMainMenu();
        } else if (v == calibrateButton) {
            setupCalibration();
        } else if (v == rotateImageLeftButton) {
            rotateImage(-90.0f);
        } else if (v == rotateImageRightButton) {
            rotateImage(90.0f);
        } else if (v == tweakImageLeftButton) {
            rotateImage(-1.0f);
        } else if (v == tweakImageRightButton) {
            rotateImage(1.0f);
        } else if (v == microTweakImageLeftButton) {
            rotateImage(-0.1f);
        } else if (v == microTweakImageRightButton) {
            rotateImage(0.1f);
        } else if (v == resetImageButton) {
            resetImage();
        } else if (v == rotateDoneButton) {
            gotoPreviousMenu();
        } else if (v == horizontalCaliperButton) {
            addTimeCaliper();
        } else if (v == verticalCaliperButton) {
            addAmplitudeCaliper();
        } else if (v == angleCaliperButton) {
            addAngleCaliper();
        } else if (v == setCalibrationButton) {
            setCalibration();
        } else if (v == clearCalibrationButton) {
            clearCalibration();
        } else if (v == intervalRateButton) {
            toggleIntervalRate();
        } else if (v == meanRateButton) {
            meanRR();
        } else if (v == qtcButton) {
            calculateQTc();
        } else if (v == measureRRButton) {
            qtcMeasureRR();
        } else if (v == measureQTButton) {
            doQTcCalculation();
        } else if (v == previousPageButton) {
            showPreviousPage();
        } else if (v == nextPageButton) {
            showNextPage();
        } else if (v == gotoPageButton) {
            gotoPage();
        } else if (v == pdfDoneButton) {
            gotoPreviousMenu();
        } else if (v == colorDoneButton) {
            calipersView.setTweakingOrColoring(false);
            gotoPreviousMenu();
        } else if (v == tweakDoneButton) {
            calipersView.setTweakingOrColoring(false);
            tweakDone();
        } else if (v == leftButton) {
            left();
        } else if (v == rightButton) {
            right();
        } else if (v == microLeftButton) {
            microLeft();
        } else if (v == microRightButton) {
            microRight();
        } else if (v == upButton) {
            up();
        } else if (v == downButton) {
            down();
        } else if (v == microUpButton) {
            microUp();
        } else if (v == microDownButton) {
            microDown();
        } else if (v == microDoneButton) {
            microDone();
        }
    }

    private void addToolTip(Button button, CharSequence text) {
        // Ignore tooltips in unsupported versions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            button.setTooltipText(text);
        }
    }

    // N.B. Android doesn't allow sharing of buttons with different parents.
    // Thus, we need separate cancel buttons, for example, all of which do
    // the same thing, but belong to different menus.
    private void createButtons() {
        // Main/Caliper menu
        calibrateButton = createButton(getString(R.string.calibrate_button_title));
        addToolTip(calibrateButton, getString(R.string.setup_calibration_tooltip));
        intervalRateButton = createButton(getString(R.string.interval_rate_button_title));
        addToolTip(intervalRateButton, getString(R.string.int_rate_tooltip));
        meanRateButton = createButton(getString(R.string.mean_rate_button_title));
        addToolTip(meanRateButton, getString(R.string.mean_rate_tooltip));
        qtcButton = createButton(getString(R.string.qtc_button_title));
        addToolTip(qtcButton, getString(R.string.qtc_tooltip));
        // PDF menu
        previousPageButton = createButton(getString(R.string.previous_button_label));
        nextPageButton = createButton(getString(R.string.next_button_label));
        gotoPageButton = createButton(getString(R.string.go_to_page));
        pdfDoneButton = createButton(getString(R.string.done_button_title));
        // Add Caliper menu
        horizontalCaliperButton = createButton(getString(R.string.horizontal_caliper_button_title));
        verticalCaliperButton = createButton(getString(R.string.vertical_caliper_button_title));
        angleCaliperButton = createButton(getString(R.string.angle_caliper_button_title));
        cancelAddCaliperButton = createButton(getString(R.string.cancel_button_title));
        // Adjust Image menu
        rotateImageRightButton = createButton(getString(R.string.rotate_image_right_button_title));
        rotateImageLeftButton = createButton(getString(R.string.rotate_image_left_button_title));
        tweakImageRightButton = createButton(getString(R.string.tweak_image_right_button_title));
        tweakImageLeftButton = createButton(getString(R.string.tweak_image_left_button_title));
        microTweakImageRightButton = createButton(getString(R.string.micro_tweak_image_right_button_title));
        microTweakImageLeftButton = createButton(getString(R.string.micro_tweak_image_left_button_title));
        resetImageButton = createButton(getString(R.string.reset_image_button_title));
        rotateDoneButton = createButton(getString(R.string.done_button_title));
        // Calibration menu
        setCalibrationButton = createButton(getString(R.string.set_calibration_button_title));
        addToolTip(setCalibrationButton, getString(R.string.set_calibration_tooltip));
        clearCalibrationButton = createButton(getString(R.string.clear_calibration_button_title));
        addToolTip(clearCalibrationButton, getString(R.string.clear_calibration_tooltip));
        doneCalibrationButton = createButton(getString(R.string.done_button_title));
        // QTc menu
        measureRRButton = createButton(getString(R.string.measure_button_label));
        addToolTip(measureRRButton, getString(R.string.qtc_step_1_tooltip));
        cancelQTcButton = createButton(getString(R.string.cancel_button_title));
        measureQTButton = createButton(getString(R.string.measure_button_label));
        addToolTip(measureQTButton, getString(R.string.qtc_step_2_tooltip));
        cancelQTcMeasurementButton = createButton(getString(R.string.cancel_button_title));
        // Color menu
        colorDoneButton = createButton(getString(R.string.done_button_title));
        // Tweak menu
        tweakDoneButton = createButton(getString(R.string.done_button_title));
        // MicroMovement menu
        leftButton = createButton(getString(R.string.left_label));
        rightButton = createButton(getString(R.string.right_label));
        microLeftButton = createButton(getString(R.string.micro_left_label));
        microRightButton = createButton(getString(R.string.micro_right_label));
        upButton = createButton(getString(R.string.up_label));
        downButton = createButton(getString(R.string.down_label));
        microUpButton = createButton(getString(R.string.micro_up_label));
        microDownButton = createButton(getString(R.string.micro_down_label));
        microDoneButton = createButton(getString(R.string.done_button_title));

        upDownButtons = Arrays.asList(upButton, downButton, microUpButton, microDownButton);
        rightLeftButtons = Arrays.asList(leftButton, rightButton, microLeftButton, microRightButton);
    }

    private Button createButton(String text) {
        Button button = new Button(this);
        button.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        button.setText(text);
        button.setOnClickListener(this);
        return button;
    }

    private void createMainMenu() {
        ArrayList<TextView> buttons = new ArrayList<>();
        buttons.add(calibrateButton);
        buttons.add(intervalRateButton);
        buttons.add(meanRateButton);
        buttons.add(qtcButton);
        mainMenu = createMenu(buttons);
    }

    private void createPDFMenu() {
        ArrayList<TextView> buttons = new ArrayList<>();
        buttons.add(previousPageButton);
        buttons.add(nextPageButton);
        buttons.add(gotoPageButton);
        buttons.add(pdfDoneButton);
        pdfMenu = createMenu(buttons);
    }

    private void createAddCaliperMenu() {
        ArrayList<TextView> buttons = new ArrayList<>();
        buttons.add(horizontalCaliperButton);
        buttons.add(verticalCaliperButton);
        buttons.add(angleCaliperButton);
        buttons.add(cancelAddCaliperButton);
        addCaliperMenu = createMenu(buttons);
    }

    private void createRotateImageMenu() {
        ArrayList<TextView> buttons = new ArrayList<>();
        buttons.add(rotateImageLeftButton);
        buttons.add(rotateImageRightButton);
        buttons.add(tweakImageLeftButton);
        buttons.add(tweakImageRightButton);
        buttons.add(microTweakImageLeftButton);
        buttons.add(microTweakImageRightButton);
        buttons.add(resetImageButton);
        buttons.add(rotateDoneButton);
        rotateImageMenu = createMenu(buttons);
    }

    private void createCalibrationMenu() {
        ArrayList<TextView> buttons = new ArrayList<>();
        buttons.add(setCalibrationButton);
        buttons.add(clearCalibrationButton);
        buttons.add(doneCalibrationButton);
        calibrationMenu = createMenu(buttons);
    }

    private void createQTcStep1Menu() {
        ArrayList<TextView> items = new ArrayList<>();
        TextView textView = new TextView(this);
        textView.setText(getString(R.string.select_rr_intervals_message));
        items.add(textView);
        items.add(measureRRButton);
        items.add(cancelQTcButton);
        qtcStep1Menu = createMenu(items);
    }

    private void createQTcStep2Menu() {
        ArrayList<TextView> items = new ArrayList<>();
        TextView textView = new TextView(this);
        textView.setText(getString(R.string.qt_interval_query));
        items.add(textView);
        items.add(measureQTButton);
        items.add(cancelQTcMeasurementButton);
        qtcStep2Menu = createMenu(items);
    }

    private void createColorMenu() {
        ArrayList<TextView> items = new ArrayList<>();
        TextView textView = new TextView(this);
        textView.setText(R.string.color_menu_text);
        items.add(textView);
        items.add(colorDoneButton);
        colorMenu = createMenu(items);
    }

    private void createTweakMenu() {
        ArrayList<TextView> items = new ArrayList<>();
        TextView textView = new TextView(this);
        textView.setText(R.string.tweak_menu_text);
        items.add(textView);
        items.add(tweakDoneButton);
        tweakMenu = createMenu(items);
    }

    private void createMicroMovementMenu() {
        ArrayList<TextView> items = new ArrayList<>();
        microTextView = new TextView(this);
        items.add(microTextView);
        items.add(leftButton);
        items.add(rightButton);
        items.add(upButton);
        items.add(downButton);
        items.add(microLeftButton);
        items.add(microRightButton);
        items.add(microUpButton);
        items.add(microDownButton);
        items.add(microDoneButton);
        microMovementMenu = createMenu(items);
    }

    private void pushToolbarMenuStack(ToolbarMenu menu) {
        toolbarMenuDeque.addLast(menu);
    }

    private ToolbarMenu popToolbarMenuStack() {
        ToolbarMenu menu = toolbarMenuDeque.pollLast();
        if (menu == null) {
            return ToolbarMenu.Main;
        }
        return menu;
    }

    private void clearToolbarMenuStack() {
        toolbarMenuDeque.clear();
    }

    // Select menus
    public void selectMenu(ToolbarMenu menu) {
        switch (menu) {
            case Main:
                selectMainMenu();
                break;
            case AddCaliper:
                selectAddCaliperMenu();
                break;
            case Rotate:
                selectRotateImageMenu();
                break;
            case PDF:
                selectPDFMenu();
                break;
            case Color:
                selectColorMenu();
                break;
            case QTc1:
                selectQTcStep1Menu();
                break;
            case QTc2:
                selectQTcStep2Menu();
                break;
            case Calibration:
                selectCalibrationMenu();
                break;
            case Tweak:
                selectTweakMenu();
                break;
            case Move:
                // FIXME: can't select micromovement menu
                break;
            default:
                selectMainMenu();
                break;
        }
    }
    
    private void selectMainMenu() {
        EPSLog.log("selectMainMenu called");
        if (mainMenu == null) {
            createMainMenu();
        }
        // remove ActionModes if present
        if (currentActionMode != null) {
            currentActionMode.finish();
            currentActionMode = null;
        }
        calipersView.setTweakingOrColoring(false);
        selectMenu(mainMenu);
        clearToolbarMenuStack();
        pushToolbarMenuStack(ToolbarMenu.Main);
        boolean enable = horizontalCalibration.canDisplayRate();
        intervalRateButton.setEnabled(enable);
        meanRateButton.setEnabled(enable);
        qtcButton.setEnabled(enable);
        calipersView.setLocked(false);
        calipersView.setAllowTweakPosition(false);
        calipersView.setAllowColorChange(false);
        inQtc = false;
    }

    private void selectPDFMenu() {
        if (pdfMenu == null) {
            createPDFMenu();
        }
        boolean enable = (numberOfPdfPages  > 0);
        enablePageButtons(enable);
        selectMenu(pdfMenu);
    }

    private void enablePageButtons(boolean enable) {
        previousPageButton.setEnabled(enable);
        nextPageButton.setEnabled(enable);
        if (enable) {
            if (currentPdfPageNumber <= 0) {
                previousPageButton.setEnabled(false);
            }
            if (currentPdfPageNumber >= numberOfPdfPages -1) {
                nextPageButton.setEnabled(false);
            }
        }
    }

    private void gotoPreviousMenu() {
        ToolbarMenu menu = popToolbarMenuStack();
        selectMenu(menu);
    }

    private void selectAddCaliperMenu() {
        if (addCaliperMenu == null) {
            createAddCaliperMenu();
        }
        if (toolbarMenuDeque.peekLast() == ToolbarMenu.AddCaliper) {
            toolbarMenuDeque.removeLast();
            gotoPreviousMenu();
            return;
        }
        pushToolbarMenuStack(ToolbarMenu.AddCaliper);
        selectMenu(addCaliperMenu);
    }

    private void returnFromAddCaliperMenu() {
        if (toolbarMenuDeque.peekLast() == ToolbarMenu.AddCaliper) {
            toolbarMenuDeque.removeLast();
            gotoPreviousMenu();
        }
        else {
            gotoPreviousMenu();
        }
    }

    private void selectRotateImageMenu() {
        if (rotateImageMenu == null) {
            createRotateImageMenu();
        }
        selectMenu(rotateImageMenu);
    }

    private void selectCalibrationMenu() {
        if (calibrationMenu == null) {
            createCalibrationMenu();
        }
        pushToolbarMenuStack(ToolbarMenu.Calibration);
        selectMenu(calibrationMenu);
    }

    private void selectQTcStep1Menu() {
        if (qtcStep1Menu == null) {
            createQTcStep1Menu();
        }
        pushToolbarMenuStack(ToolbarMenu.QTc1);
        selectMenu(qtcStep1Menu);
    }

    private void selectQTcStep2Menu() {
        if (qtcStep2Menu == null) {
            createQTcStep2Menu();
        }
        pushToolbarMenuStack(ToolbarMenu.QTc2);
        selectMenu(qtcStep2Menu);
        inQtc = true;
    }

    public void selectColorMenu() {
        if (!thereAreCalipers()) {
            noCalipersAlert();
            return;
        }
        if (colorMenu == null) {
            createColorMenu();
        }
        selectMenu(colorMenu);
        calipersView.setAllowColorChange(true);
    }

    public void selectTweakMenu() {
        if (!thereAreCalipers()) {
            noCalipersAlert();
            return;
        }
        if (tweakMenu == null) {
            createTweakMenu();
        }
        selectMenu(tweakMenu);
        calipersView.setAllowTweakPosition(true);
    }



    //public because used by CalipersView
    public void selectMicroMovementMenu(Caliper c, Caliper.Component component) {
        if (microMovementMenu == null) {
            createMicroMovementMenu();
        }
        selectMenu(microMovementMenu);
        if (component == Caliper.Component.Crossbar) {
            setButtonsVisibility(upDownButtons, View.VISIBLE);
            setButtonsVisibility(rightLeftButtons, View.VISIBLE);
            microTextView.setText(c.isAngleCaliper() ? getString(R.string.move_angle_apex_text) : getString(R.string.move_crossbar_text));
        }
        else if (c.getDirection() == Caliper.Direction.HORIZONTAL) {
            setButtonsVisibility(upDownButtons, View.GONE);
            setButtonsVisibility(rightLeftButtons, View.VISIBLE);
            switch (component) {
                case Bar1:
                    microTextView.setText(getString(R.string.left_bar_label));
                    break;
                case Bar2:
                    microTextView.setText(getString(R.string.right_bar_label));
                    break;
                default:
                    break;
            }
        }
        else {      // vertical caliper
            setButtonsVisibility(upDownButtons, View.VISIBLE);
            setButtonsVisibility(rightLeftButtons, View.GONE);
            switch (component) {
                case Bar1:
                    microTextView.setText(getString(R.string.up_bar_label));
                    break;
                case Bar2:
                    microTextView.setText(getString(R.string.down_bar_label));
                    break;
                default:
                    break;
            }
        }
    }

    private void setButtonsVisibility(List<Button> buttons, int visibility) {
        for (Button button : buttons) {
            button.setVisibility(visibility);
        }
    }

    private void selectMenu(HorizontalScrollView menu) {
        clearToolbar();
        menuToolbar.addView(menu);
    }

    private HorizontalScrollView createMenu(ArrayList<TextView> items) {
        HorizontalScrollView scrollView = new HorizontalScrollView(this);
        HorizontalScrollView.LayoutParams layoutParams = new HorizontalScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        scrollView.setLayoutParams(layoutParams);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setLayoutParams(layoutParams);
        for (TextView item : items) {
            layout.addView(item);
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
        EPSLog.log("onCreateOptionsMenu");
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
        if (id == R.id.add_caliper) {
            selectAddCaliperMenu();
            return true;
        }
        if (id == android.R.id.home) {
            drawerLayout.openDrawer(GravityCompat.START);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void lockImage() {
        imageIsLocked = !imageIsLocked;
        lockImage(imageIsLocked);
    }

    private void lockImage(boolean lock) {
        imageView.setEnabled(!lock);
        calipersView.setLockImage(lock);
        if (lock) {
            lockImageMenuItem.setTitle(getString(R.string.drawer_unlock_image));
            lockImageMenuItem.setIcon(R.drawable.ic_lock_open);
        }
        else {
            lockImageMenuItem.setTitle(getString(R.string.drawer_lock_image));
            lockImageMenuItem.setIcon(R.drawable.ic_lock);
        }
        calipersView.invalidate();
    }

    private void changeSettings() {
        Intent i = new Intent(this, Prefs.class);
        startActivity(i);
    }

    private void loadSampleEcg() {
        Bitmap image = BitmapFactory.decodeResource(this.getResources(),
                R.drawable.sample_ecg);
        updateImageView(image);
    }

    // Note: for target SDK over 22, must add specific code to check for permissions,
    // as Android > 6 has dynamic granting of permissions.  Leave target SDK to avoid this.
    // See: http://stackoverflow.com/questions/32431723/read-external-storage-permission-for-android
    // and https://developer.android.com/training/permissions/requesting.html
    private void selectImageFromGallery() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        }
        else {
            proceedToSelectImageFromGallery();
        }
    }

    private void proceedToSelectImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, RESULT_LOAD_IMAGE);
    }

    private void takePhoto() {
        // check permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED  ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_CAMERA);

        }
        else {
            proceedToTakePhoto();
        }
    }

    private void proceedToTakePhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (deviceHasCamera(takePictureIntent)) {
            File photoFile;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                showFileErrorAlert();
                return;
            }
            if (photoFile != null) {
                Uri photoUri;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    photoUri = FileProvider.getUriForFile(MainActivity.this,
                            BuildConfig.APPLICATION_ID + ".provider",
                            photoFile);
                }
                else {
                    photoUri = Uri.fromFile(photoFile);
                }
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        photoUri);
                startActivityForResult(takePictureIntent, RESULT_CAPTURE_IMAGE);
            }
        }
        // camera icon inactivate with if no camera present, so no warning here

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    EPSLog.log("Camera permission granted");
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    enableCameraMenuItem(true);
                    proceedToTakePhoto();
                } else {
                    EPSLog.log("Camera permission denied");
                    enableCameraMenuItem(false);
                }
                break;
            }
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    EPSLog.log("Write External storage permission granted");
                    proceedToSelectImageFromGallery();
                } else {
                    EPSLog.log("Write external storage permission denied");
                    // We won't disable the select image button, but will just keep asking about
                    // this every time the button is pressed.
                }
                break;
            }
            case MY_PERMISSIONS_REQUEST_STARTUP_IMAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    EPSLog.log("Write External storage permission granted");
                    proceedToHandleImage();
                } else {
                    EPSLog.log("Write external storage permission denied");
                }
                break;
            }
            case MY_PERMISSIONS_REQUEST_STARTUP_SENT_IMAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    EPSLog.log("Write External storage permission granted");
                    proceedToHandleSentImage();
                } else {
                    EPSLog.log("Write external storage permission denied");
                }
                break;
            }
            case MY_PERMISSIONS_REQUEST_STARTUP_PDF: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    EPSLog.log("Write External storage permission granted");
                    proceedToHandlePDF();
                } else {
                    EPSLog.log("Write external storage permission denied");
                }
                break;
            }
            case MY_PERMISSIONS_REQUEST_STORE_BITMAP: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    EPSLog.log("Write External storage permission granted");
                    proceedToStoreBitmap();
                } else {
                    EPSLog.log("Write external storage permission denied");
                }
                break;
            }
        }
    }

    private void enableCameraMenuItem(boolean enable) {
        Menu menuNav = navigationView.getMenu();
        MenuItem cameraMenuItem = menuNav.findItem(R.id.nav_camera);
        cameraMenuItem.setEnabled(enable);
    }

    private boolean deviceHasCamera(Intent takePictureIntent) {
        return takePictureIntent.resolveActivity(getPackageManager()) != null;
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = getTimeStamp();
        String imageFileName = "JPEG_" + timeStamp + "_"; //NON-NLS
        File storageDir = Environment.getExternalStorageDirectory();
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        currentPhotoPath = image.getAbsolutePath();
       // galleryAddPic();
        return image;
    }

    private File createTmpPdfFile() throws IOException {
        String timeStamp = getTimeStamp();
        String pdfFileName = "PDF_" + timeStamp + "_"; //NON-NLS
        File storageDir = this.getCacheDir();
        //currentPhotoPath = image.getAbsolutePath();
        return File.createTempFile(
                pdfFileName,  /* prefix */
                ".pdf",         /* suffix */
                storageDir      /* directory */
        );
    }

    @SuppressLint("SimpleDateFormat")
    private String getTimeStamp() {
        return new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    }

    // possibly implement save photo to gallery
//    private void galleryAddPic() {
//        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//        File f = new File(currentPhotoPath);
//        Uri contentUri = Uri.fromFile(f);
//        mediaScanIntent.setData(contentUri);
//        this.sendBroadcast(mediaScanIntent);
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            if (selectedImage == null) {
                return;
            }
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            if (cursor == null) {
                return;
            }
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            updateImageViewWithPath(picturePath);
        }
        if (requestCode == RESULT_CAPTURE_IMAGE && resultCode == RESULT_OK) {
            updateImageViewWithPath(currentPhotoPath);
        }
    }

    private void updateImageViewWithPath(String path) {
        Bitmap bitmap = getScaledBitmap(path);
        updateImageView(bitmap);
    }

    private void updateImageView(Bitmap bitmap) {
        imageView.setImageBitmap(bitmap);
        scaleImageForImageView();
        imageView.setVisibility(View.VISIBLE);
        clearCalibration();
        // updateImageView not used for PDFs, so
        // reset all the PDF variables
        resetPdf();
    }

    private Bitmap getScaledBitmap(String picturePath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(picturePath, options);
        int targetWidth = imageView.getWidth();
        int targetHeight = imageView.getHeight();
        options.inSampleSize = calculateInSampleSize(options, targetWidth, targetHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(picturePath, options);
    }

    private void rotateImage(float degrees) {
        totalRotation += degrees;
        imageView.setRotationBy(degrees);
    }

    private void resetImage() {
        totalRotation = 0.0f;
        imageView.setRotationTo(0f);
    }

    private void showHelp() {
        startActivity(new Intent(this, HelpTopics.class));
    }

    private void about() {
        Intent i = new Intent(this, About.class);
        i.putExtra(getString(R.string.version_number), version.getVersionName());
        startActivity(i);
    }

    private void meanRR() {
        if (!thereAreCalipers()) {
            noCalipersAlert();
            selectMainMenu();
            return;
        }
        Caliper singleHorizontalCaliper = getLoneTimeCaliper();
        if (singleHorizontalCaliper != null) {
            calipersView.selectCaliperAndUnselectOthers(singleHorizontalCaliper);
        }
        if (calipersView.noCaliperIsSelected()) {
            noCaliperSelectedAlert();
            return;
        }
        Caliper c = calipersView.activeCaliper();
        if (c.getDirection() == Caliper.Direction.VERTICAL || c.isAngleCaliper()) {
            showNoTimeCaliperSelectedAlert();
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.number_of_intervals_dialog_title));
        builder.setMessage(getString(R.string.number_of_intervals_dialog_message));
        final EditText input = new EditText(this);
        // not sure I need ALL of the below!
        input.setLines(1);
        input.setMaxLines(1);
        input.setSingleLine(true);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint(getString(R.string.mean_rr_dialog_hint));
        input.setText(getString(R.string.default_number_rr_intervals));
        input.setSelection(0);

        builder.setView(input);
        builder.setPositiveButton(getString(R.string.ok_title), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialogResult = input.getText().toString();
                processMeanRR();
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

    private void processMeanRR() {
        int divisor;
        try {
            divisor = Integer.parseInt(dialogResult);
        } catch (NumberFormatException ex) {
            return;
        }
        if (divisor > 0) {
            Caliper c = calipersView.activeCaliper();
            if (c == null) {
                return;
            }
            double intervalResult = Math.abs(c.intervalResult());
            double meanRR = intervalResult / divisor;
            double meanRate = c.rateResult(meanRR);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.mean_rr_result_dialog_title));
            DecimalFormat decimalFormat = new DecimalFormat("@@@##");

            builder.setMessage(String.format(getString(R.string.mean_rr_result_dialog_message),
                    decimalFormat.format(meanRR), c.getCalibration().getRawUnits(),
                    decimalFormat.format(meanRate) ));
//            builder.setMessage("Mean interval = " + decimalFormat.format(meanRR) + " " +
//                    c.getCalibration().getRawUnits() + "\nMean rate = " +
//                    decimalFormat.format(meanRate) + " " + getString(R.string.bpm));
            builder.show();
        }
    }

    private void calculateQTc() {
        horizontalCalibration.setDisplayRate(false);
        calipersView.invalidate();
        Caliper singleHorizontalCaliper = getLoneTimeCaliper();
        if (singleHorizontalCaliper != null && !singleHorizontalCaliper.isSelected()) {
            calipersView.selectCaliperAndUnselectOthers(singleHorizontalCaliper);
        }
        if (noTimeCaliperSelected()) {
            showNoTimeCaliperSelectedAlert();
            calipersView.setLocked(true);
        }
        else {
            rrIntervalForQTc = 0.0;
            selectQTcStep1Menu();
            calipersView.setLocked(true);
        }
    }

    private void qtcMeasureRR() {
        if (noTimeCaliperSelected()) {
            showNoTimeCaliperSelectedAlert();
        }
        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.number_of_intervals_dialog_title));
            builder.setMessage(getString(R.string.number_of_intervals_dialog_message));
            final EditText input = new EditText(this);
            // not sure I need ALL of the below!
            input.setLines(1);
            input.setMaxLines(1);
            input.setSingleLine(true);
            input.setInputType(InputType.TYPE_CLASS_NUMBER);
            input.setHint(getString(R.string.mean_rr_dialog_hint));
            input.setText(getString(R.string.default_number_qtc_rr_intervals));
            input.setSelection(0);

            builder.setView(input);
            builder.setPositiveButton(getString(R.string.continue_title), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Caliper c = calipersView.activeCaliper();
                    if (c == null) {
                        dialog.cancel();
                        return;
                    }
                    dialogResult = input.getText().toString();
                    int divisor;
                    try {
                        divisor = Integer.parseInt(dialogResult);
                    } catch (Exception ex) {
                        dialog.cancel();
                        return;
                    }
                    double intervalResult = Math.abs(c.intervalResult());
                    double meanRR = intervalResult / divisor;
                    rrIntervalForQTc = c.intervalInSecs(meanRR);
                    selectQTcStep2Menu();
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
    }

    private void doQTcCalculation() {
        if (noTimeCaliperSelected()) {
            showNoTimeCaliperSelectedAlert();
        }
        else {
            Caliper c = calipersView.activeCaliper();
            if (c == null) {
                return;
            }
            double qt = Math.abs(c.intervalInSecs(c.intervalResult()));
            double meanRR = Math.abs(rrIntervalForQTc);
            String result;
            if (meanRR > 0) {
                QtcCalculator calculator = new QtcCalculator(qtcFormulaPreference, this);
                result = calculator.calculate(qt, meanRR, c.getCalibration().unitsAreMsec(),
                        c.getCalibration().getUnits());
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getString(R.string.calculated_qtc_dialog_title));
                builder.setMessage(result);
                builder.setNegativeButton(getString(R.string.repeat_qt_button_title), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectQTcStep2Menu();
                    }
                });
                builder.setPositiveButton(getString(R.string.done_button_title), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectMainMenu();
                    }
                });
                builder.show();
            }
        }
    }

    private void toggleIntervalRate() {
        horizontalCalibration.setDisplayRate(!horizontalCalibration.getDisplayRate());
        calipersView.invalidate();
    }

    private void microMoveBar(Caliper c, Caliper.Component component, float distance, Caliper.MovementDirection direction) {
        if (component == Caliper.Component.None) {
            return;
        }
        c.moveBar(distance, component, direction);
        calipersView.invalidate();
    }

    private void left() {
        microMoveBar(calipersView.chosenCaliper(), calipersView.getPressedComponent(), -1f, Caliper.MovementDirection.Left);
    }

    private void right() {
        microMoveBar(calipersView.chosenCaliper(), calipersView.getPressedComponent(), 1f, Caliper.MovementDirection.Right);
    }

    private void microLeft() {
        microMoveBar(calipersView.chosenCaliper(), calipersView.getPressedComponent(), -0.1f, Caliper.MovementDirection.Left);
    }

    private void microRight() {
        microMoveBar(calipersView.chosenCaliper(), calipersView.getPressedComponent(), 0.1f, Caliper.MovementDirection.Right);
    }

    private void up() {
        microMoveBar(calipersView.chosenCaliper(), calipersView.getPressedComponent(), -1f, Caliper.MovementDirection.Up);
    }

    private void down() {
        microMoveBar(calipersView.chosenCaliper(), calipersView.getPressedComponent(), 1f, Caliper.MovementDirection.Down);
    }

    private void microUp() {
        microMoveBar(calipersView.chosenCaliper(), calipersView.getPressedComponent(), -0.1f, Caliper.MovementDirection.Up);
    }

    private void microDown() {
        microMoveBar(calipersView.chosenCaliper(), calipersView.getPressedComponent(), 0.1f, Caliper.MovementDirection.Down);
    }

    private void tweakDone() {
        calipersView.setTweakingOrColoring(false);
        gotoPreviousMenu();
    }

    private void microDone() {
        calipersView.unchooseAllCalipersAndComponents();
        calipersView.invalidate();
        selectTweakMenu();
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
        return !thereAreCalipers() ||
                calipersView.noCaliperIsSelected() ||
                calipersView.activeCaliper().getDirection() == Caliper.Direction.VERTICAL ||
                calipersView.activeCaliper().isAngleCaliper();
    }

    private boolean noAngleCaliperSelected() {
        return !thereAreCalipers() ||
                calipersView.noCaliperIsSelected() ||
                !calipersView.activeCaliper().isAngleCaliper();
    }

    private void noCaliperSelectedAlert() {
        showSimpleAlert(R.string.no_caliper_selected_alert_title,
                R.string.no_caliper_selected_alert_message);
    }

    private void setupCalibration() {
        if (!thereAreCalipers()) {
            noCalipersAlert();
        }
        else {
            selectCalibrationMenu();
            calipersView.selectCaliperIfNoneSelected();
            // ok to change selected calipers in this menu
            calipersView.setLocked(false);
        }
    }

    private void setCalibration() {
        if (!thereAreCalipers()) {
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
        if (!c.requiresCalibration()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.angle_caliper_title);
            builder.setMessage(R.string.angle_caliper_calibration_message);
            builder.setNegativeButton(getString(R.string.ok_title), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.show();
            return;
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
        // not sure I need ALL of the below!
        input.setLines(1);
        input.setMaxLines(1);
        input.setSingleLine(true);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint(getString(R.string.calibration_dialog_hint));
        input.setSelection(0);
        String calibrationString = "";
        if (horizontalCalibration.getCalibrationString().length() < 1) {
            horizontalCalibration.setCalibrationString(defaultTimeCalibration);
        }
        if (verticalCalibration.getCalibrationString().length() < 1) {
            verticalCalibration.setCalibrationString(defaultAmplitudeCalibration);
        }
        input.setText(calibrationString);

        Caliper.Direction direction = c.getDirection();
        if (direction == Caliper.Direction.HORIZONTAL) {
            calibrationString = horizontalCalibration.getCalibrationString();
        } else {
            calibrationString = verticalCalibration.getCalibrationString();
        }

        input.setText(calibrationString);

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

    private void processCalibration() {
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
        cal.setOriginalZoom(imageView.getScale());
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
        if (chunks.size() < 1) {
            return calibrationResult;
        }
        NumberFormat format = NumberFormat.getInstance();
        try {
            Number number = format.parse(chunks.get(0));
            calibrationResult.value = number.floatValue();
        } catch (ParseException ex) {
            EPSLog.log("Exception = " + ex.toString());
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
        builder.setPositiveButton(getString(R.string.ok_title), null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showFileErrorAlert() {
        showSimpleAlert(R.string.file_load_error, R.string.file_load_message);
    }

    private void clearCalibration() {
        resetCalibration();
    }

    private void resetCalibration() {
        if (horizontalCalibration.isCalibrated() || verticalCalibration.isCalibrated()) {
            flashCalipers();
            horizontalCalibration.reset();
            verticalCalibration.reset();
        }
    }

    private void flashCalipers() {
        final float originalAlpha = calipersView.getAlpha();
        calipersView.setAlpha(1.0f);
        calipersView.animate()
                .alphaBy(-0.8f)
                .setDuration(shortAnimationDuration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        calipersView.setAlpha(originalAlpha);
                        calipersView.invalidate();
                        super.onAnimationEnd(animation);
                    }
                });
    }

    public boolean thereAreCalipers() {
        return calipersView.getCalipers().size() > 0;
    }


    private Caliper getLoneTimeCaliper() {
        Caliper c = null;
        int n = 0;
        if (thereAreCalipers()) {
            for (Caliper caliper : getCalipers()) {
                if (caliper.getDirection() == Caliper.Direction.HORIZONTAL && !caliper.isAngleCaliper()) {
                    c = caliper;
                    n++;
                }
            }
        }
        return (n == 1) ? c : null;
    }

    // will be used when Brugadometer button/calculator is implemented
    private Caliper getLoneAngleCaliper() {
        Caliper c = null;
        int n = 0;
        if (thereAreCalipers()) {
            for (Caliper caliper : getCalipers()) {
                if (caliper.isAngleCaliper()) {
                    c = caliper;
                    n++;
                }
            }
        }
        return (n == 1) ? c : null;
    }

    public void toggleMarchingCalipers() {
        calipersView.toggleShowMarchingCaliper();
        calipersView.invalidate();
    }

    private void addCaliperWithDirection(Caliper.Direction direction) {
        addCaliperWithDirectionAtRect(direction, new Rect(0, 0, calipersView.getWidth(),
                calipersView.getHeight()));
        calipersView.invalidate();
        returnFromAddCaliperMenu();
    }

    private void setLineWidth(Caliper c, float width) {
        float pixels = dpToPixel(width);
        c.setLineWidth(pixels);
    }

    private void addCaliperWithDirectionAtRect(Caliper.Direction direction,
                                               Rect rect) {
        Caliper c = new Caliper();
        c.setUnselectedColor(currentCaliperColor);
        c.setSelectedColor(currentHighlightColor);
        c.setColor(currentCaliperColor);
        setLineWidth(c, currentLineWidth);
        c.setxOffset(getResources().getDimension(R.dimen.caliper_text_offset));
        c.setyOffset(getResources().getDimension(R.dimen.caliper_text_offset));
        c.setDirection(direction);
        if (direction == Caliper.Direction.HORIZONTAL) {
            c.setCalibration(horizontalCalibration);
            c.setTextPosition(timeCaliperTextPositionPreference);
        } else {
            c.setCalibration(verticalCalibration);
            c.setTextPosition(amplitudeCaliperTextPositionPreference);
        }
        c.setFontSize(useLargeFont ? largeFontSize : smallFontSize);
        c.setRoundMsecRate(roundMsecRate);
        c.setAutoPositionText(autoPositionText);
        c.setInitialPosition(rect);
        getCalipers().add(c);
    }

    private void addTimeCaliper() {
            addCaliperWithDirection(Caliper.Direction.HORIZONTAL);
    }

    private void addAmplitudeCaliper() {
            addCaliperWithDirection(Caliper.Direction.VERTICAL);
    }
    
    private void addAngleCaliper() {
        AngleCaliper c = new AngleCaliper();
        Rect rect = new Rect(0, 0, calipersView.getWidth(),
                calipersView.getHeight());
        c.setUnselectedColor(currentCaliperColor);
        c.setSelectedColor(currentHighlightColor);
        c.setColor(currentCaliperColor);
        setLineWidth(c, currentLineWidth);
        c.setFontSize(useLargeFont ? largeFontSize : smallFontSize);
        c.setxOffset(getResources().getDimension(R.dimen.caliper_text_offset));
        c.setyOffset(getResources().getDimension(R.dimen.caliper_text_offset));
        c.setRoundMsecRate(roundMsecRate);
        c.setAutoPositionText(autoPositionText);
        c.setDirection(Caliper.Direction.HORIZONTAL);
        c.setCalibration(horizontalCalibration);
        c.setVerticalCalibration(verticalCalibration);
        c.setTextPosition(timeCaliperTextPositionPreference);
        c.setFontSize(useLargeFont ? largeFontSize : smallFontSize);
        c.setInitialPosition(rect);
        getCalipers().add(c);
        calipersView.invalidate();
        returnFromAddCaliperMenu();
    }

    private void matrixChangedAction() {
        //Log.d(EPS, "Matrix changed, scale = " + attacher.getScale());
        adjustCalibrationForScale(imageView.getScale());
        calipersView.invalidate();
    }

    private void adjustCalibrationForScale(float scale) {
        horizontalCalibration.setCurrentZoom(scale);
        verticalCalibration.setCurrentZoom(scale);
    }

    private class CalibrationResult {
        boolean success;
        float value;
        String units;

        CalibrationResult() {
            success = false;
            value = 0.0f;
            units = "";
        }
    }

    private class MatrixChangeListener implements OnMatrixChangedListener {
        @Override
        public void onMatrixChanged(RectF rect) {
            matrixChangedAction();

        }
    }

}


