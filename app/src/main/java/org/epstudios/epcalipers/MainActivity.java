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
import android.graphics.drawable.ColorDrawable;
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
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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
import android.widget.TextView;
import android.widget.Toast;

import org.epstudios.epcalipers.QtcCalculator.QtcFormula;
import org.vudroid.core.DecodeServiceBase;
import org.vudroid.pdfdroid.codec.PdfContext;
import org.vudroid.pdfdroid.codec.PdfPage;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.co.senab.photoview.PhotoViewAttacher;

import static org.epstudios.epcalipers.MyPreferenceFragment.ALL;
import static org.epstudios.epcalipers.MyPreferenceFragment.BAZETT;
import static org.epstudios.epcalipers.MyPreferenceFragment.FRAMINGHAM;
import static org.epstudios.epcalipers.MyPreferenceFragment.FRIDERICIA;
import static org.epstudios.epcalipers.MyPreferenceFragment.HODGES;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    // TODO: regex below includes Cyrillic and must be updated with new alphabets
    private static final String calibrationRegex = "[.,0-9]+|[a-zA-ZА-яЁё]+";
    private static final Pattern VALID_PATTERN = Pattern.compile(calibrationRegex);
    private static final String EPS = "EPS";
    private static final String LF = "\n";
    private static final int RESULT_LOAD_IMAGE = 1;
    private static final int RESULT_CAPTURE_IMAGE = 2;
    private static final int DEFAULT_CALIPER_COLOR = Color.BLUE;
    private static final int DEFAULT_HIGHLIGHT_COLOR = Color.RED;
    private static final int DEFAULT_LINE_WIDTH = 2;
    private static final String TEMP_BITMAP_FILE_NAME = "/tempEPCalipersImageBitmap.png";
    private static final String ANGLE_B1 = "angleB1";
    private static final String ANGLE_B2 = "angleB2";

    // new permissions for Android >= 6.0
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 100;
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 101;
    private static final int MY_PERMISSIONS_REQUEST_STARTUP_IMAGE = 102;
    private static final int MY_PERMISSIONS_REQUEST_STARTUP_PDF = 103;
    private static final int MY_PERMISSIONS_REQUEST_STORE_BITMAP = 104;
    private static final int MY_PERMISSIONS_REQUEST_STARTUP_SENT_IMAGE = 105;
    private static final String HORIZONTAL = "Horizontal";
    private static final String VERTICAL = "Vertical";
    private static final String IMAGE_TYPE = "image/";
    private static final String APPLICATION_PDF_TYPE = "application/pdf";

    private Button addCaliperButton;
    private Button calibrateButton;
    private Button intervalRateButton;
    private Button meanRateButton;
    private Button qtcButton;
    private Button colorButton;
    private Button colorDoneButton;
    private Button tweakButton;
    private Button tweakDoneButton;
    private Button marchingButton;
    private Button cameraButton;
    private Button selectImageButton;
    private Button adjustImageButton;
    private Button imageLockButton;
    private Button sampleEcgButton;
    private Button previousPageButton;
    private Button nextPageButton;
    private Button rotateImageRightButton;
    private Button rotateImageLeftButton;
    private Button tweakImageRightButton;
    private Button tweakImageLeftButton;
    private Button microTweakImageRightButton;
    private Button microTweakImageLeftButton;
    private Button resetImageButton;
    private Button backToImageMenuButton;
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
    private HorizontalScrollView mainMenu;
    private HorizontalScrollView imageMenu;
    private HorizontalScrollView addCaliperMenu;
    private HorizontalScrollView adjustImageMenu;
    private HorizontalScrollView calibrationMenu;
    private HorizontalScrollView qtcStep1Menu;
    private HorizontalScrollView qtcStep2Menu;
    private HorizontalScrollView colorMenu;
    private HorizontalScrollView tweakMenu;
    private HorizontalScrollView microMovementMenu;
    private Calibration horizontalCalibration;
    private Calibration verticalCalibration;
    private ImageView imageView;
    private CalipersView calipersView;
    private Toolbar menuToolbar;
    private Toolbar actionBar;
    private boolean calipersMode;
    private PhotoViewAttacher attacher;
    private String currentPhotoPath;
    private RelativeLayout layout;
    private double rrIntervalForQTc;
    private float sizeDiffWidth;
    private float sizeDiffHeight;
    private boolean isRotatedImage;
    private float portraitWidth;
    private float portraitHeight;
    private float landscapeWidth;
    private float landscapeHeight;
    private boolean showStartImage;
    private boolean roundMsecRate;

    /// TODO: use these variables
    private Caliper.TextPosition timeCaliperTextPosition;
    private Caliper.TextPosition amplitudeCaliperTextPosition;
    
    private boolean allowTweakDuringQtc;
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
    private Menu menu;
    private boolean useLargeFont;
    private SharedPreferences.OnSharedPreferenceChangeListener listener;
    private final float max_zoom = 10.0f;
    private boolean imageIsLocked = false;

    private Bitmap previousBitmap = null;

    private List<Button> upDownButtons;
    private List<Button> rightLeftButtons;

//    private enum QtcFormulaPreference {
//        Bazett,
//        Framingham,
//        Hodges,
//        Fridericia,
//        All
//    }
    private Map<String, QtcFormula> qtcFormulaMap;
    // Bazett has the been the only option in previous versions of EP Calipers,
    // so it is now the default.  Users who want to change this must opt in using Preferences.
    private QtcFormula qtcFormulaPreference = QtcFormula.qtcBzt;

    // TODO: make false for release
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

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Log.d(EPS, "onCreate");

//        // see: https://stackoverflow.com/questions/38200282/android-os-fileuriexposedexception-file-storage-emulated-0-test-txt-exposed
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
//            StrictMode.setVmPolicy(builder.build());
//        }


        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        externalImageLoad = false;
        currentPdfUri = null;
        numberOfPdfPages = 0;
        currentPdfPageNumber = 0;
        useLargeFont = false;
        setContentView(R.layout.activity_main);
        findViewById(R.id.loadingPanel).setVisibility(View.GONE);

        noSavedInstance = (savedInstanceState == null);


        currentCaliperColor = DEFAULT_CALIPER_COLOR;
        currentHighlightColor = DEFAULT_HIGHLIGHT_COLOR;
        currentLineWidth = DEFAULT_LINE_WIDTH;

        qtcFormulaMap = new HashMap<>();
        qtcFormulaMap.put(BAZETT, QtcFormula.qtcBzt);
        qtcFormulaMap.put(FRAMINGHAM, QtcFormula.qtcFrm);
        qtcFormulaMap.put(HODGES, QtcFormula.qtcHdg);
        qtcFormulaMap.put(FRIDERICIA, QtcFormula.qtcFrd);
        qtcFormulaMap.put(ALL, QtcFormula.qtcAll);

        loadSettings();

        imageView = (ImageView) findViewById(R.id.imageView);
        // imageView always enabled in v2.0+
        imageView.setEnabled(true);
        if (!showStartImage && noSavedInstance) {
            imageView.setVisibility(View.INVISIBLE);
        }
        attacher = new PhotoViewAttacher(imageView);
        attacher.setScaleType(ImageView.ScaleType.CENTER);
        attacher.setMaximumScale(max_zoom);
        attacher.setMinimumScale(0.3f);
        // We need to use MatrixChangeListener and not ScaleChangeListener
        // since the former only fires when scale has completely changed and
        // the latter fires while the scale is changing, so is inaccurate.
        attacher.setOnMatrixChangeListener(new MatrixChangeListener());

        if (noSavedInstance && Intent.ACTION_SEND.equals(action) && type != null) {
            if (type.startsWith(IMAGE_TYPE)) {
                handleSentImage();
            }
        } else if (noSavedInstance && Intent.ACTION_VIEW.equals(action) && type != null) {
            if (type.equals(APPLICATION_PDF_TYPE)) {
                handlePDF();
            }
            if (type.startsWith(IMAGE_TYPE)) {
                handleImage();
            }
        }


        calipersView = (CalipersView) findViewById(R.id.caliperView);
        calipersView.setMainActivity(this);

        shortAnimationDuration = getResources().getInteger(
                android.R.integer.config_shortAnimTime);

        actionBar = (Toolbar) findViewById(R.id.action_bar);
        setSupportActionBar(actionBar);

        menuToolbar = (Toolbar) findViewById(R.id.menu_toolbar);

        createButtons();

        horizontalCalibration = new Calibration(Caliper.Direction.HORIZONTAL, this);
        verticalCalibration = new Calibration(Caliper.Direction.VERTICAL, this);

        rrIntervalForQTc = 0.0;

        totalRotation = 0.0f;

        calipersMode = true;
        selectMainMenu();

        // entry point to load external pics/pdfs
        if (externalImageLoad) {
            updateImageView(externalImageBitmap);
            externalImageLoad = false;
        }


        // OnSharedPreferenceListener must be a class field, i.e. strong reference
        // as otherwise it is a weak reference and will be garbage collected, thus
        // making it stop working.
        // See http://stackoverflow.com/questions/2542938/sharedpreferences-onsharedpreferencechangelistener-not-being-called-consistently
        listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                // show start image only has effect with restart
                Log.d(EPS, "onSharedPreferenceChangeListener");
                if (key.equals(getString(R.string.show_start_image_key))) {
                    return;
                }
                if (key.equals(getString(R.string.tweak_during_qtc_key))) {
                    allowTweakDuringQtc = sharedPreferences.getBoolean(key,
                            false);
                    return;
                }
                if (key.equals(getString(R.string.default_time_calibration_key))) {
                    defaultTimeCalibration = sharedPreferences.getString(key,
                            getString(R.string.default_time_calibration_value));
                    horizontalCalibration.setCalibrationString(defaultTimeCalibration);
                    return; // no need to invalidate calipersView.
                }
                if (key.equals(getString(R.string.default_amplitude_calibration_key))) {
                    defaultAmplitudeCalibration = sharedPreferences.getString(key,
                            getString(R.string.default_amplitude_calibration_value));
                    verticalCalibration.setCalibrationString(defaultAmplitudeCalibration);
                    return; // no need to invalidate calipersView.
                }
                if (key.equals(getString(R.string.default_qtc_formula_key))) {
                    String qtcFormulaName = sharedPreferences.getString(key,
                            getString(R.string.default_qtc_formula_value));
                    qtcFormulaPreference = qtcFormulaMap.get(qtcFormulaName);
                    return;  // no need to invalidate calipersView
                }
                if (key.equals(getString(R.string.default_caliper_color_key))) {
                    try {
                        int color = Integer.parseInt(sharedPreferences.getString(key,
                                getString(R.string.default_caliper_color)));
                        currentCaliperColor = color;
                        // We don't change already drawn calipers anymore,
                        // since we want to keep the custom colors that
                        // have already been applied.  Only new calipers
                        // will have the currentCaliperColor
                    } catch (Exception ex) {
                        return;
                    }
                }
                if (key.equals(getString(R.string.default_highlight_color_key))) {
                    try {
                        int color = Integer.parseInt(sharedPreferences.getString(key,
                                getString(R.string.default_highlight_color)));
                        currentHighlightColor = color;
                        for (Caliper c : calipersView.getCalipers()) {
                            c.setSelectedColor(color);
                            if (c.isSelected()) {
                                c.setColor(color);
                            }
                        }
                    } catch (Exception ex) {
                        return;
                    }
                }
                if (key.equals(getString(R.string.default_line_width_key))) {
                    try {
                        int lineWidth = Integer.parseInt(sharedPreferences.getString(key,
                                getString(R.string.default_line_width)));
                        currentLineWidth = lineWidth;
                        for (Caliper c : calipersView.getCalipers()) {
                            c.setLineWidth(lineWidth);
                        }
                    } catch (Exception ex) {
                        return;
                    }
                }
                if (key.equals(getString(R.string.use_large_font_key))) {
                    useLargeFont = sharedPreferences.getBoolean(key, false);
                    for (Caliper c : calipersView.getCalipers()) {
                        c.setUseLargeFont(useLargeFont);
                    }
                }
                if (key.equals(getString(R.string.round_msec_rate_key))) {
                    roundMsecRate = sharedPreferences.getBoolean(key, true);
                    for (Caliper c : calipersView.getCalipers()) {
                        c.setRoundMsecRate(roundMsecRate);
                    }
                }
                /// TODO: map strings to TextPosition and finish this
//                if (key.equals(getString(R.string.default_time_caliper_text_position_key))) {
//                    Caliper.TextPosition textPosition = sharedPreferences.getString(key, "centerBelow");
//                }
                calipersView.invalidate();
            }
        };

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        prefs.registerOnSharedPreferenceChangeListener(listener);

        layout = (RelativeLayout)findViewById(R.id.activity_main_id);
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
                //scaleImageForImageView();
                Log.d(EPS, "onGlobalLayoutListener called");

                if (noSavedInstance) {
                    addCaliperWithDirection(Caliper.Direction.HORIZONTAL);
                    Log.d(EPS, "ScaleImageForImageView()");
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
                        Log.d(EPS, "calipersView.getWidth() = " + calipersView.getWidth()
                                + " calipersView.getHeight() = " + calipersView.getHeight());
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
                attacher.setRotationBy(totalRotation);
            }
        });

        // TODO: update BOTH quick_start_messages (there are 2 strings.xml files)
        //noinspection ConstantConditions
        if (force_first_run || getFirstRun(prefs)) {
            Log.d(EPS, "firstRun");
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
        Log.d(EPS, "handleImage");
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
        Log.d(EPS, "handleSentImage");
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

    private void epsLog(String s) {
        Log.d(EPS, s);
    }

    private void proceedToHandleImage() {
        try {
            Uri imageUri = getIntent().getData();
            if (imageUri != null) {
                Log.d(EPS, "imageUri = " + imageUri.toString());
                externalImageLoad = true;
                externalImageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            }
        }
        catch (Exception e) {
            showFileErrorAlert();
        }
    }

    private void proceedToHandleSentImage() {
        try {
            Uri imageUri = getIntent().getParcelableExtra(Intent.EXTRA_STREAM);
            if (imageUri != null) {
                Log.d(EPS, "imageUri = " + imageUri.toString());
                externalImageLoad = true;
                externalImageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            }
        }
        catch (Exception e) {
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
            new NougatAsyncLoadPDF().execute(uriPage);
        }
        else {
            new AsyncLoadPDF().execute(uriPage);
        }
    }

    private Uri getTempUri(Uri uri) {
        try {
            Log.d(EPS, "File uri is " + uri.getPath());
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
            Log.d(EPS, "bytes length is " + bytes.length);
            File file = createTmpPdfFile();
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            bos.write(bytes);
            bos.flush();
            bos.close();
            return Uri.fromFile(file);
        }
        catch (Exception e) {
            return null;
        }
    }

    private class UriPage {
        public Uri uri;
        public int pageNumber;
    }

    @TargetApi(25)
    private class NougatAsyncLoadPDF extends AsyncTask<UriPage, Void, Bitmap> {
        private boolean isNewPdf;
        private String exceptionMessage = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
            Toast toast = Toast.makeText(getApplicationContext(), R.string.opening_pdf_message, Toast.LENGTH_SHORT);
            toast.show();

        }

        @Override
        protected Bitmap doInBackground(UriPage... params) {
            UriPage uriPage = params[0];
            Uri pdfUri = uriPage.uri;
            if (pdfUri == null) {
                if (currentPdfUri == null) {
                    // can't do anything if all is null
                    return null;
                }
                // use currently opened PDF
                pdfUri = currentPdfUri;
                isNewPdf = false;
            }
            else {
                // change Uri to a real file path
                pdfUri = getTempUri(pdfUri);
                // if getTempUri returns null then exception was thrown
                if (pdfUri == null) {
                    return null;
                }
                // close old currentPdfUri if possible
                if (currentPdfUri != null) {
                    File file = new File(currentPdfUri.getPath());
                    if (file.exists()) {
                        //noinspection ResultOfMethodCallIgnored
                        file.delete();
                    }
                }
                // retain PDF Uri for future page changes
                currentPdfUri = pdfUri;
                isNewPdf = true;
            }
            try {
                File file = new File(pdfUri.getPath());
                ParcelFileDescriptor fd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
                PdfRenderer renderer = new PdfRenderer(fd);
                numberOfPdfPages = renderer.getPageCount();
                currentPdfPageNumber = uriPage.pageNumber;
                PdfRenderer.Page page = renderer.openPage(currentPdfPageNumber);

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
            } catch (Exception e) {
                // catch out of memory errors and just don't load rather than crash
                exceptionMessage = e.getMessage();
                return null;
            }
        }

        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null) {
                // Set pdf bitmap directly, scaling screws it up
                imageView.setImageBitmap(bitmap);
                // must set visibility as imageview will be hidden if started with sample ecg hidden
                imageView.setVisibility(View.VISIBLE);
                attacher.update();
                attacher.setScale(attacher.getMinimumScale());
                if (isNewPdf) {
                    clearCalibration();
                }
            }
            else {
                Toast toast = Toast.makeText(getApplicationContext(), getString(R.string.pdf_error_message) +
                        LF + exceptionMessage, Toast.LENGTH_SHORT);
                toast.show();
            }
            findViewById(R.id.loadingPanel).setVisibility(View.GONE);
        }
    }


    private class AsyncLoadPDF extends AsyncTask<UriPage,
            Void, Bitmap> {
        private boolean isNewPdf;
        private String exceptionMessage = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
            Toast toast = Toast.makeText(getApplicationContext(), R.string.opening_pdf_message, Toast.LENGTH_SHORT);
            toast.show();

        }

        @Override
        protected Bitmap doInBackground(UriPage... params) {
            DecodeServiceBase decodeService = new DecodeServiceBase(new PdfContext());
            decodeService.setContentResolver(getContentResolver());
            UriPage uriPage = params[0];
            Uri pdfUri = uriPage.uri;
            if (pdfUri == null) {
                if (currentPdfUri == null) {
                    // can't do anything if all is null
                    return null;
                }
                // use currently opened PDF
                pdfUri = currentPdfUri;
                isNewPdf = false;
            }
            else {
                // change Uri to a real file path
                pdfUri = getTempUri(pdfUri);
                // if getTempUri returns null then exception was thrown
                if (pdfUri == null) {
                    return null;
                }
                // close old currentPdfUri if possible
                if (currentPdfUri != null) {
                    File file = new File(currentPdfUri.getPath());
                    if (file.exists()) {
                        //noinspection ResultOfMethodCallIgnored
                        file.delete();
                    }
                }
                // retain PDF Uri for future page changes
                currentPdfUri = pdfUri;
                isNewPdf = true;
            }
            try {
                decodeService.open(pdfUri);
                numberOfPdfPages = decodeService.getPageCount();
                currentPdfPageNumber = uriPage.pageNumber;
                PdfPage page = (PdfPage) decodeService.getPage(currentPdfPageNumber);

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
            if (bitmap != null) {
                // Set pdf bitmap directly, scaling screws it up
                imageView.setImageBitmap(bitmap);
                // must set visibility as imageview will be hidden if started with sample ecg hidden
                imageView.setVisibility(View.VISIBLE);
                attacher.update();
                attacher.setScale(attacher.getMinimumScale());
                if (isNewPdf) {
                    clearCalibration();
                }
            }
            else {
                Toast toast = Toast.makeText(getApplicationContext(), getString(R.string.pdf_error_message) +
                        LF + exceptionMessage, Toast.LENGTH_SHORT);
                toast.show();
            }
            findViewById(R.id.loadingPanel).setVisibility(View.GONE);
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

    public boolean getFirstRun(SharedPreferences prefs) {
      return prefs.getBoolean("firstRun" + About.VERSION, true);
    }

    public void setRunned(SharedPreferences prefs) {
        SharedPreferences.Editor edit = prefs.edit();
        edit.putBoolean("firstRun" + About.VERSION, false);
        edit.apply();
    }


    void loadSettings() {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(this);
        showStartImage = sharedPreferences.getBoolean(
                getString(R.string.show_start_image_key), true);
        roundMsecRate = sharedPreferences.getBoolean(getString(R.string.round_msec_rate_key), true);
        defaultTimeCalibration = sharedPreferences.getString(getString(
                R.string.default_time_calibration_key), getString(R.string.default_time_calibration_value));
        defaultAmplitudeCalibration = sharedPreferences.getString(
                getString(R.string.default_amplitude_calibration_key), getString(R.string.default_amplitude_calibration_value));
        useLargeFont = sharedPreferences.getBoolean(getString(R.string.use_large_font_key), false);
        allowTweakDuringQtc = sharedPreferences.getBoolean(getString(R.string.tweak_during_qtc_key), false);
        String qtcFormulaName = sharedPreferences.getString(getString(R.string.default_qtc_formula_key),
                getString(R.string.default_qtc_formula_value));
        qtcFormulaPreference = qtcFormulaMap.get(qtcFormulaName);
        try {
            currentCaliperColor = Integer.parseInt(sharedPreferences.getString(getString(R.string.default_caliper_color_key),
                    Integer.valueOf(DEFAULT_CALIPER_COLOR).toString()));
            currentHighlightColor = Integer.parseInt(sharedPreferences.getString(getString(R.string.default_highlight_color_key),
                    Integer.valueOf(DEFAULT_HIGHLIGHT_COLOR).toString()));
            currentLineWidth = Integer.parseInt(sharedPreferences.getString(getString(R.string.default_line_width_key),
                    Integer.valueOf(DEFAULT_LINE_WIDTH).toString()));
        } catch (Exception ex) {
            currentCaliperColor = DEFAULT_CALIPER_COLOR;
            currentHighlightColor = DEFAULT_HIGHLIGHT_COLOR;
            currentLineWidth = DEFAULT_LINE_WIDTH;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(EPS, "onResume");

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

//        Log.d(EPS, "ActionBar height = " + actionBarHeight + " Toolbar height = " +
//                toolbarHeight + " StatusBar height = " + statusBarHeight);
//        Log.d(EPS, "ImageView height = " + imageView.getHeight());
//        Log.d(EPS, "Screen height = " + screenHeight);
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
//        Log.d(EPS, "imageWidth = " + imageWidth + " imageHeight = " + imageHeight);
//        Log.d(EPS, "bitmapWidth = " + bitmapWidth + " bitmapHeight = " +
//                bitmapHeight);
        Matrix matrix = new Matrix();
        matrix.postScale(ratio, ratio);
//        Log.d(EPS, "ratio = " + ratio);
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
        Log.d(EPS, "onSaveInstanceState");
        super.onSaveInstanceState(outState);
        outState.putBoolean("calipersMode", calipersMode);
        outState.putFloat("scale", attacher.getScale());
        outState.putFloat("totalRotation", totalRotation);
        outState.putBoolean("imageIsLocked", imageIsLocked);
        outState.putBoolean("multipagePDF", numberOfPdfPages > 0);
        outState.putBoolean("aCaliperIsMarching", calipersView.isACaliperIsMarching());

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
        outState.putString("hcalUnits", horizontalCalibration.getRawUnits());
        outState.putString("hcalCalibrationString", horizontalCalibration.getCalibrationString());
        outState.putBoolean("hcalDisplayRate", horizontalCalibration.getDisplayRate());
        outState.putFloat("hcalOriginalZoom", horizontalCalibration.getOriginalZoom());
        outState.putFloat("hcalCurrentZoom", horizontalCalibration.getCurrentZoom());
        outState.putBoolean("hcalCalibrated", horizontalCalibration.isCalibrated());
        outState.putFloat(("hcalOriginalCalFactor"), horizontalCalibration.getOriginalCalFactor());

        outState.putString("vcalUnits", verticalCalibration.getRawUnits());
        outState.putString("vcalCalibrationString", verticalCalibration.getCalibrationString());
        outState.putBoolean("vcalDisplayRate", verticalCalibration.getDisplayRate());
        outState.putFloat("vcalOriginalZoom", verticalCalibration.getOriginalZoom());
        outState.putFloat("vcalCurrentZoom", verticalCalibration.getCurrentZoom());
        outState.putBoolean("vcalCalibrated", verticalCalibration.isCalibrated());
        outState.putFloat("vcalOriginalCalFactor", verticalCalibration.getOriginalCalFactor());
        // save calipers
        for (int i = 0; i < calipersCount(); i++) {
            Caliper c = calipersView.getCalipers().get(i);
            outState.putString(i + "CaliperDirection",
                    c.getDirection() == Caliper.Direction.HORIZONTAL ?
                            HORIZONTAL : VERTICAL);
            // maxX normalizes bar and crossbar positions regardless of caliper direction,
            // i.e. X is direction for bars and Y is direction for crossbars.
            float maxX = c.getDirection() == Caliper.Direction.HORIZONTAL
                    ? calipersView.getWidth()
                    : calipersView.getHeight();
            float maxY = c.getDirection() == Caliper.Direction.HORIZONTAL
                    ? calipersView.getHeight()
                    : calipersView.getWidth();
            outState.putFloat(i + "CaliperBar1Position",
                    transformCoordinate(c.getBar1Position(), maxX));
            outState.putFloat(i + "CaliperBar2Position",
                    transformCoordinate(c.getBar2Position(), maxX));
            outState.putFloat(i + "CaliperCrossbarPosition",
                    transformCoordinate(c.getCrossbarPosition(), maxY));
            outState.putBoolean(i + "CaliperSelected", c.isSelected());
            outState.putBoolean(i + "IsAngleCaliper", c.isAngleCaliper());
            outState.putInt(i + "UnselectedColor", c.getUnselectedColor());
            outState.putBoolean(i + "MarchingCaliper", c.isMarching());

            if (c.isAngleCaliper()) {
                outState.putDouble(i + ANGLE_B1, ((AngleCaliper)c).getBar1Angle());
                outState.putDouble(i + ANGLE_B2, ((AngleCaliper)c).getBar2Angle());
            }
            else {
                outState.putDouble(i + ANGLE_B1, 0.0);
                outState.putDouble(i + ANGLE_B2, 0.0);
            }


        }

        outState.putInt("CalipersCount", calipersCount());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d(EPS, "onRestoreInstanceState");
        calipersMode = savedInstanceState.getBoolean("calipersMode");
        imageIsLocked = savedInstanceState.getBoolean("imageIsLocked");
        lockImage(imageIsLocked);
        calipersView.setACaliperIsMarching(savedInstanceState.getBoolean("aCaliperIsMarching"));

        boolean isMultipagePdf = savedInstanceState.getBoolean("multipagePDF");

        // Bitmap now passed via temporary file
        //Bitmap image = savedInstanceState.getParcelable("Image");
        Bitmap image = getBitmapFromTempFile();
        imageView.setImageBitmap(image);
        previousBitmap = image;

        totalRotation = savedInstanceState.getFloat("totalRotation");

        attacher.update();
        float scale = Math.max(savedInstanceState.getFloat("scale"), attacher.getMinimumScale());
        scale = Math.min(scale, attacher.getMaximumScale());
        attacher.setScale(scale, true);

        // Calibration
        horizontalCalibration.setUnits(savedInstanceState.getString("hcalUnits"));
        horizontalCalibration.setCalibrationString(savedInstanceState.getString("hcalCalibrationString"));
        horizontalCalibration.setDisplayRate(savedInstanceState.getBoolean("hcalDisplayRate"));
        horizontalCalibration.setOriginalZoom(savedInstanceState.getFloat("hcalOriginalZoom"));
        horizontalCalibration.setCurrentZoom(savedInstanceState.getFloat("hcalCurrentZoom"));
        horizontalCalibration.setCalibrated(savedInstanceState.getBoolean("hcalCalibrated"));
        horizontalCalibration.setOriginalCalFactor(savedInstanceState.getFloat("hcalOriginalCalFactor"));

        verticalCalibration.setUnits(savedInstanceState.getString("vcalUnits"));
        verticalCalibration.setCalibrationString(savedInstanceState.getString("vcalCalibrationString"));
        verticalCalibration.setDisplayRate(savedInstanceState.getBoolean("vcalDisplayRate"));
        verticalCalibration.setOriginalZoom(savedInstanceState.getFloat("vcalOriginalZoom"));
        verticalCalibration.setCurrentZoom(savedInstanceState.getFloat("vcalCurrentZoom"));
        verticalCalibration.setCalibrated(savedInstanceState.getBoolean("vcalCalibrated"));
        verticalCalibration.setOriginalCalFactor(savedInstanceState.getFloat("vcalOriginalCalFactor"));

        // restore calipers
        int calipersCount = savedInstanceState.getInt("CalipersCount");
        for (int i = 0; i < calipersCount; i++) {
            String directionString = savedInstanceState.getString(i + "CaliperDirection");
            boolean isAngleCaliper = savedInstanceState.getBoolean(i + "IsAngleCaliper");
            boolean isMarching = savedInstanceState.getBoolean(i + "MarchingCaliper");
            if (directionString == null) {
                // something very wrong, give up on restoring calipers
                return;
            }
            int unselectedColor = savedInstanceState.getInt(i + "UnselectedColor");
            Caliper.Direction direction = directionString.equals(HORIZONTAL) ?
                    Caliper.Direction.HORIZONTAL : Caliper.Direction.VERTICAL;
            float bar1Position = savedInstanceState.getFloat(i + "CaliperBar1Position");
            float bar2Position = savedInstanceState.getFloat(i + "CaliperBar2Position");
            double bar1Angle = savedInstanceState.getDouble(i + ANGLE_B1);
            double bar2Angle = savedInstanceState.getDouble(i + ANGLE_B2);
            float crossbarPosition = savedInstanceState.getFloat(i + "CaliperCrossbarPosition");
            boolean selected = savedInstanceState.getBoolean(i + "CaliperSelected");

            Caliper c;
            if (isAngleCaliper) {
                c = new AngleCaliper();
                ((AngleCaliper)c).setVerticalCalibration(verticalCalibration);
            }
            else {
                c = new Caliper();
            }
            c.setDirection(direction);

            c.setBar1Position(bar1Position);
            c.setBar2Position(bar2Position);
            c.setCrossbarPosition(crossbarPosition);
            c.setSelected(selected);
            c.setUnselectedColor(unselectedColor);
            c.setSelectedColor(currentHighlightColor);
            c.setColor(c.isSelected() ? currentHighlightColor : unselectedColor);
            c.setLineWidth(currentLineWidth);
            c.setUseLargeFont(useLargeFont);
            c.setRoundMsecRate(roundMsecRate);
            c.setMarching(isMarching);
            if (c.getDirection() == Caliper.Direction.HORIZONTAL) {
                c.setCalibration(horizontalCalibration);
            }
            else {
                c.setCalibration(verticalCalibration);
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
            File file = new File(getCacheDir() + TEMP_BITMAP_FILE_NAME);
            FileOutputStream fOut = new FileOutputStream(file);

            bitmap.compress(Bitmap.CompressFormat.PNG, 85, fOut);
            fOut.flush();
            fOut.close();
        }
        catch (Exception ex) {
            Toast toast = Toast.makeText(this, R.string.temp_image_file_warning, Toast.LENGTH_SHORT);
            toast.show();
            Log.d(EPS, "Could not store temp file");
        }
    }

    private Bitmap getBitmapFromTempFile() {

            String path = getCacheDir() + TEMP_BITMAP_FILE_NAME;
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
        } else if (v == microTweakImageLeftButton) {
            rotateImage(-0.1f);
        } else if (v == microTweakImageRightButton) {
            rotateImage(0.1f);
        } else if (v == resetImageButton) {
            resetImage();
        } else if (v == horizontalCaliperButton) {
            addCaliperWithDirection(Caliper.Direction.HORIZONTAL);
        } else if (v == verticalCaliperButton) {
            addCaliperWithDirection(Caliper.Direction.VERTICAL);
        } else if (v == angleCaliperButton) {
            addAngleCaliper();
        } else if (v == selectImageButton) {
            selectImageFromGallery();
        } else if (v == cameraButton) {
            takePhoto();
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
        } else if (v == cancelQTcButton) {
            selectMainMenu();
        } else if (v == measureRRButton) {
            qtcMeasureRR();
        } else if (v == measureQTButton) {
            doQTcCalculation();
        } else if (v == cancelQTcMeasurementButton) {
            selectMainMenu();
        } else if (v == imageLockButton) {
            lockImage();
        } else if (v == sampleEcgButton) {
            loadSampleEcg();
        } else if (v == previousPageButton) {
            showPreviousPage();
        } else if (v == nextPageButton) {
            showNextPage();
        } else if (v == colorButton) {
            selectColorMenu();
        } else if (v == colorDoneButton) {
            selectMainMenu();
        } else if (v == tweakButton) {
            selectTweakMenu();
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
        } else if (v == marchingButton) {
            toggleMarchingCalipers();
        }

    }

    private void createButtons() {
        // Main/Caliper menu
        addCaliperButton = createButton(getString(R.string.add_caliper_button_title));
        calibrateButton = createButton(getString(R.string.calibrate_button_title));
        intervalRateButton = createButton(getString(R.string.interval_rate_button_title));
        meanRateButton = createButton(getString(R.string.mean_rate_button_title));
        qtcButton = createButton(getString(R.string.qtc_button_title));
        // Image menuº
        cameraButton = createButton(getString(R.string.camera_button_title));
        cameraButton.setEnabled(getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA));
        selectImageButton = createButton(getString(R.string.select_image_button_title));
        adjustImageButton = createButton(getString(R.string.adjust_image_button_title));
        imageLockButton = createButton(getString(R.string.lock_label));
        sampleEcgButton = createButton(getString(R.string.sample_label));
        previousPageButton = createButton(getString(R.string.previous_button_label));
        nextPageButton = createButton(getString(R.string.next_button_label));
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
        backToImageMenuButton = createButton(getString(R.string.done_button_title));
        // Calibration menu
        setCalibrationButton = createButton(getString(R.string.set_calibration_button_title));
        clearCalibrationButton = createButton(getString(R.string.clear_calibration_button_title));
        doneCalibrationButton = createButton(getString(R.string.done_button_title));
        // QTc menu
        measureRRButton = createButton(getString(R.string.measure_button_label));
        cancelQTcButton = createButton(getString(R.string.cancel_button_title));
        measureQTButton = createButton(getString(R.string.measure_button_label));
        cancelQTcMeasurementButton = createButton(getString(R.string.cancel_button_title));
        // Color menu
        colorButton = createButton(getString(R.string.color_label));
        colorDoneButton = createButton(getString(R.string.done_button_title));
        // Tweak menu
        tweakButton = createButton(getString(R.string.tweak_label));
        tweakDoneButton = createButton(getString(R.string.done_button_title));
        // Marching calipers
        marchingButton = createButton(getString(R.string.marching_label));
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
        buttons.add(addCaliperButton);
        buttons.add(calibrateButton);
        buttons.add(intervalRateButton);
        buttons.add(meanRateButton);
        buttons.add(qtcButton);
        buttons.add(colorButton);
        buttons.add(tweakButton);
        buttons.add(marchingButton);
        mainMenu = createMenu(buttons);
    }

    private void createImageMenu() {
        ArrayList<TextView> buttons = new ArrayList<>();
        buttons.add(cameraButton);
        buttons.add(selectImageButton);
        buttons.add(adjustImageButton);
        buttons.add(imageLockButton);
        buttons.add(sampleEcgButton);
        buttons.add(previousPageButton);
        buttons.add(nextPageButton);
        imageMenu = createMenu(buttons);
    }

    private void createAddCaliperMenu() {
        ArrayList<TextView> buttons = new ArrayList<>();
        buttons.add(horizontalCaliperButton);
        buttons.add(verticalCaliperButton);
        buttons.add(angleCaliperButton);
        buttons.add(cancelAddCaliperButton);
        addCaliperMenu = createMenu(buttons);
    }

    private void createAdjustImageMenu() {
        ArrayList<TextView> buttons = new ArrayList<>();
        buttons.add(rotateImageLeftButton);
        buttons.add(rotateImageRightButton);
        buttons.add(tweakImageLeftButton);
        buttons.add(tweakImageRightButton);
        buttons.add(microTweakImageLeftButton);
        buttons.add(microTweakImageRightButton);
        buttons.add(resetImageButton);
        buttons.add(backToImageMenuButton);
        adjustImageMenu = createMenu(buttons);
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

    private void selectMainMenu() {
        if (mainMenu == null) {
            createMainMenu();
        }
        selectMenu(mainMenu);
        boolean enable = horizontalCalibration.canDisplayRate();
        intervalRateButton.setEnabled(enable);
        meanRateButton.setEnabled(enable);
        qtcButton.setEnabled(enable);
        calipersView.setLocked(false);
        calipersView.setAllowTweakPosition(false);
        calipersView.setAllowColorChange(false);
        inQtc = false;
    }

    private void selectImageMenu() {
        if (imageMenu == null) {
            createImageMenu();
        }
        boolean enable = (numberOfPdfPages  > 0);
        enablePageButtons(enable);
        selectMenu(imageMenu);
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

    private void selectQTcStep1Menu() {
        if (qtcStep1Menu == null) {
            createQTcStep1Menu();
        }
        selectMenu(qtcStep1Menu);
    }

    private void selectQTcStep2Menu() {
        if (qtcStep2Menu == null) {
            createQTcStep2Menu();
        }
        selectMenu(qtcStep2Menu);
        inQtc = true;
        calipersView.setAllowTweakPosition(allowTweakDuringQtc);
    }

    private void selectColorMenu() {
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

    private void selectTweakMenu() {
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
        Log.d(EPS, "onCreateOptionsMenu");
        // Inflate the menu; this adds items to the action bar if it is present.
        this.menu = menu;
        getMenuInflater().inflate(R.menu.menu_main, menu);
        setMode();
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


    private void lockImage() {
        imageIsLocked = !imageIsLocked;
        lockImage(imageIsLocked);
    }

    private void lockImage(boolean lock) {
        imageView.setEnabled(!lock);
        calipersView.setLockImage(lock);
        if (lock) {
            imageLockButton.setText(getString(R.string.unlock_label));
        }
        else {
            imageLockButton.setText(getString(R.string.lock_label));
        }
        calipersView.invalidate();
    }

    private void setMode() {
        // imageView is always enabled now that touch events pass through
        // imageView.setEnabled(!calipersMode);
        calipersView.setEnabled(calipersMode);

        MenuItem switchModeMenuItem = menu.findItem(R.id.action_switch);

        if (calipersMode) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(getString(R.string.ep_calipers_title));
                getSupportActionBar().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.primary)));
            }
            unfadeCalipersView();
            switchModeMenuItem.setTitle(R.string.image_button_title);
            selectMainMenu();
        } else {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(getString(R.string.image_mode_title));
                getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.BLACK));
            }
            fadeCalipersView();
            switchModeMenuItem.setTitle(R.string.measure_button_title);
            selectImageMenu();
        }
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
                    Log.d(EPS, "Camera permission granted");
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    proceedToTakePhoto();
                } else {
                    Log.d(EPS, "Camera permission denied");
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                break;
            }
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(EPS, "Write External storage permission granted");
                    proceedToSelectImageFromGallery();
                } else {
                    Log.d(EPS, "Write external storage permission denied");
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                break;
            }
            case MY_PERMISSIONS_REQUEST_STARTUP_IMAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(EPS, "Write External storage permission granted");
                    proceedToHandleImage();
                } else {
                    Log.d(EPS, "Write external storage permission denied");
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                break;
            }
            case MY_PERMISSIONS_REQUEST_STARTUP_SENT_IMAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(EPS, "Write External storage permission granted");
                    proceedToHandleSentImage();
                } else {
                    Log.d(EPS, "Write external storage permission denied");
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                break;
            }
            case MY_PERMISSIONS_REQUEST_STARTUP_PDF: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(EPS, "Write External storage permission granted");
                    proceedToHandlePDF();
                } else {
                    Log.d(EPS, "Write external storage permission denied");
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                break;
            }
            case MY_PERMISSIONS_REQUEST_STORE_BITMAP: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(EPS, "Write External storage permission granted");
                    proceedToStoreBitmap();
                } else {
                    Log.d(EPS, "Write external storage permission denied");
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                break;
            }

        }
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
        attacher.update();
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
        attacher.setRotationBy(degrees);
    }

    private void resetImage() {
        totalRotation = 0.0f;
        attacher.setRotationTo(0f);
    }

    private void showHelp() {
        startActivity(new Intent(this, Help.class));
    }

    private void about() {
        startActivity(new Intent(this, About.class));
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
        } catch (Exception ex) {
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
        microMoveBar(calipersView.activeCaliper(), calipersView.getPressedComponent(), -1f, Caliper.MovementDirection.Left);
    }

    private void right() {
        microMoveBar(calipersView.activeCaliper(), calipersView.getPressedComponent(), 1f, Caliper.MovementDirection.Right);
    }

    private void microLeft() {
        microMoveBar(calipersView.activeCaliper(), calipersView.getPressedComponent(), -0.1f, Caliper.MovementDirection.Left);
    }

    private void microRight() {
        microMoveBar(calipersView.activeCaliper(), calipersView.getPressedComponent(), 0.1f, Caliper.MovementDirection.Right);
    }

    private void up() {
        microMoveBar(calipersView.activeCaliper(), calipersView.getPressedComponent(), -1f, Caliper.MovementDirection.Up);
    }

    private void down() {
        microMoveBar(calipersView.activeCaliper(), calipersView.getPressedComponent(), 1f, Caliper.MovementDirection.Down);
    }

    private void microUp() {
        microMoveBar(calipersView.activeCaliper(), calipersView.getPressedComponent(), -0.1f, Caliper.MovementDirection.Up);
    }

    private void microDown() {
        microMoveBar(calipersView.activeCaliper(), calipersView.getPressedComponent(), 0.1f, Caliper.MovementDirection.Down);
    }

    private void tweakDone() {
        selectMainMenu();
    }

    private void microDone() {
        if (inQtc && allowTweakDuringQtc) {
            selectQTcStep2Menu();
        }
        else {
            calipersView.setLocked(false);
            selectMainMenu();
        }
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

    private void fadeCalipersView() {
        calipersView.setAlpha(0.5f);
    }

    private void unfadeCalipersView() {
        calipersView.setAlpha(1.0f);
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

    private void toggleMarchingCalipers() {
        calipersView.toggleShowMarchingCaliper();
        calipersView.invalidate();
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
        c.setUnselectedColor(currentCaliperColor);
        c.setSelectedColor(currentHighlightColor);
        c.setColor(currentCaliperColor);
        c.setLineWidth(currentLineWidth);
        c.setDirection(direction);
        if (direction == Caliper.Direction.HORIZONTAL) {
            c.setCalibration(horizontalCalibration);
        } else {
            c.setCalibration(verticalCalibration);
        }
        c.setUseLargeFont(useLargeFont);
        c.setRoundMsecRate(roundMsecRate);
        c.setInitialPosition(rect);
        getCalipers().add(c);
    }

    private void addAngleCaliper() {
        AngleCaliper c = new AngleCaliper();
        Rect rect = new Rect(0, 0, calipersView.getWidth(),
                calipersView.getHeight());
        c.setUnselectedColor(currentCaliperColor);
        c.setSelectedColor(currentHighlightColor);
        c.setColor(currentCaliperColor);
        c.setLineWidth(currentLineWidth);
        c.setUseLargeFont(useLargeFont);
        c.setRoundMsecRate(roundMsecRate);
        c.setDirection(Caliper.Direction.HORIZONTAL);
        c.setCalibration(horizontalCalibration);
        c.setVerticalCalibration(verticalCalibration);
        c.setInitialPosition(rect);
        getCalipers().add(c);
        calipersView.invalidate();
        selectMainMenu();
    }

    private void matrixChangedAction() {
        //Log.d(EPS, "Matrix changed, scale = " + attacher.getScale());
        adjustCalibrationForScale(attacher.getScale());
        calipersView.invalidate();
    }

    private void adjustCalibrationForScale(float scale) {
        horizontalCalibration.setCurrentZoom(scale);
        verticalCalibration.setCurrentZoom(scale);
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

    private class MatrixChangeListener implements PhotoViewAttacher.OnMatrixChangedListener {

        @Override
        public void onMatrixChanged(RectF rect) {
            matrixChangedAction();

        }
    }

}



