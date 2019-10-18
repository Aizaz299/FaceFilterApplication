package com.gsrathoreniks.facefilter;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.gsrathoreniks.facefilter.camera.CameraSourcePreview;
import com.gsrathoreniks.facefilter.camera.GraphicOverlay;
import com.gsrathoreniks.facefilter.model.GeoFence;
import com.gsrathoreniks.facefilter.model.Session;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.view.View.GONE;

public class FaceFilterActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private File mImageFile;
    private FirebaseAuth mAuth;
    private ImageView gymImageView;
    private RelativeLayout relativeLayout;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    boolean isConfirmed;

    private static final String TAG = "Kainat";
    private Location lastLocation;          //Last known location of the user.
    private GoogleApiClient googleApiClient;//For location services
    private LocationRequest locationRequest;//For location getting we request android os through this

    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;//Permission request code to identify this intent in onActivityResult
    private final int UPDATE_INTERVAL = 10000;//Get location after this milliseconds. In this case its 1 second
    private final int FASTEST_INTERVAL = 9000;//If location is available sooner you can get it using this time interval
    private final int REQ_PERMISSION = 999;//Request code to use for requesting location.
    private ImageButton noFilter,hair,op,snap,glasses2,glasses3,glasses4,glasses5,mask,mask2,mask3,dog,cat2;//Image buttons objs from front end
    private ImageButton stillGym, stillGymFull, stillGymNoTeam, stillGymRacket, stillTutiFruity, stillNCLib, stillMissSaima, stillLawLib, stillFinalsWeek, stillDroplane, stillSelected;//Image buttons objs from front end
    private List<ImageButton> filtersList;//Image Buttons list to enable or disable on loaction change
    private TextView geofenceName;//in which geofence you are will be displayed in this textview from the front end
    private FaceDetector detector;//FaceDetector api from google for finding faces
    private boolean isFacing;//is front camera = true else false

    private CameraSource mCameraSource = null;//CameraSource api from google which keeps sending frames to the FaceDetector continuously for checking if face is detected.
    private int typeFace = 0;//loop for setting images to the front end's image buttons having filters in each.
    //All the ids of imagebuttons from front end to initialize them in java code and put images on them
    private static final int MASK[] = {
            R.id.no_filter,
            R.id.hair,
            R.id.op,
            R.id.snap,
            R.id.glasses2,
            R.id.glasses3,
            R.id.glasses4,
            R.id.glasses5,
            R.id.mask,
            R.id.mask2,
            R.id.mask3,
            R.id.dog,
            R.id.cat2,
    };

    private CameraSourcePreview mPreview;
    private GraphicOverlay mGraphicOverlay;
    private StorageReference mStorageRef;
    private LinearLayout linearLayout;

    private static final int RC_HANDLE_GMS = 9001;
    // permission request codes need to be < 256
    private static final int RC_HANDLE_CAMERA_PERM = 2;

    //==============================================================================================
    // Activity Methods
    //==============================================================================================

    /**
     * Initializes the UI and initiates the creation of a face detector.
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        setContentView(R.layout.activity_face_filter);//Setting layout file to be displayed when this activity will be called
        filtersList = new ArrayList<>();              //Filters list from which some filters will be disabled or enabled based on the location
        linearLayout = findViewById(R.id.buttonsView);
        mStorageRef = FirebaseStorage.getInstance().getReference();
        createGoogleApi();                           //Google api for location.
        setGeofences();                              //Geofences list will be altered in this method according to your universities' locations

        mPreview = (CameraSourcePreview) findViewById(R.id.preview);//Front end other than the bottom buttons
        mGraphicOverlay = (GraphicOverlay) findViewById(R.id.faceOverlay);//Filter that will be displayed on the screen when face will be detected
        geofenceName = findViewById(R.id.geofence_name);//Location name will be displayed in this textview
        gymImageView = findViewById(R.id.img_gym);
        relativeLayout = findViewById(R.id.rel);
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        if(!isMyServiceRunning(MyService.class))
            startService(new Intent(getBaseContext(), MyService.class));

        //Getting id specified in the front end to java object to use it in this class for setting image or other
        // properties dynamically through java code
        ImageButton face = (ImageButton) findViewById(R.id.face);
        face.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(findViewById(R.id.scrollView).getVisibility() == GONE){
                    findViewById(R.id.scrollView).setVisibility(View.VISIBLE);
                    ((ImageButton) findViewById(R.id.face)).setImageResource(R.drawable.face_select);
                }else{
                    findViewById(R.id.scrollView).setVisibility(GONE);
                    ((ImageButton) findViewById(R.id.face)).setImageResource(R.drawable.face);
                }
            }
        });

        //Getting id specified in the front end to java object to use it in this class for setting image or other
        // properties dynamically through java code
        ImageButton no_filter = (ImageButton) findViewById(R.id.no_filter);
        no_filter.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background);
                typeFace = 0;
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background_select);
                gymImageView.setVisibility(View.GONE);

                if (stillSelected!=null){
                    stillSelected.setBackgroundResource(R.drawable.round_background);
                }
            }
        });
        this.noFilter = no_filter;

        //Getting id specified in the front end to java object to use it in this class for setting image or other
        // properties dynamically through java code
        ImageButton hair = (ImageButton) findViewById(R.id.hair);
        hair.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background);
                typeFace = 1;
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background_select);
                gymImageView.setVisibility(View.GONE);

                if (stillSelected!=null){
                    stillSelected.setBackgroundResource(R.drawable.round_background);
                }
            }
        });
        this.hair = hair;

        //Getting id specified in the front end to java object to use it in this class for setting image or other
        // properties dynamically through java code
        ImageButton op = (ImageButton) findViewById(R.id.op);
        op.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background);
                typeFace = 2;
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background_select);
                gymImageView.setVisibility(View.GONE);

                if (stillSelected!=null){
                    stillSelected.setBackgroundResource(R.drawable.round_background);
                }
            }
        });
        this.op = op;

        //Getting id specified in the front end to java object to use it in this class for setting image or other
        // properties dynamically through java code
        ImageButton snap = (ImageButton) findViewById(R.id.snap);
        snap.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background);
                typeFace = 3;
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background_select);
                gymImageView.setVisibility(View.GONE);

                if (stillSelected!=null){
                    stillSelected.setBackgroundResource(R.drawable.round_background);
                }
            }
        });
        this.snap = snap;

        //Getting id specified in the front end to java object to use it in this class for setting image or other
        // properties dynamically through java code
        ImageButton glasses2 = (ImageButton) findViewById(R.id.glasses2);
        glasses2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background);
                typeFace = 4;
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background_select);
                gymImageView.setVisibility(View.GONE);

                if (stillSelected!=null){
                    stillSelected.setBackgroundResource(R.drawable.round_background);
                }
            }
        });
        this.glasses2 = glasses2;

        //Getting id specified in the front end to java object to use it in this class for setting image or other
        // properties dynamically through java code
        ImageButton glasses3 = (ImageButton) findViewById(R.id.glasses3);
        glasses3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background);
                typeFace = 5;
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background_select);
                gymImageView.setVisibility(View.GONE);

                if (stillSelected!=null){
                    stillSelected.setBackgroundResource(R.drawable.round_background);
                }
            }
        });
        this.glasses3 = glasses3;

        //Getting id specified in the front end to java object to use it in this class for setting image or other
        // properties dynamically through java code
        ImageButton glasses4 = (ImageButton) findViewById(R.id.glasses4);
        glasses4.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background);
                typeFace = 6;
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background_select);
                gymImageView.setVisibility(View.GONE);

                if (stillSelected!=null){
                    stillSelected.setBackgroundResource(R.drawable.round_background);
                }
            }
        });
        this.glasses4 = glasses4;

        //Getting id specified in the front end to java object to use it in this class for setting image or other
        // properties dynamically through java code
        ImageButton glasses5 = (ImageButton) findViewById(R.id.glasses5);
        glasses5.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background);
                typeFace = 7;
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background_select);
                gymImageView.setVisibility(View.GONE);

                if (stillSelected!=null){
                    stillSelected.setBackgroundResource(R.drawable.round_background);
                }
            }
        });
        this.glasses5 = glasses5;

        //Getting id specified in the front end to java object to use it in this class for setting image or other
        // properties dynamically through java code
        ImageButton mask = findViewById(R.id.mask);
        mask.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background);
                typeFace = 8;
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background_select);
                gymImageView.setVisibility(View.GONE);

                if (stillSelected!=null){
                    stillSelected.setBackgroundResource(R.drawable.round_background);
                }
            }
        });
        this.mask = mask;

        //Getting id specified in the front end to java object to use it in this class for setting image or other
        // properties dynamically through java code
        ImageButton mask2 = (ImageButton) findViewById(R.id.mask2);
        mask2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background);
                typeFace = 9;
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background_select);
                gymImageView.setVisibility(View.GONE);

                if (stillSelected!=null){
                    stillSelected.setBackgroundResource(R.drawable.round_background);
                }
            }
        });
        this.mask2 = mask2;

        //Getting id specified in the front end to java object to use it in this class for setting image or other
        // properties dynamically through java code
        ImageButton mask3 = (ImageButton) findViewById(R.id.mask3);
        mask3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background);
                typeFace = 10;
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background_select);
                gymImageView.setVisibility(View.GONE);

                if (stillSelected!=null){
                    stillSelected.setBackgroundResource(R.drawable.round_background);
                }
            }
        });
        this.mask3 = mask3;

        //Getting id specified in the front end to java object to use it in this class for setting image or other
        // properties dynamically through java code
        ImageButton dog = (ImageButton) findViewById(R.id.dog);
        dog.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background);
                typeFace = 11;
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background_select);
                gymImageView.setVisibility(View.GONE);

                if (stillSelected!=null){
                    stillSelected.setBackgroundResource(R.drawable.round_background);
                }
            }
        });
        this.dog = dog;

        //Getting id specified in the front end to java object to use it in this class for setting image or other
        // properties dynamically through java code
        ImageButton cat2 = (ImageButton) findViewById(R.id.cat2);
        cat2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background);
                typeFace = 12;
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background_select);
                gymImageView.setVisibility(View.GONE);

                if (stillSelected!=null){
                    stillSelected.setBackgroundResource(R.drawable.round_background);
                }
            }
        });
        this.cat2 = cat2;

        final ImageButton gymnasium = (ImageButton) findViewById(R.id.still_gym);
        gymnasium.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background);
                typeFace = 0;
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background_select);
                if (gymImageView.getVisibility() == GONE) gymImageView.setVisibility(View.VISIBLE);
                gymImageView.setImageResource(R.drawable.gymnasium);
                if (stillSelected!=null){
                    stillSelected.setBackgroundResource(R.drawable.round_background);
                }
                gymnasium.setBackgroundResource(R.drawable.round_background_select);
                stillSelected = gymnasium;
            }
        });
        this.stillGym = gymnasium;

        final ImageButton gymnasiumFull = (ImageButton) findViewById(R.id.still_gym_full);
        gymnasiumFull.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background);
                typeFace = 0;
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background_select);

                if (gymImageView.getVisibility() == GONE) gymImageView.setVisibility(View.VISIBLE);
                gymImageView.setImageResource(R.drawable.gym_full);

                if (stillSelected!=null){
                    stillSelected.setBackgroundResource(R.drawable.round_background);
                }
                gymnasiumFull.setBackgroundResource(R.drawable.round_background_select);
                stillSelected = gymnasiumFull;
            }
        });
        this.stillGymFull = gymnasiumFull;

        final ImageButton gymnasiumNoTeam = (ImageButton) findViewById(R.id.still_gym_no_team);
        gymnasiumNoTeam.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background);
                typeFace = 0;
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background_select);

                if (gymImageView.getVisibility() == GONE) gymImageView.setVisibility(View.VISIBLE);
                gymImageView.setImageResource(R.drawable.gym_no_team);

                if (stillSelected!=null){
                    stillSelected.setBackgroundResource(R.drawable.round_background);
                }
                gymnasiumNoTeam.setBackgroundResource(R.drawable.round_background_select);
                stillSelected = gymnasiumNoTeam;
            }
        });
        this.stillGymNoTeam = gymnasiumNoTeam;

        final ImageButton gymnasiumRacket = (ImageButton) findViewById(R.id.still_gym_racket);
        gymnasiumRacket.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background);
                typeFace = 0;
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background_select);

                if (gymImageView.getVisibility() == GONE) gymImageView.setVisibility(View.VISIBLE);
                gymImageView.setImageResource(R.drawable.gym_racket);

                if (stillSelected!=null){
                    stillSelected.setBackgroundResource(R.drawable.round_background);
                }
                gymnasiumRacket.setBackgroundResource(R.drawable.round_background_select);
                stillSelected = gymnasiumRacket;
            }
        });
        this.stillGymRacket = gymnasiumRacket;

        final ImageButton tutiFruity = (ImageButton) findViewById(R.id.still_tuti_fruity);
        tutiFruity.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background);
                typeFace = 0;
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background_select);

                if (gymImageView.getVisibility() == GONE) gymImageView.setVisibility(View.VISIBLE);
                gymImageView.setImageResource(R.drawable.tuti_fruty_bash);

                if (stillSelected!=null){
                    stillSelected.setBackgroundResource(R.drawable.round_background);
                }
                tutiFruity.setBackgroundResource(R.drawable.round_background_select);
                stillSelected = tutiFruity;
            }
        });
        this.stillTutiFruity = tutiFruity;

        final ImageButton ncLib = (ImageButton) findViewById(R.id.still_nc_lib);
        ncLib.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background);
                typeFace = 0;
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background_select);

                if (gymImageView.getVisibility() == GONE) gymImageView.setVisibility(View.VISIBLE);
                gymImageView.setImageResource(R.drawable.nc_libriary);


                if (stillSelected!=null){
                    stillSelected.setBackgroundResource(R.drawable.round_background);
                }
                ncLib.setBackgroundResource(R.drawable.round_background_select);
                stillSelected = ncLib;
            }
        });
        this.stillNCLib = ncLib;

        final ImageButton missSaima = (ImageButton) findViewById(R.id.still_miss_saima);
        missSaima.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background);
                typeFace = 0;
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background_select);

                if (gymImageView.getVisibility() == GONE) gymImageView.setVisibility(View.VISIBLE);
                gymImageView.setImageResource(R.drawable.miss_saima);


                if (stillSelected!=null){
                    stillSelected.setBackgroundResource(R.drawable.round_background);
                }
                missSaima.setBackgroundResource(R.drawable.round_background_select);
                stillSelected = missSaima;
            }
        });
        this.stillMissSaima = missSaima;

        final ImageButton lawLib = (ImageButton) findViewById(R.id.still_law_lib);
        lawLib.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background);
                typeFace = 0;
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background_select);

                if (gymImageView.getVisibility() == GONE) gymImageView.setVisibility(View.VISIBLE);
                gymImageView.setImageResource(R.drawable.law_libriary);


                if (stillSelected!=null){
                    stillSelected.setBackgroundResource(R.drawable.round_background);
                }
                lawLib.setBackgroundResource(R.drawable.round_background_select);
                stillSelected = lawLib;
            }
        });
        this.stillLawLib = lawLib;

        final ImageButton finalsWeek = (ImageButton) findViewById(R.id.still_finals_week);
        finalsWeek.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background);
                typeFace = 0;
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background_select);

                if (gymImageView.getVisibility() == GONE) gymImageView.setVisibility(View.VISIBLE);
                gymImageView.setImageResource(R.drawable.finals_week);


                if (stillSelected!=null){
                    stillSelected.setBackgroundResource(R.drawable.round_background);
                }
                finalsWeek.setBackgroundResource(R.drawable.round_background_select);
                stillSelected = finalsWeek;
            }
        });
        this.stillFinalsWeek = finalsWeek;

        final ImageButton droplane = (ImageButton) findViewById(R.id.still_droplane);
        droplane.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background);
                typeFace = 0;
                findViewById(MASK[typeFace]).setBackgroundResource(R.drawable.round_background_select);

                if (gymImageView.getVisibility() == GONE) gymImageView.setVisibility(View.VISIBLE);
                gymImageView.setImageResource(R.drawable.droplane);

                if (stillSelected!=null){
                    stillSelected.setBackgroundResource(R.drawable.round_background);
                }
                droplane.setBackgroundResource(R.drawable.round_background_select);
                stillSelected = droplane;
            }
        });
        this.stillDroplane = droplane;

        //Getting id specified in the front end to java object to use it in this class for setting image or other
        // properties dynamically through java code
        //For camera mode changing front or back camera
        ImageButton button = (ImageButton) findViewById(R.id.change);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(FaceFilterActivity.this.detector!=null){
                    if(isFacing){
                        mCameraSource = new CameraSource.Builder(FaceFilterActivity.this, detector)
                                .setRequestedPreviewSize(640, 480)
                                .setAutoFocusEnabled(true)
                                .setFacing(CameraSource.CAMERA_FACING_FRONT)
                                .setRequestedFps(30.0f)
                                .build();
                        isFacing = false;
                    }
                    else{
                        mCameraSource = new CameraSource.Builder(FaceFilterActivity.this, detector)
                                .setRequestedPreviewSize(640, 480)
                                .setAutoFocusEnabled(true)
                                .setFacing(CameraSource.CAMERA_FACING_BACK)
                                .setRequestedFps(30.0f)
                                .build();
                        isFacing = true;
                    }
                    if (mCameraSource != null) {
                        try {
                            mPreview.stop();
                            mPreview.start(mCameraSource, mGraphicOverlay);
                        } catch (IOException e) {
                            Log.e(TAG, "Unable to start camera source.", e);
                            mCameraSource.release();
                            mCameraSource = null;
                        }
                    }
                }
            }
        });


       //Capture image button
        ImageButton camera = (ImageButton) findViewById(R.id.camera);
        camera.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                isConfirmed();
            }
        });



        // Check for the camera permission before accessing the camera.  If the
        // permission is not granted yet, request permission.
        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            createCameraSource();
        } else {
            requestCameraPermission();
        }
    }

    private boolean isConfirmed() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        //Yes button clicked
                        isConfirmed = true;
                        getSupportActionBar().hide();
                        invisibleAll();
                        takeImage();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        isConfirmed = false;
                        break;
                        default:
                            isConfirmed = false;
                            break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do you want to save this image?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();

        return isConfirmed;
    }

    private void visibleAll() {
        geofenceName.setVisibility(View.VISIBLE);
        linearLayout.setVisibility(View.VISIBLE);

        if (Build.VERSION.SDK_INT < 16) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        else {
            View decorView = getWindow().getDecorView();
            // Hide the status bar.
            int uiOptions = View.SYSTEM_UI_FLAG_VISIBLE;
            decorView.setSystemUiVisibility(uiOptions);
            // Remember that you should never show the action bar if the
            // status bar is hidden, so hide that too if necessary.
            getSupportActionBar().show();
        }
    }

    private void invisibleAll() {
        geofenceName.setVisibility(View.GONE);
        linearLayout.setVisibility(View.GONE);

        if (Build.VERSION.SDK_INT < 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        else {
            View decorView = getWindow().getDecorView();
            // Hide the status bar.
            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    private void uploadImage(File file) {
        Uri uri = Uri.fromFile(file);

        StorageReference riversRef = mStorageRef.child("uploads/"+
                mAuth.getCurrentUser().getUid()+"_"+getCurrentTimeStamp().substring(0,getCurrentTimeStamp().lastIndexOf(":"))+".jpg");
        String currentTime = getCurrentTimeStamp();
        final String imageName = currentTime.substring(0,getCurrentTimeStamp().lastIndexOf(":"))+"_jpg";

        riversRef.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get a URL to the uploaded contentk
                        Task downloadUrl = mStorageRef.getDownloadUrl();
                        myRef = database.getReference("stories/"+mAuth.getCurrentUser().getUid()+"/"+imageName);//+"/"+mAuth.getCurrentUser().getUid());
                        myRef.setValue(0);
                        Toast.makeText(FaceFilterActivity.this, "Image uploaded successfully", Toast.LENGTH_SHORT).show();
                        visibleAll();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        // ...
                        visibleAll();
                    }
                });
    }

    private void setFiltersList() {
        filtersList.add(this.noFilter);
        filtersList.add(this.hair);
        filtersList.add(this.op);
        filtersList.add(this.snap);
        filtersList.add(this.glasses2);
        filtersList.add(this.glasses3);
        filtersList.add(this.glasses4);
        filtersList.add(this.glasses5);
        filtersList.add(this.mask);
        filtersList.add(this.mask2);
        filtersList.add(this.mask3);
        filtersList.add(this.dog);
        filtersList.add(this.cat2);

        filtersList.add(this.stillGym);
        filtersList.add(this.stillGymFull);
        filtersList.add(this.stillGymNoTeam);
        filtersList.add(this.stillGymRacket);
        filtersList.add(this.stillDroplane);
        filtersList.add(this.stillFinalsWeek);
        filtersList.add(this.stillMissSaima);
        filtersList.add(this.stillLawLib);
        filtersList.add(this.stillNCLib);
        filtersList.add(this.stillTutiFruity);
    }

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
        if (!checkPermissions()) {
            requestPermissions();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        googleApiClient.disconnect();
    }

    private void takeImage() {
        try{
            mCameraSource.takePicture(null, new CameraSource.PictureCallback() {

                private File imageFile;
                @Override
                public void onPictureTaken(byte[] bytes) {
                    try {
                        // convert byte array into bitmap
                        Bitmap loadedImage = null;
                        Bitmap rotatedBitmap = null;
                        loadedImage = BitmapFactory.decodeByteArray(bytes, 0,
                                bytes.length);

                        // rotate Image
                        Matrix rotateMatrix = new Matrix();
                        rotateMatrix.postRotate(getWindowManager().getDefaultDisplay().getRotation());
                        rotatedBitmap = Bitmap.createBitmap(loadedImage, 0, 0,
                                loadedImage.getWidth(), loadedImage.getHeight(),
                                rotateMatrix, false);
                        File folder = new File(getExternalFilesDir(null),"faceFilter");
                        boolean success = true;
                        if (!folder.exists())
                            success = folder.mkdir();
                        //folder created
                        if (success) {
                            try (FileOutputStream out = new FileOutputStream(folder+"/"+getCurrentTimeStamp().substring(
                                    0,getCurrentTimeStamp().lastIndexOf(':')
                            )+".jpg")) {
                                File file = new File(folder,getCurrentTimeStamp().substring(
                                        0,getCurrentTimeStamp().lastIndexOf(':')
                                )+".jpg");
                                rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 10, out); // rotatedBitmap is your Bitmap instance
                                // PNG is a lossless format, the compression factor (100) is ignored
                                uploadImage(file);
                            } catch (IOException e) {
                                Log.d("Kainat","Error",e);
                            }
                        } else {
                            Toast.makeText(getBaseContext(), "Image folder does not exist",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        ByteArrayOutputStream ostream = new ByteArrayOutputStream();

                        // save image into gallery
                        rotatedBitmap = resize(rotatedBitmap, 800, 600);
                        rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, ostream);

                        FileOutputStream fout = new FileOutputStream(imageFile);
                        fout.write(ostream.toByteArray());
                        fout.close();
                        ContentValues values = new ContentValues();

                        values.put(MediaStore.Images.Media.DATE_TAKEN,
                                System.currentTimeMillis());
                        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                        values.put(MediaStore.MediaColumns.DATA,
                                imageFile.getAbsolutePath());

                        setResult(Activity.RESULT_OK); //add this
                        //finish();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

        }catch (Exception ex){
            Log.d("Kainat","Exception",ex);
        }

    }

    private Bitmap resize(Bitmap image, int maxWidth, int maxHeight) {
        if (maxHeight > 0 && maxWidth > 0) {
            int width = image.getWidth();
            int height = image.getHeight();
            float ratioBitmap = (float) width / (float) height;
            float ratioMax = (float) maxWidth / (float) maxHeight;

            int finalWidth = maxWidth;
            int finalHeight = maxHeight;
            if (ratioMax > 1) {
                finalWidth = (int) ((float) maxHeight * ratioBitmap);
            } else {
                finalHeight = (int) ((float) maxWidth / ratioBitmap);
            }
            image = Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true);
            return image;
        } else {
            return image;
        }
    }

    /**
     * Handles the requesting of the camera permission.  This includes
     * showing a "Snackbar" message of why the permission is needed then
     * sending the request.
     */
    private void requestCameraPermission() {
        Log.w(TAG, "Camera permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
            return;
        }

        final Activity thisActivity = this;

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions,
                        RC_HANDLE_CAMERA_PERM);
            }
        };

        Snackbar.make(mGraphicOverlay, R.string.permission_camera_rationale,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, listener)
                .show();
    }

    /**
     * Creates and starts the camera.  Note that this uses a higher resolution in comparison
     * to other detection examples to enable the barcode detector to detect small barcodes
     * at long distances.
     */
    private void createCameraSource() {

        Context context = getApplicationContext();
        FaceDetector detector = new FaceDetector.Builder(context)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .setMode(FaceDetector.ACCURATE_MODE)
                .build();

        detector.setProcessor(
                new MultiProcessor.Builder<>(new GraphicFaceTrackerFactory())
                        .build());

        //new MultiProcessor.Builder<>(new GraphicTextTrackerFactory()).build();

        if (!detector.isOperational()) {
            // Note: The first time that an app using face API is installed on a device, GMS will
            // download a native library to the device in order to do detection.  Usually this
            // completes before the app is run for the first time.  But if that download has not yet
            // completed, then the above call will not detect any faces.
            //
            // isOperational() can be used to check if the required native library is currently
            // available.  The detector will automatically become operational once the library
            // download completes on device.
            Log.w(TAG, "Face detector dependencies are not yet available.");
        }
        else this.detector = detector;

        mCameraSource = new CameraSource.Builder(context, detector)
                .setRequestedPreviewSize(640, 480)
                .setAutoFocusEnabled(true)
                .setFacing(CameraSource.CAMERA_FACING_FRONT)
                .setRequestedFps(30.0f)
                .build();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isConnectingToInternet(this)){
            android.support.v7.app.AlertDialog alertDialog = new android.support.v7.app.AlertDialog.Builder(this).create();
            alertDialog.setTitle("Wifi");
            alertDialog.setMessage("You are not connected to the internet.");
            alertDialog.setButton(android.support.v7.app.AlertDialog.BUTTON_NEUTRAL, "Open settings",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                        }
                    });
            alertDialog.show();
        }
        LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );

        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            geofenceName.setText("Turn on your location");
        }
        else geofenceName.setText("You are not inside any geofence");
        startCameraSource();
    }

    public boolean isConnectingToInternet(Context context){
        try {
            ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivity != null) {
                NetworkInfo info[] = connectivity.getAllNetworkInfo();
                if (info != null) {
                    for (int i = 0; i < info.length; i++)

                        if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                            return true;
                        }
                }
            }
        }catch (Exception e){}
        return false;

    }

    /**
     * Stops the camera.
     */
    @Override
    protected void onPause() {
        super.onPause();
        mPreview.stop();
    }

    /**
     * Releases the resources associated with the camera source, the associated detector, and the
     * rest of the processing pipeline.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCameraSource != null) {
            mCameraSource.release();
        }
    }

    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on {@link #requestPermissions(String[], int)}.
     * <p>
     * <strong>Note:</strong> It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     * </p>
     *
     * @param requestCode  The request code passed in {@link #requestPermissions(String[], int)}.
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either {@link PackageManager#PERMISSION_GRANTED}
     *                     or {@link PackageManager#PERMISSION_DENIED}. Never null.
     * @see #requestPermissions(String[], int)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // we have permission, so create the camerasource
            createCameraSource();
            return;
        }

        Log.e(TAG, "Permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Face Tracker sample")
                .setMessage(R.string.no_camera_permission)
                .setPositiveButton(R.string.ok, listener)
                .show();
    }

    //==============================================================================================
    // Camera Source Preview
    //==============================================================================================

    /**
     * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private void startCameraSource() {

        // check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
            dlg.show();
        }

        if (mCameraSource != null) {
            try {
                mPreview.start(mCameraSource, mGraphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }

    private class GraphicTextTracker extends Tracker<String> {
        private GraphicOverlay mOverlay;
        private TextGraphic mTextGraphic ;

        GraphicTextTracker(GraphicOverlay overlay) {
            mOverlay = overlay;
            mTextGraphic = new TextGraphic(overlay);
        }

        @Override
        public void onDone() {
            mOverlay.remove(mTextGraphic);
        }
    }

    //==============================================================================================
    // Graphic Face Tracker
    //==============================================================================================

    /**
     * Factory for creating a face tracker to be associated with a new face.  The multiprocessor
     * uses this factory to create face trackers as needed -- one for each individual.
     */
    private class GraphicFaceTrackerFactory implements MultiProcessor.Factory<Face> {
        @Override
        public Tracker<Face> create(Face face) {
            return new GraphicFaceTracker(mGraphicOverlay);
        }
    }

    /**
     * Face tracker for each detected individual. This maintains a face graphic within the app's
     * associated face overlay.
     */
    private class GraphicFaceTracker extends Tracker<Face> {
        private GraphicOverlay mOverlay;
        private FaceGraphic mFaceGraphic;

        GraphicFaceTracker(GraphicOverlay overlay) {
            mOverlay = overlay;
            mFaceGraphic = new FaceGraphic(overlay,typeFace,getApplicationContext());
        }

        /**
         * Start tracking the detected face instance within the face overlay.
         */
        @Override
        public void onNewItem(int faceId, Face item) {
            mFaceGraphic.setId(faceId);
        }

        /**
         * Update the position/characteristics of the face within the overlay.
         */
        @Override
        public void onUpdate(FaceDetector.Detections<Face> detectionResults, Face face) {
            mOverlay.add(mFaceGraphic);
            mFaceGraphic.updateFace(face,typeFace, getApplicationContext());
        }

        /**
         * Hide the graphic when the corresponding face was not detected.  This can happen for
         * intermediate frames temporarily (e.g., if the face was momentarily blocked from
         * view).
         */
        @Override
        public void onMissing(FaceDetector.Detections<Face> detectionResults) {
            mOverlay.remove(mFaceGraphic);
        }

        /**
         * Called when the face is assumed to be gone for good. Remove the graphic annotation from
         * the overlay.
         */
        @Override
        public void onDone() {
            mOverlay.remove(mFaceGraphic);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        lastLocation = location;
        Session.setLocation(lastLocation);
        checkGeofence();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "onConnected()");
        getLastKnownLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    // Get last known location
    private void getLastKnownLocation() {
        if (checkPermission()) {
            lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            if (lastLocation != null) {
                Log.i(TAG, "LasKnown location. " +
                        "Long: " + lastLocation.getLongitude() +
                        " | Lat: " + lastLocation.getLatitude());
                startLocationUpdates();
            } else {
                Log.w(TAG, "No location retrieved yet");
                startLocationUpdates();
            }
        } else askPermission();
    }

    private boolean checkPermission() {
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED);
    }

    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");
            ActivityCompat.requestPermissions(FaceFilterActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        } else {
            Log.i(TAG, "Requesting permission");
            ActivityCompat.requestPermissions(FaceFilterActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    // Start location Updates
    private void startLocationUpdates() {
        Log.i(TAG, "startLocationUpdates()");
        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);
        if (checkPermission())
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    // Asks for permission
    private void askPermission() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQ_PERMISSION
        );
    }



    private void createGoogleApi() {
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    private void checkGeofence() {
        if(Session.getLocation()!=null){
            for (int index = 0 ; index<Session.getGeoFences().size() ; index++){
                GeoFence geofence = Session.getGeoFences().get(index);
                //set geofence name
                if (isGeofence(geofence)){
                        hide(geofence.getId(), geofence.getName());
                }
            }
        }
    }

    private void hide(long geoFenceID, String geofenceName) {
        if(filtersList.size() == 0) setFiltersList();
        if (geoFenceID == 1L){
            for (int index = 21  ; index<filtersList.size() ; index++){
                if (filtersList!=null){
                    filtersList.get(index).setEnabled(false);
                    filtersList.get(index).setAlpha((float) 0.5);
                }
            }
            this.geofenceName.setText(geofenceName);
        }
        else if (geoFenceID == 2L){
            for (int index = 13  ; index<3 ; index++){
                if (filtersList!=null){
                    filtersList.get(index).setVisibility(GONE);
                }
            }
            this.geofenceName.setText(geofenceName);
        }
        else if (geoFenceID == 3L){
            for (int index = 13  ; index<3 ; index++){
                if (filtersList!=null){
                    filtersList.get(index).setVisibility(GONE);
                }
            }
            this.geofenceName.setText(geofenceName);
        }
        if (geoFenceID == 4L){
            for (int index = 16  ; index<filtersList.size() ; index++){
                if (filtersList!=null){
                    filtersList.get(index).setEnabled(false);
                    filtersList.get(index).setAlpha((float) 0.5);
                }
            }
            this.geofenceName.setText(geofenceName);
        }
        else if (geoFenceID == 5L){
            for (int index = 1  ; index<3 ; index++){
                if (filtersList!=null){
                    filtersList.get(index).setVisibility(GONE);
                    //Toast.makeText(this, "", Toast.LENGTH_SHORT).show();
                }
            }
            this.geofenceName.setText(geofenceName);
        }
        else if (geoFenceID == 6L){
            for (int index = 20  ; index<filtersList.size() ; index++){
                if (filtersList!=null){
                    filtersList.get(index).setEnabled(false);
                    filtersList.get(index).setAlpha((float) 0.5);
                }
            }
            this.geofenceName.setText(geofenceName);
        }
        else if (geoFenceID == 7L){
            for (int index = 19  ; index<filtersList.size() ; index++){
                if (filtersList!=null){
                    filtersList.get(index).setEnabled(false);
                    filtersList.get(index).setAlpha((float) 0.5);
                }
            }
            this.geofenceName.setText(geofenceName);
        }
        else if (geoFenceID == 8L){
            for (int index = 18  ; index<filtersList.size() ; index++){
                if (filtersList!=null){
                    filtersList.get(index).setEnabled(false);
                    filtersList.get(index).setAlpha((float) 0.5);
                }
            }
            this.geofenceName.setText(geofenceName);
        }
        else if (geoFenceID == 9L){
            for (int index = 15  ; index<filtersList.size() ; index++){
                if (filtersList!=null){
                    filtersList.get(index).setEnabled(false);
                    filtersList.get(index).setAlpha((float) 0.5);
                }
            }
            this.geofenceName.setText(geofenceName);
        }
        else if (geoFenceID == 10L){
            for (int index = 17  ; index<filtersList.size() ; index++){
                if (filtersList!=null){
                    filtersList.get(index).setEnabled(false);
                    filtersList.get(index).setAlpha((float) 0.5);
                }
            }
            this.geofenceName.setText(geofenceName);
        }
        else if (geoFenceID == 11L){
            /*for (int index = 16  ; index<filtersList.size() ; index++){
                if (filtersList!=null){
                    filtersList.get(index).setEnabled(false);
                    filtersList.get(index).setAlpha((float) 0.5);
                }
            }*/
            this.geofenceName.setText(geofenceName);
        }
    }

    private void setGeofences() {
        //new GeoFence(Long id, String name, String latitude, String longitude, String radius,
        // String expiration_duration, String isActive)

        List<GeoFence> geoFences = new ArrayList<>();
        geoFences.add(new GeoFence(1L,"My Home","33.651893099999995","73.24062409999999","10"
                ,"10","1"));
        geoFences.add(new GeoFence(2L,"Shahan University","33.6771635","73.0680579","10"
                ,"10","1"));
        geoFences.add(new GeoFence(3L,"Ten Eleven","33.7149308","73.03341879999999","10"
                ,"10","1"));
        geoFences.add(new GeoFence(4L,"Kainat's Home","33.715596","73.033452","10"
                ,"10","1"));
        geoFences.add(new GeoFence(5L,"Shahan's Office","33.6646412","72.99527359999999","10"
                ,"10","1"));
        geoFences.add(new GeoFence(6L,"XC","33.7160959","73.02822","10"
                ,"10","1"));
        geoFences.add(new GeoFence(7L,"NC/CAFE","33.7160959","73.029","10"
                ,"10","1"));
        geoFences.add(new GeoFence(8L,"OC","33.7158346","73.0294284","10"
                ,"10","1"));
        geoFences.add(new GeoFence(9L,"Drop Lane","33.7156069","73.0296512","10"
                ,"10","1"));
        geoFences.add(new GeoFence(10L,"Gym","33.7159952","73.0285035","10"
                ,"10","1"));
        //Its masjid's latlon
        geoFences.add(new GeoFence(11L,"DSP Lab","33.7157878","73.0290027","10"
                ,"10","1"));
        Session.setGeoFences(geoFences);
    }

    private boolean isGeofence(GeoFence geofence) {
        Location crntLocation = Session.getLocation();

        Location newLocation = new Location("newlocation");
        newLocation.setLatitude(Double.parseDouble(geofence.getLatitude()));
        newLocation.setLongitude(Double.parseDouble(geofence.getLongitude()));

        float distance = crntLocation.distanceTo(newLocation) / 1000;

        double geofenceRange = 0.5;
        return distance < geofenceRange;
    }

    public static String getCurrentTimeStamp(){
        try {

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String currentDateTime = dateFormat.format(new Date()); // Find todays date

            return currentDateTime;
        } catch (Exception e) {
            e.printStackTrace();

            return null;
        }
    }

    @Override
    public void onBackPressed() {
        if(mAuth.getCurrentUser()!=null){
            Intent a = new Intent(Intent.ACTION_MAIN);
            a.addCategory(Intent.CATEGORY_HOME);
            a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(a);
        }
        else startActivity(new Intent(this,LoginActivity.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.title_bar_menu, menu);
        menu.findItem(R.id.camera_btn).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.users_list_btn:
                startActivity(new Intent(this, UsersActivity.class));
                break;
            case R.id.stories_btn:
                startActivity(new Intent(this,StoriesActivity2.class));
                break;
            case R.id.logout_btn:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(this, LoginActivity.class));
                break;
        }
        return false;
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
