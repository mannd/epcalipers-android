package org.epstudios.epcalipers;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.chrisbanes.photoview.OnMatrixChangedListener;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.epstudios.epcalipers.QtcCalculator.QtcFormula;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import static org.epstudios.epcalipers.CalibrationProcessor.processCalibrationString;
import static org.epstudios.epcalipers.MyPreferenceFragment.ALL;
import static org.epstudios.epcalipers.MyPreferenceFragment.BAZETT;
import static org.epstudios.epcalipers.MyPreferenceFragment.FRAMINGHAM;
import static org.epstudios.epcalipers.MyPreferenceFragment.FRIDERICIA;
import static org.epstudios.epcalipers.MyPreferenceFragment.HODGES;

// Note EP Calipers is legacy software.  Ideally all variables that aren't UI related
// would be moved to a view model class, but instead we rely still rely on onSaveInstanceState()
// to save and reload these variables directly to this Activity.  Thus the imageView
// bitmap is held in the MainViewModel class to deal with device rotation, while
// the imageView URI is held in the app bundle to deal with the app going into the
// background.
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    // TODO: Remove unused constants
    private static final int RESULT_LOAD_IMAGE = 1;
    private static final int RESULT_CAPTURE_IMAGE = 2;

    private static final int DEFAULT_LINE_WIDTH = 2;

    // Image scaling limits
    private static final float MAX_SCALE = 10.0f;
    private static final float MIN_SCALE = 0.3f;

    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 100;

    // TODO: Make sure the 1st part of this conditional never changes.  force_first_run
    // MUST be false for a release.  The 2nd part of the condition can be set true to
    // test onboarding with each startup.
    private final boolean force_first_run = !BuildConfig.DEBUG ? false : false;

    // Store version information
    private Version version;

    // OnSharedPreferenceListener must be a strong reference
    // because if it is a weak reference it will be garbage collected and stop working.
    // See http://stackoverflow.com/questions/2542938/sharedpreferences-onsharedpreferencechangelistener-not-being-called-consistently
    @SuppressWarnings("FieldCanBeLocal")
    private SharedPreferences.OnSharedPreferenceChangeListener onSharedPreferenceChangeListener;

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
    private List<Button> upDownButtons;
    private List<Button> rightLeftButtons;

    // Toolbar menus
    private final Deque<ToolbarMenu> toolbarMenuDeque = new ArrayDeque<>();
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
    private NavigationView navigationView;
    @SuppressWarnings("FieldCanBeLocal")
    private Toolbar actionBar;

    // Contains big objects that must persist with rotation, e.g. the imageView Bitmap.
    private MainViewModel mainViewModel;

    // Side menu items
    private MenuItem lockImageMenuItem;

    // Other variables
    private FrameLayout layout;
    private DrawerLayout drawerLayout;
    private double rrIntervalForQTc;
    private boolean showStartImage;
    private boolean roundMsecRate;
    private boolean autoPositionText;
    private boolean showValidationDialog;
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
    private File photoFile; // Used as tmp file to hold results of taking a photo.
    private Uri currentImageUri; // The URI of the image held in imageView.

    private boolean useLargeFont;
    private boolean imageIsLocked;
    private float smallFontSize;
    private float largeFontSize;

    private Map<String, QtcFormula> qtcFormulaMap;
    private QtcFormula qtcFormulaPreference = QtcFormula.qtcBzt;
    private HashMap<String, Caliper.TextPosition> textPositionMap;
    // These are equal to the defaults in MyPreferenceFragment mapped to strings "CenterAbove"
    // and "Right".
    private Caliper.TextPosition timeCaliperTextPositionPreference = Caliper.TextPosition.CenterAbove;
    private Caliper.TextPosition amplitudeCaliperTextPositionPreference = Caliper.TextPosition.Right;

    // New way to get take photo and get media images.
    final ActivityResultLauncher<Uri> getPhotoContent = registerForActivityResult(
            new ActivityResultContracts.TakePicture(),
            new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean result) {
                    if (result) {
                        Uri photoUri = Uri.fromFile(photoFile);
                        if (photoUri == null) {
                            return;
                        }
                        updateImageView(photoUri);
                        // rotates image
//                        updateImageViewWithPath(photoUri.getPath());
                    }
                }
            });
    final ActivityResultLauncher<String> getImageContent = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            imageUri -> {
                if (imageUri == null) {
                    return;
                }
                Bitmap bitmap;
                currentImageUri = imageUri;
                try {
                    if (Build.VERSION.SDK_INT < 28) {
                        bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                    } else {
                        ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), imageUri);
                        bitmap = ImageDecoder.decodeBitmap(source);
                    }
                    updateImageView(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
    final ActivityResultLauncher<Intent> imageResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Intent intent = result.getData();
                try {
                    if (intent != null) {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                                getContentResolver(), intent.getData()
                        );
                        updateImageView(bitmap);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
    );

    // Set up the long press menus.
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
            final int itemId = item.getItemId();
            if (itemId == R.id.menu_rotate) {
                selectRotateImageMenu();
                mode.finish();
                return true;
            } else if (itemId == R.id.menu_pdf) {
                selectPDFMenu();
                mode.finish();
                return true;
            } else {
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
            if (calipersView.getTouchedCaliper() != null) {
                marchingMenuItem.setVisible(calipersView.getTouchedCaliper().isTimeCaliper());
            } else {
                marchingMenuItem.setVisible(false);
            }
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            final int itemId = item.getItemId();
            if (itemId == R.id.menu_color) {
                selectColorMenu();
                calipersView.setTweakingOrColoring(true);
                mode.finish();
                return true;
            } else if (itemId == R.id.menu_tweak) {
                selectTweakMenu();
                calipersView.setTweakingOrColoring(true);
                mode.finish();
                return true;
            } else if (itemId == R.id.menu_march) {
                toggleMarchingCalipers();
                mode.finish();
                return true;
            } else {
                return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            currentActionMode = null;
        }
    };

    public ActionMode getCurrentActionMode() {
        return currentActionMode;
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EPSLog.log("onCreate");

        // App started from scratch
        noSavedInstance = (savedInstanceState == null);

        setContentView(R.layout.activity_main);
        findViewById(R.id.loadingPanel).setVisibility(View.GONE);

        // Set up calipers view
        calipersView = findViewById(R.id.caliperView);
        calipersView.setMainActivity(this);

        // Set up side menu
        navigationView = findViewById(R.id.nav_view);
        drawerLayout = findViewById(R.id.activity_main_id);
        Menu menuNav = navigationView.getMenu();
        MenuItem cameraMenuItem = menuNav.findItem(R.id.nav_camera);
        // Disable camera button if no camera present
        cameraMenuItem.setEnabled(getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY));
        lockImageMenuItem = menuNav.findItem(R.id.nav_lock_image);
        // Set up actions for navigation (hamburger) menu.
        navigationView.setNavigationItemSelectedListener(
                menuItem -> {
                    int id = menuItem.getItemId();
                    if (id == R.id.nav_camera) {
                        takePhoto();
                        selectMainMenu();
                    } else if (id == R.id.nav_image) {
                        selectImageFromGallery();
                        selectMainMenu();
                    } else if (id == R.id.nav_lock_image) {
                        lockImage();
                    } else if (id == R.id.nav_sample_ecg) {
                        loadSampleEcg();
                        selectMainMenu();
                    } else if (id == R.id.nav_about) {
                        about();
                    } else if (id == R.id.nav_help) {
                        showHelp();
                    } else if (id == R.id.nav_preferences) {
                        changeSettings();
                    }
                    drawerLayout.closeDrawers();
                    return true;
                }
        );

        // Initialize variables
        smallFontSize = getResources().getDimension(R.dimen.small_font_size);
        largeFontSize = getResources().getDimension(R.dimen.large_font_size);
        currentCaliperColor = ContextCompat.getColor(this, R.color.default_caliper_color);
        currentHighlightColor = ContextCompat.getColor(this, R.color.default_highlight_color);
        currentLineWidth = DEFAULT_LINE_WIDTH;
        shortAnimationDuration = getResources().getInteger(
                android.R.integer.config_shortAnimTime);

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

        loadPreferences();


        imageView = findViewById(R.id.imageView);
        imageView.setEnabled(true);
        if (!showStartImage && noSavedInstance) {
            imageView.setVisibility(View.INVISIBLE);
        }
        imageView.setScaleType(ImageView.ScaleType.CENTER);
        imageView.setMaximumScale(MAX_SCALE);
        imageView.setMinimumScale(MIN_SCALE);
        imageView.setOnMatrixChangeListener(new MatrixChangeListener());
        imageView.setOnLongClickListener(v -> {
            if (currentActionMode != null) { // || calipersView.isTweakingOrColoring()) {
                return false;
            }
            startActionMode(imageCallBack);
            return true;
        });

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
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

//        // Initialize view model to hold bitmap during app lifecycle
        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);
//        Drawable viewModelDrawable = mainViewModel.getDrawable();
//        EPSLog.log("mainViewModel.getDrawable() = " + viewModelDrawable);
//       if (savedInstanceState != null) {
//           if (viewModelDrawable != null) {
//               imageView.setImageDrawable(viewModelDrawable);
//           }
//       }
//       }//        String image_key = getString(R.string.image_uri_key);
//        EPSLog.log("****" + image_key);
////        Uri savedInstanceStateImageUri = (Uri)savedInstanceState.getParcelable(getString(R.string.image_uri_key));
////        if (savedInstanceState != null) {
//            if (viewModelDrawable != null) {
//                imageView.setImageDrawable(viewModelDrawable);
//            }
////            else if (savedInstanceStateImageUri != null ){
////                imageView.setImageURI(savedInstanceStateImageUri);
////            }
////        } else {
//            // Start up from scratch
//            EPSLog.log("savedInstanceState is null");
////        }

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

        selectMainMenu();

        // entry point to load external pics/PDFs
        if (externalImageLoad) {
            updateImageView(externalImageBitmap);
            externalImageLoad = false;
        }

        onSharedPreferenceChangeListener = (sharedPreferences, key) -> {
            // show start image only has effect with restart
            EPSLog.log("onSharedPreferenceChangeListener");
            Objects.requireNonNull(sharedPreferences, "Shared preferences must not be null!");
            if (key.equals(getString(R.string.show_start_image_key))) {
                return;
            }
            if (key.equals(getString(R.string.show_validation_dialog_key))) {
                showValidationDialog = sharedPreferences.getBoolean(key, true);
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
                        ContextCompat.getColor(getApplicationContext(), R.color.default_caliper_color));
                return;
            }
            if (key.equals(getString(R.string.new_highlight_color_key))) {
                currentHighlightColor = sharedPreferences.getInt(key,
                        ContextCompat.getColor(getApplicationContext(), R.color.default_highlight_color));
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
                    String lineWidthString = sharedPreferences.getString(key,
                            getString(R.string.default_line_width));
                    int lineWidth = DEFAULT_LINE_WIDTH;
                    if (lineWidthString != null) {
                        lineWidth = Integer.parseInt(lineWidthString);
                    }
                    currentLineWidth = lineWidth;
                    for (Caliper c : calipersView.getCalipers()) {
                        setLineWidth(c, lineWidth);
                    }
                } catch (NumberFormatException ex) {
                    currentLineWidth = DEFAULT_LINE_WIDTH;
                    for (Caliper c : calipersView.getCalipers()) {
                        setLineWidth(c, currentLineWidth);
                    }
                }
                return;
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
        };

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);

        PackageInfo packageInfo;
        int versionCode = 0;
        String versionName = "";
        try {
            packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            // versionCode deprecated in Android Pie.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                versionCode = (int)packageInfo.getLongVersionCode();
            } else {
                versionCode = packageInfo.versionCode;
            }
            versionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        version = new Version(this, prefs, versionName, versionCode);

        layout = findViewById(R.id.frame_layout);
        ViewTreeObserver viewTreeObserver = layout.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @SuppressLint("NewApi")
            @SuppressWarnings("deprecation")
            @Override
            public void onGlobalLayout() {
                EPSLog.log("onGlobalLayout");
                int androidVersion = Build.VERSION.SDK_INT;
                if (androidVersion >= Build.VERSION_CODES.JELLY_BEAN) {
                    layout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    layout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }

                if (noSavedInstance) {
                    addCaliperWithDirection(Caliper.Direction.HORIZONTAL);
                }
                // else rotate view after brief delay
                else {
                    Handler handler = new Handler();
                    handler.postDelayed(this::rotateImageView, 50);

                }
            }

            private void rotateImageView() {
                imageView.setRotationBy(totalRotation);
            }
        });

        if (force_first_run || version.isUpgrade() || version.isNewInstallation()) {
            startActivity(new Intent(this, Onboarder.class));
            version.saveVersion();
        }

        if (externalImageLoad) {
            startActivity(intent);
        }
    }

    private void handleImage() {
//        if (ContextCompat.checkSelfPermission(this,
//                Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
//                    MY_PERMISSIONS_REQUEST_STARTUP_IMAGE);
//        }
//        else {
            proceedToHandleImage();
//        }
    }

    private void handleSentImage() {
//        if (ContextCompat.checkSelfPermission(this,
//                Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
//                    MY_PERMISSIONS_REQUEST_STARTUP_SENT_IMAGE);
//        }
//        else {
            proceedToHandleSentImage();
//        }
    }

    private void proceedToHandleImage() {
        try {
            currentImageUri = getIntent().getData();
            if (currentImageUri != null) {
                externalImageLoad = true;
                externalImageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), currentImageUri);
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
        // FIXME: these permissions no longer needed???
//        if (ContextCompat.checkSelfPermission(this,
//                Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
//                    MY_PERMISSIONS_REQUEST_STARTUP_PDF);
//        }
//        else {
            proceedToHandlePDF();
//        }
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
        new NougatAsyncLoadPDF(this).execute(uriPage);
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
        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.input_dialog, null);
        // No message TextView for this dialog; just title and hint.
        final TextInputLayout numberOfIntervalsTextInputLayout = alertLayout.findViewById(R.id.inputDialogTextInputLayout);
        final TextInputEditText numberOfIntervalsEditText = alertLayout.findViewById(R.id.inputDialogEditText);
        numberOfIntervalsEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
        numberOfIntervalsTextInputLayout.setHint(getString(R.string.page_number_hint));

        String currentPageNumberString = Integer.toString(currentPdfPageNumber + 1);
        numberOfIntervalsEditText.setText(currentPageNumberString);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.go_to_page_title);
        builder.setCancelable(false);
        builder.setView(alertLayout);
        builder.setPositiveButton(getString(R.string.ok_title), (dialog, which) -> {
            int pageNumber;
            try {
                dialogResult = Objects.requireNonNull(numberOfIntervalsEditText.getText()).toString();
                pageNumber = Integer.parseInt(dialogResult);
            } catch (NullPointerException | NumberFormatException ex) {
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
        });
        builder.setNegativeButton(getString(R.string.cancel_title), (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void loadPreferences() {
        EPSLog.log("loadPreferences()");
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
        // Must use ContextCompat.getColor for default, otherwise colors are wrong.
        currentCaliperColor = sharedPreferences.getInt(getString(R.string.new_caliper_color_key),
                ContextCompat.getColor(this, R.color.default_caliper_color));
        currentHighlightColor = sharedPreferences.getInt(getString(R.string.new_highlight_color_key),
                ContextCompat.getColor(this, R.color.default_highlight_color));
        showValidationDialog = sharedPreferences.getBoolean(getString(R.string.show_validation_dialog_key),
                true);
        try {
            String lineWidthString = sharedPreferences.getString(getString(R.string.line_width_key),
                    Integer.valueOf(DEFAULT_LINE_WIDTH).toString());
            int lineWidth = DEFAULT_LINE_WIDTH;
            if (lineWidthString != null) {
                lineWidth = Integer.parseInt(lineWidthString);
            }
            currentLineWidth = lineWidth;
        } catch (NumberFormatException ex) {
            currentLineWidth = DEFAULT_LINE_WIDTH;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        EPSLog.log("onResume");
    }

    private Bitmap getImageViewBitmap() {
        Drawable drawable = imageView.getDrawable();
        BitmapDrawable bitmapDrawable = (BitmapDrawable)drawable;
        return bitmapDrawable.getBitmap();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        EPSLog.log("onSaveInstanceState");
        EPSLog.log("imageView.scale = " + imageView.getScale());
        EPSLog.log("imageView.displayRect = " + imageView.getDisplayRect());
        outState.putFloat(getString(R.string.imageview_scale_key), imageView.getScale());
        outState.putFloat(getString(R.string.total_rotation_key), totalRotation);
        outState.putBoolean(getString(R.string.image_locked_key), imageIsLocked);
        outState.putBoolean(getString(R.string.multipage_pdf_key), numberOfPdfPages > 0);
        outState.putBoolean(getString(R.string.a_caliper_is_marching_key), calipersView.isACaliperIsMarching());

        Drawable drawable = imageView.getDrawable();
        mainViewModel.setDrawable(drawable);
        outState.putParcelable(getString(R.string.image_uri_key), currentImageUri);
        outState.putParcelable(getString(R.string.current_pdf_uri), currentPdfUri);
        outState.putInt(getString(R.string.number_pdf_pages), numberOfPdfPages);
        outState.putInt(getString(R.string.current_pdf_page), currentPdfPageNumber);

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
            outState.putFloat(i + getString(R.string.caliper_bar1_position_key),
                    c.getAbsoluteBar1Position());
            outState.putFloat(i + getString(R.string.caliper_bar2_position_key),
                    c.getAbsoluteBar2Position());
            outState.putFloat(i + getString(R.string.caliper_crossbar_position_key),
                    c.getAbsoluteCrossBarPosition());
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
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        EPSLog.log("onRestoreInstanceState");
        super.onRestoreInstanceState(savedInstanceState);
        // See: https://proandroiddev.com/customizing-the-new-viewmodel-cf28b8a7c5fc
        Uri imageUri = savedInstanceState.getParcelable(getString(R.string.image_uri_key));
        // Todo: need to make sure drawables and Uris all saved when they need to be.
        // Also, if external image loaded, don't want to overwrite it with old image.
        Drawable drawable = mainViewModel.getDrawable();
        if (drawable != null) {
            imageView.setImageDrawable(drawable);
        } else if (imageUri != null) {
            imageView.setImageURI(imageUri);
        } else {
            return; // else return?? don't restore anything if no image?
        }
        currentPdfUri = savedInstanceState.getParcelable(getString(R.string.current_pdf_uri));
        numberOfPdfPages = savedInstanceState.getInt(getString(R.string.number_pdf_pages));
        currentPdfPageNumber = savedInstanceState.getInt(getString(R.string.current_pdf_page));

        imageIsLocked = savedInstanceState.getBoolean(getString(R.string.image_locked_key));
        lockImage(imageIsLocked);
        calipersView.setACaliperIsMarching(savedInstanceState.getBoolean(getString(R.string.a_caliper_is_marching_key)));

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

        horizontalCalibration.setOffset(new PointF(imageView.getDisplayRect().left, imageView.getDisplayRect().top));

        verticalCalibration.setUnits(savedInstanceState.getString(getString(R.string.vcal_units_key)));
        verticalCalibration.setCalibrationString(savedInstanceState.getString(getString(R.string.vcal_string_key)));
        verticalCalibration.setDisplayRate(savedInstanceState.getBoolean(getString(R.string.vcal_display_rate_key)));
        verticalCalibration.setOriginalZoom(savedInstanceState.getFloat(getString(R.string.vcal_original_zoom_key)));
        verticalCalibration.setCurrentZoom(savedInstanceState.getFloat(getString(R.string.vcal_current_zoom_key)));
        verticalCalibration.setCalibrated(savedInstanceState.getBoolean(getString(R.string.vcal_is_calibrated_key)));
        verticalCalibration.setOriginalCalFactor(savedInstanceState.getFloat(getString(R.string.vcal_original_cal_factor_key)));

        verticalCalibration.setOffset(new PointF(imageView.getDisplayRect().left, imageView.getDisplayRect().top));

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
            if (c.getDirection() == Caliper.Direction.HORIZONTAL) {
                c.setCalibration(horizontalCalibration);
                c.setTextPosition(timeCaliperTextPositionPreference);
            }
            else {
                c.setCalibration(verticalCalibration);
                c.setTextPosition(amplitudeCaliperTextPositionPreference);
            }
            if (c instanceof AngleCaliper) {
                ((AngleCaliper) c).setVerticalCalibration(verticalCalibration);
                ((AngleCaliper) c).setBar1Angle(bar1Angle);
                ((AngleCaliper) c).setBar2Angle(bar2Angle);
            }
            c.setAbsoluteBar1Position(bar1Position);
            c.setAbsoluteBar2Position(bar2Position);
            c.setAbsoluteCrossBarPosition(crossbarPosition);
            c.setSelected(selected);
            c.setUnselectedColor(unselectedColor);
            c.setSelectedColor(currentHighlightColor);
            c.setColor(c.isSelected() ? currentHighlightColor : unselectedColor);
            setLineWidth(c, currentLineWidth);
            c.setFontSize(useLargeFont ? largeFontSize : smallFontSize);
            c.setRoundMsecRate(roundMsecRate);
            c.setAutoPositionText(autoPositionText);
            c.setMarching(isMarching);
            calipersView.getCalipers().add(c);
        }
        // To avoid weird situations, rather than restore active menu,
        // go back to Main Menu with rotation.  This avoids problems
        // with rotation while in QTc, tweaking, etc.
        selectMainMenu();
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
            colorDone();
        } else if (v == tweakDoneButton) {
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
    private void selectMenu(ToolbarMenu menu) {
        switch (menu) {
            case Main:
            case Move:
            default:
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
        // Ensure we don't leave a caliper with a chosen component highlighted,
        // in case we get back to the main menu via a shortcut while tweaking,
        // e.g. trying to select a new image.
        calipersView.unchooseAllCalipersAndComponents();
        selectMenu(mainMenu);
        clearToolbarMenuStack();
        pushToolbarMenuStack(ToolbarMenu.Main);
        boolean enable = horizontalCalibration.canDisplayRate();
        intervalRateButton.setEnabled(enable);
        meanRateButton.setEnabled(enable);
        qtcButton.setEnabled(enable);
//        calipersView.setLocked(false);
        calipersView.setAllowTweakPosition(false);
        calipersView.setAllowColorChange(false);
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
        }
        gotoPreviousMenu();
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
    }

    private void selectColorMenu() {
        if (!thereAreCalipers()) {
            noCalipersAlert();
            return;
        }
        if (colorMenu == null) {
            createColorMenu();
        }
        pushToolbarMenuStack(ToolbarMenu.Color);
        selectMenu(colorMenu);
        calipersView.setAllowColorChange(true);
    }

    private void selectTweakMenu() {
        if (!thereAreCalipers()) {
            noCalipersAlert();
            return;
        }
        if (tweakMenu == null) {
            createTweakMenu();
        }
        pushToolbarMenuStack(ToolbarMenu.Tweak);
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
        }
        else if (c.getDirection() == Caliper.Direction.HORIZONTAL) {
            setButtonsVisibility(upDownButtons, View.GONE);
            setButtonsVisibility(rightLeftButtons, View.VISIBLE);
        }
        else {      // vertical caliper
            setButtonsVisibility(upDownButtons, View.VISIBLE);
            setButtonsVisibility(rightLeftButtons, View.GONE);
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

    private void selectImageFromGallery() {
        getImageContent.launch("image/*");
    }

    // FIXME: Use this to get bitmap from Uri??  From: https://pednekarshashank33.medium.com/android-10s-scoped-storage-image-picker-gallery-camera-d3dcca427bbf
    // and is in Kotlin
//    @Throws(IOException::class)
//    private fun getBitmapFromUri(uri: Uri): Bitmap {
//        val parcelFileDescriptor: ParcelFileDescriptor = contentResolver.openFileDescriptor(uri, "r")
//        val fileDescriptor: FileDescriptor = parcelFileDescriptor.fileDescriptor
//        val image: Bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor)
//        parcelFileDescriptor.close()
//        return image
//    }

    // FIXME: We do need permission to take photos.
    private void takePhoto() {
        // check permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    MY_PERMISSIONS_REQUEST_CAMERA);
            // Buttons are unresponsive if permission not granted.
//            showPermissionsRequestToast();
            EPSLog.log("Permission needed to take photos.");
        } else {
            // FIXME: Do on background thread?
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                showFileErrorAlert();
                return;
            }
            Uri photoUri;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                photoUri = FileProvider.getUriForFile(MainActivity.this,
                        BuildConfig.APPLICATION_ID + ".provider",
                        photoFile);
            } else {
                photoUri = Uri.fromFile(photoFile);
            }
            getPhotoContent.launch(photoUri);
            // TODO: Delete the photoUri.  Or do we need to keep it so the Uri can be restored if
            // the activity is sent to background or killed by operating system?.
        }
    }

    // No longer used.
    private void enableCameraMenuItem(boolean enable) {
        Menu menuNav = navigationView.getMenu();
        MenuItem cameraMenuItem = menuNav.findItem(R.id.nav_camera);
        cameraMenuItem.setEnabled(enable);
    }

    // No longer used.
    private void enableImageMenuItem(boolean enable) {
        Menu menuNav = navigationView.getMenu();
        MenuItem imageMenuItem = menuNav.findItem(R.id.nav_image);
        imageMenuItem.setEnabled(enable);
    }

    private boolean deviceHasCamera(Intent takePictureIntent) {
        return takePictureIntent.resolveActivity(getPackageManager()) != null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_CAMERA) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                EPSLog.log("Camera permission granted");
                // permission was granted, yay! Do the
                // contacts-related task you need to do.
                takePhoto();
            } else {
                EPSLog.log("Camera permission denied");
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = getTimeStamp();
        String imageFileName = "JPEG_" + timeStamp + "_"; //NON-NLS

//        File storageDir = getExternalFilesDir(null);
        File storageDir = getExternalFilesDir(null);
//        File storageDir = Environment.getExternalStorageDirectory();
        EPSLog.log("storage directory = " + storageDir);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        String currentPhotoPath = image.getAbsolutePath();
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

    // TODO: see https://developer.android.com/training/camera/photobasics
    private void updateImageViewWithPath(String path) {
        Bitmap bitmap = getScaledBitmap(path);
        updateImageView(bitmap);
    }

    private void updateImageView(Bitmap bitmap) {
        imageView.setImageBitmap(bitmap);
        imageView.setVisibility(View.VISIBLE);
        clearCalibration();
        // updateImageView not used for PDFs, so
        // reset all the PDF variables
        resetPdf();
    }

    // FIXME: not doing any of the processing done in getScaledBitmap(path)
    private void updateImageView(Uri uri) {
        imageView.setImageURI(uri);
        imageView.setVisibility(View.VISIBLE);
        clearCalibration();
        // updateImageView not used for PDFs, so
        // reset all the PDF variables
        resetPdf();
    }

    // FIXME: See https://developer.android.com/training/camera/photobasics for explanation of why this is good to avoid running out of memory.
    // Original code fails in Android 29.  See https://medium.com/@sriramaripirala/android-10-open-failed-eacces-permission-denied-da8b630a89df
    private Bitmap getScaledBitmap(String picturePath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        // FIXME: file is decoded twice in this method.  Probable bug.
        BitmapFactory.decodeFile(picturePath, options);
        int targetWidth = imageView.getWidth();
        int targetHeight = imageView.getHeight();
        options.inSampleSize = calculateInSampleSize(options, targetWidth, targetHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(picturePath, options);
    }

    private static int calculateInSampleSize(
            BitmapFactory.Options options,
            int reqWidth,
            int reqHeight) {
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
        i.putExtra(getString(R.string.version_code), version.getVersionCode());
        startActivity(i);
    }

    private void meanRR() {
        if (!thereAreCalipers()) {
            noCalipersAlert();
            return;
        }
        Caliper singleHorizontalCaliper = getLoneTimeCaliper();
        if (singleHorizontalCaliper != null) {
            calipersView.selectCaliperAndUnselectOthers(singleHorizontalCaliper);
        }
        if (calipersView.noCaliperIsSelected()) {
            showNoCaliperSelectedAlert();
            return;
        }
        Caliper c = calipersView.activeCaliper();
        if (c.getDirection() == Caliper.Direction.VERTICAL || c.isAngleCaliper()) {
            showNoTimeCaliperSelectedAlert();
            return;
        }

        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.input_dialog, null);
        final TextInputLayout numberOfIntervalsTextInputLayout = alertLayout.findViewById(R.id.inputDialogTextInputLayout);
        final TextInputEditText numberOfIntervalsEditText = alertLayout.findViewById(R.id.inputDialogEditText);
        numberOfIntervalsEditText.setText(getString(R.string.default_number_rr_intervals));
        numberOfIntervalsEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
        numberOfIntervalsTextInputLayout.setHint(getString(R.string.mean_rr_dialog_hint));

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.number_of_intervals_dialog_title));
        builder.setMessage(R.string.number_of_intervals_dialog_message);
        builder.setCancelable(false);
        builder.setView(alertLayout);
        builder.setPositiveButton(getString(R.string.ok_title), (dialog, which) -> {
            try {
                dialogResult = Objects.requireNonNull(numberOfIntervalsEditText.getText()).toString();
                processMeanRR();
            } catch (NullPointerException ex) {
                dialog.cancel();
            }
        });
        builder.setNegativeButton(getString(R.string.cancel_title), (dialog, which) -> dialog.cancel());
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

            DecimalFormat decimalFormat = new DecimalFormat("@@@##");
            String title = getString(R.string.mean_rr_result_dialog_title);
            String message = String.format(getString(R.string.mean_rr_result_dialog_message),
                    decimalFormat.format(meanRR), c.getCalibration().getRawUnits(),
                    decimalFormat.format(meanRate));
            Alerts.simpleAlert(this, title, message);
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
        }
        else {
            rrIntervalForQTc = 0.0;
            selectQTcStep1Menu();
        }
    }

    private void qtcMeasureRR() {
        if (noTimeCaliperSelected()) {
            showNoTimeCaliperSelectedAlert();
        }
        else {
            LayoutInflater inflater = getLayoutInflater();
            final View alertLayout = inflater.inflate(R.layout.input_dialog, null);
            final TextInputLayout numberOfIntervalsTextInputLayout = alertLayout.findViewById(R.id.inputDialogTextInputLayout);
            final TextInputEditText numberOfIntervalsEditText = alertLayout.findViewById(R.id.inputDialogEditText);
            numberOfIntervalsEditText.setText(getString(R.string.default_number_qtc_rr_intervals));
            numberOfIntervalsEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
            numberOfIntervalsTextInputLayout.setHint(getString(R.string.mean_rr_dialog_hint));

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.number_of_intervals_dialog_title));
            builder.setMessage(getString(R.string.number_of_intervals_dialog_message));
            builder.setView(alertLayout);
            builder.setCancelable(false);
            builder.setPositiveButton(getString(R.string.continue_title), (dialog, which) -> {
                Caliper c = calipersView.activeCaliper();
                if (c == null) {
                    dialog.cancel();
                    return;
                }
                try {
                    dialogResult = Objects.requireNonNull(numberOfIntervalsEditText.getText()).toString();
                } catch (NullPointerException ex) {
                    dialog.cancel();
                }
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
            });
            builder.setNegativeButton(getString(R.string.cancel_title), (dialog, which) -> dialog.cancel());

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
                builder.setNegativeButton(getString(R.string.repeat_qt_button_title), (dialog, which) -> selectQTcStep2Menu());
                builder.setPositiveButton(getString(R.string.done_button_title), (dialog, which) -> selectMainMenu());
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

    private void colorDone() {
        calipersView.setTweakingOrColoring(false);
        popToolbarMenuStack();
        gotoPreviousMenu();
    }

    private void tweakDone() {
        calipersView.setTweakingOrColoring(false);
        // go back until we are at the last menu before the tweak menu
        ToolbarMenu menu;
        do {
            menu = popToolbarMenuStack();
        } while (menu == ToolbarMenu.Tweak);
        selectMenu(menu);
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
        Alerts.simpleAlert(this, R.string.no_time_caliper_selected_title, R.string.no_time_caliper_selected_message);
    }

    private boolean noTimeCaliperSelected() {
        return !thereAreCalipers() ||
                calipersView.noCaliperIsSelected() ||
                calipersView.activeCaliper().getDirection() == Caliper.Direction.VERTICAL ||
                calipersView.activeCaliper().isAngleCaliper();
    }

    private void showNoCaliperSelectedAlert() {
        Alerts.simpleAlert(this, R.string.no_caliper_selected_alert_title, R.string.no_caliper_selected_alert_message);
    }

    private void setupCalibration() {
        if (!thereAreCalipers()) {
            noCalipersAlert();
        }
        else {
            selectCalibrationMenu();
            calipersView.selectCaliperIfNoneSelected();
        }
    }

    private void setCalibration() {
        if (!thereAreCalipers()) {
            noCalipersAlert();
            selectMainMenu();
            return;
        }
        if (calipersView.noCaliperIsSelected()) {
            showNoCaliperSelectedAlert();
            return;
        }
        final Caliper c = calipersView.activeCaliper();
        if (c == null) {
            return; // shouldn't happen, but if it does...
        }
        if (!c.requiresCalibration()) {
            Alerts.simpleAlert(this, R.string.angle_caliper_title, R.string.angle_caliper_calibration_message);
            return;
        }

        // Create set calibration dialog.
        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.input_dialog, null);
        final TextInputLayout calibrationIntervalTextInputLayout = alertLayout.findViewById(R.id.inputDialogTextInputLayout);
        final TextInputEditText calibrationIntervalEditText = alertLayout.findViewById(R.id.inputDialogEditText);

        // Have hint depend on caliper type.
        String example;
        if (c.isAmplitudeCaliper()) {
            example = getString(R.string.example_amplitude_measurement);
        }
        else { // time caliper
            example = getString(R.string.example_time_measurement);
        }
        final String calibrationHint = String.format(getString(R.string.calibration_dialog_hint), example);
        calibrationIntervalTextInputLayout.setHint(calibrationHint);

        // Use default calibration if no prior calibration.
        String calibrationString;
        if (horizontalCalibration.getCalibrationString().length() < 1) {
            horizontalCalibration.setCalibrationString(defaultTimeCalibration);
        }
        if (verticalCalibration.getCalibrationString().length() < 1) {
            verticalCalibration.setCalibrationString(defaultAmplitudeCalibration);
        }
        if (c.getDirection() == Caliper.Direction.HORIZONTAL) {
            calibrationString = horizontalCalibration.getCalibrationString();
        } else {
            calibrationString = verticalCalibration.getCalibrationString();
        }
        calibrationIntervalEditText.setText(calibrationString);
        calibrationIntervalEditText.setInputType(InputType.TYPE_CLASS_TEXT);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.calibrate_dialog_title));
        builder.setMessage(R.string.calibration_dialog_message);
        builder.setView(alertLayout);
        builder.setCancelable(false);
        builder.setPositiveButton(getString(R.string.set_calibration_button_title), (dialog, which) -> {
            try {
                String calibrationIntervalString = Objects.requireNonNull(calibrationIntervalEditText.getText()).toString();
                showValidationDialog(calibrationIntervalString, c.getDirection());
            } catch (NullPointerException ex) {
                dialog.cancel();
            }
        });
        builder.setNegativeButton(getString(R.string.cancel_title), (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void showValidationDialog(String calibrationIntervalString, Caliper.Direction direction) {
        CalibrationProcessor.Validation validation = CalibrationProcessor.validate(calibrationIntervalString, direction);
        EPSLog.log(validation.toString());
        dialogResult = calibrationIntervalString;
        if (validation.isValid()) {
            processCalibration();
        }
        else if (validation.noInput || validation.noNumber || validation.invalidNumber) {
            Alerts.simpleAlert(this, getString(R.string.calibration_error_title), getString(R.string.calibration_error_message));
        }
        else if (showValidationDialog &&
                (validation.noUnits
                        || (validation.invalidUnits && direction == Caliper.Direction.HORIZONTAL))) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            LayoutInflater inflater = getLayoutInflater();
            View alertLayout = inflater.inflate(R.layout.validate_calibration, null);
            final CheckBox doNotShowDialogCheckBox = alertLayout.findViewById(R.id.doNotShowValidationDialogCheckBox);
            doNotShowDialogCheckBox.setChecked(!showValidationDialog);
            final SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
            doNotShowDialogCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                showValidationDialog = !isChecked;
                editor.putBoolean(getString(R.string.show_validation_dialog_key), showValidationDialog);
                editor.apply();
            });
            builder.setTitle(R.string.calibration_warning_title);
            String message = validation.noUnits ? getString(R.string.calibration_no_units_message)
                    : getString(R.string.calibration_warning_message);
            builder.setMessage(message);
            builder.setCancelable(false);
            builder.setView(alertLayout);
            builder.setNegativeButton(R.string.cancel_title, (dialog, which) -> dialog.cancel());
            builder.setPositiveButton(R.string.calibrate_anyway_title, (dialog, which) -> processCalibration());
            builder.show();
        }
        else {
            processCalibration();
        }
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
        if (c == null) {
            return;
        }
        if (c.getValueInPoints() <= 0) {
            Alerts.simpleAlert(this, R.string.negative_caliper_title, R.string.negative_caliper_message);
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

    private void noCalipersAlert() {
        Alerts.simpleAlert(this, R.string.no_calipers_alert_title, R.string.no_caliper_selected_alert_message);
    }

    private void showFileErrorAlert() {
        Alerts.simpleAlert(this, R.string.file_load_error, R.string.file_load_message);
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

    private boolean thereAreCalipers() {
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
    @SuppressWarnings("unused")
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

    private void toggleMarchingCalipers() {
        calipersView.toggleShowMarchingCaliper();
        calipersView.invalidate();
    }

    private void addCaliperWithDirection(Caliper.Direction direction) {
        EPSLog.log("addCaliperWithDirection");
        addCaliperWithDirectionAtRect(direction, new Rect(0, 0, calipersView.getWidth(),
                calipersView.getHeight()));
        calipersView.invalidate();
        returnFromAddCaliperMenu();
    }

    private float dpToPixel(float dp) {
        float density = getResources().getDisplayMetrics().density;
        return dp * density;
    }

    private void setLineWidth(Caliper c, float width) {
        float pixels = dpToPixel(width);
        c.setLineWidth(pixels);
    }

    private void addCaliperWithDirectionAtRect(Caliper.Direction direction,
                                               Rect rect) {
        EPSLog.log("currentCaliperColor = " + currentCaliperColor);
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
        adjustCalibrationForScale(imageView.getScale());
        calipersView.invalidate();
    }

    private void adjustCalibrationForScale(float scale) {
        horizontalCalibration.setCurrentZoom(scale);
        verticalCalibration.setCurrentZoom(scale);
        horizontalCalibration.setOffset(new PointF(imageView.getDisplayRect().left, imageView.getDisplayRect().top));
        verticalCalibration.setOffset(new PointF(imageView.getDisplayRect().left, imageView.getDisplayRect().top));
        calipersView.invalidate();
    }

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

    private static class UriPage {
        Uri uri;
        int pageNumber;
    }


    // FIXME: source of crash
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
            if (activityWeakReference == null) {
                return;
            }
            activityWeakReference.get().findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
            Toast toast = Toast.makeText(activityWeakReference.get(), R.string.opening_pdf_message, Toast.LENGTH_SHORT);
            toast.show();

        }

        @Override
        protected Bitmap doInBackground(UriPage... params) {
            return getPdfBitmap(params[0]);
        }

        private Bitmap getPdfBitmap(UriPage param) {
            if (activityWeakReference == null) {
                return null;
            }
            UriPage uriPage = param;
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
                    File file = new File(Objects.requireNonNull(activityWeakReference.get().currentPdfUri.getPath()));
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
                File file = new File(Objects.requireNonNull(pdfUri.getPath()));
                ParcelFileDescriptor fd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
                PdfRenderer renderer = new PdfRenderer(fd);
                activityWeakReference.get().numberOfPdfPages = renderer.getPageCount();
                activityWeakReference.get().currentPdfPageNumber = uriPage.pageNumber;
                PdfRenderer.Page page = renderer.openPage(activityWeakReference.get().currentPdfPageNumber);

                int width = page.getWidth();
                int height = page.getHeight();

                Bitmap bitmap = Bitmap.createBitmap(width * 3, height * 3, Bitmap.Config.ARGB_8888);

                Matrix matrix = new Matrix();
                matrix.postScale(3, 3);

                page.render(bitmap, null, matrix, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                page.close();
                renderer.close();
                fd.close();
                return bitmap;
            } catch (IOException e) {
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
                // Don't set scale to minimum, is annoying with each page change.  Keep scale the same.
                //activityWeakReference.get().imageView.setScale(activityWeakReference.get().imageView.getMinimumScale());
                if (isNewPdf) {
                    activityWeakReference.get().clearCalibration();
                }
            }
            else {
                Toast toast = Toast.makeText(activityWeakReference.get(),
                        activityWeakReference.get().getString(R.string.pdf_error_message) +
                        "\n" + exceptionMessage, Toast.LENGTH_SHORT);
                toast.show();
            }
            activityWeakReference.get().findViewById(R.id.loadingPanel).setVisibility(View.GONE);
        }
    }

    private class MatrixChangeListener implements OnMatrixChangedListener {
        @Override
        public void onMatrixChanged(RectF rect) {
            matrixChangedAction();
        }
    }
}
