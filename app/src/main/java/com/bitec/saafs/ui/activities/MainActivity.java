package com.bitec.saafs.ui.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bitec.saafs.R;
import com.bitec.saafs.adapters.DrawerAdapter;
import com.bitec.saafs.interfaces.IActivityMain;
import com.bitec.saafs.models.DrawerItem;
import com.bitec.saafs.models.SimpleItem;
import com.bitec.saafs.ui.activities.account.LoginActivity;
import com.bitec.saafs.ui.activities.account.UpdateAvailable;
import com.bitec.saafs.ui.activities.friends.FriendProfile;
import com.bitec.saafs.ui.activities.notification.NotificationActivity;
import com.bitec.saafs.ui.activities.notification.NotificationImage;
import com.bitec.saafs.ui.activities.notification.NotificationImageReply;
import com.bitec.saafs.ui.activities.notification.NotificationReplyActivity;
import com.bitec.saafs.ui.activities.post.CommentsActivity;
import com.bitec.saafs.ui.activities.post.PostImage;
import com.bitec.saafs.ui.activities.post.PostText;
import com.bitec.saafs.ui.activities.post.SinglePostView;
import com.bitec.saafs.ui.fragment.About;
import com.bitec.saafs.ui.fragment.Feeds;
import com.bitec.saafs.ui.fragment.FlashMessage;
import com.bitec.saafs.ui.fragment.FriendsFragment;
import com.bitec.saafs.ui.fragment.ProfileFragment;
import com.bitec.saafs.ui.fragment.Stream;
import com.bitec.saafs.ui.fragment.Testimonials;
import com.bitec.saafs.ui.fragment.VideoUploader;
import com.bitec.saafs.utils.Config;
import com.bitec.saafs.utils.NetworkUtil;
import com.bitec.saafs.utils.database.UserHelper;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.github.javiersantos.bottomdialogs.BottomDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.tylersuehr.esr.EmptyStateRecyclerView;
import com.yarolegovich.slidingrootnav.SlidingRootNav;
import com.yarolegovich.slidingrootnav.SlidingRootNavBuilder;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity implements DrawerAdapter.OnItemSelectedListener, IActivityMain {

    private static final int    POS_FEEDS        = 0;
    private static final int    POS_STREAMS      = 1;
    private static final int    POS_TESTIMONIALS = 2;
    private static final int    POS_SEND_MESSAGE = 3;
    private static final int    POS_NETWORKS     = 4;
    private static final int    POS_VideoUpload  = 5;
    private static final int    POS_DONATE       = 6;
    private static final int    POS_ABOUT        = 7;
    private static final int    POS_LOGOUT       = 9;
    public String               userId;
    public static IActivityMain activity;
    DrawerAdapter adapter;
    View sheetView;
    private String[]          screenTitles;
    private Drawable[]        screenIcons;
    private SlidingRootNav    slidingRootNav;
    private FirebaseAuth      mAuth;
    public  FirebaseUser      currentuser;
    private FirebaseFirestore firestore;
    private UserHelper        userHelper;
    private StorageReference  storageReference;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private Intent            resultIntent;
    public  CircleImageView   imageView;
    public  TextView          username;
    private AuthCredential    credential;
    public  Fragment          mCurrentFragment;
    public  BroadcastReceiver NetworkChangeReceiver = new BroadcastReceiver()
    {
    
        @Override
        public void onReceive (Context context, Intent intent) {
            int status = NetworkUtil.getConnectivityStatusString ( context );
            Log.i ( "Network reciever", "OnReceive" );
            if ( !"android.net.conn.CONNECTIVITY_CHANGE".equals ( intent.getAction () ) ){
                if ( status != NetworkUtil.NETWORK_STATUS_NOT_CONNECTED ){

                    try {
                        performUploadTask ();
                        Snackbar.make ( findViewById ( R.id.activity_main ), "Syncing...",
                                        Snackbar.LENGTH_LONG ).show ();
                    }
                    catch (Exception e) {
                        e.printStackTrace ();
                    }
                } else {
                    try {
                        Snackbar.make ( findViewById ( R.id.activity_main ), "No Internet " +
                                                                                     "Connection...", Snackbar.LENGTH_LONG ).show ();
                    }
                    catch (Exception e) {
                        e.printStackTrace ();
                    }
                }
            }
        }

    };
    private BottomSheetDialog mBottomSheetDialog;
    public Toolbar toolbar;
    private MenuItem add_post,refresh, search;
    private boolean                mState       =true;
    private EmptyStateRecyclerView recentsRecyclerView;
    private FrameLayout            search_container;
    private boolean                mStateStream =false;

    public static void startActivity(Context context) {
        Intent intent=new Intent(context,MainActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        slidingRootNav.closeMenu(true);
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.REGISTRATION_COMPLETE));

        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.PUSH_NOTIFICATION));

        if(!TextUtils.isEmpty(getIntent().getStringExtra("openFragment"))){

            if(getIntent().getStringExtra("openFragment").equals("forLike")) {
                startActivity(new Intent(this, SinglePostView.class).putExtra("post_id", getIntent().getStringExtra("post_id")).putExtra("forComment",false));
            }else{
                startActivity(new Intent(this, SinglePostView.class).putExtra("post_id", getIntent().getStringExtra("post_id")).putExtra("forComment",true));
            }
        }

    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onStop() {
        super.onStop();
        getSharedPreferences("fcm_activity",MODE_PRIVATE).edit().putBoolean("active",false).apply();

    }

    @Override
    protected void onStart() {
        super.onStart();

        getSharedPreferences("fcm_activity",MODE_PRIVATE).edit().putBoolean("active",true).apply();

        username = findViewById(R.id.username);
        imageView = findViewById(R.id.profile_image);
        if(currentuser != null)
        {
            try {
                performUploadTask();
            }catch (Exception e){
                Log.e("Error","."+e.getLocalizedMessage());
            }

            LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                    new IntentFilter(Config.REGISTRATION_COMPLETE));

            LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                    new IntentFilter(Config.PUSH_NOTIFICATION));


        }else{
            LoginActivity.startActivityy(this);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
            if(mState){
                add_post.setVisible(true);
                refresh.setVisible(true);
                search.setVisible(false);
            }

        else {
    
                if ( !mState ){
        
                    toolbar.setTitle ( "Feeds" );
                    try {
                        getSupportActionBar ().setTitle ( "Feeds" );
                    }
                    catch (Exception e) {
                        Log.e ( "Error", e.getMessage () );
                    }
                    this.invalidateOptionsMenu ();
                    mState = true;
                    showFragment ( new Feeds () );
                    if ( slidingRootNav.isMenuOpened () ){
                        slidingRootNav.closeMenu ( true );
                    }
                    adapter.setSelected ( POS_FEEDS );
        
                } else if ( slidingRootNav.isMenuOpened () ){
                    slidingRootNav.closeMenu ( true );
                } else {
                    super.onBackPressed ();
                }
            }
        
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        activity=this;
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Feeds");
        try {
            getSupportActionBar().setTitle("Feeds");
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
        }
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/bold.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        userHelper = new UserHelper(this);
        firestore = FirebaseFirestore.getInstance();

        registerReceiver(NetworkChangeReceiver
                , new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));

        mAuth = FirebaseAuth.getInstance();
        currentuser = mAuth.getCurrentUser();

        if (currentuser == null) {

            LoginActivity.startActivityy(this);
            finish();

        } else {

            mCurrentFragment = new Feeds ();
            firebaseMessagingService();
            askPermission();

            userId = currentuser.getUid();
            storageReference = FirebaseStorage.getInstance().getReference().child("images").child(currentuser.getUid() + ".jpg");

            slidingRootNav = new SlidingRootNavBuilder(this)
                    .withToolbarMenuToggle(toolbar)
                    .withMenuOpened(false)
                    .withContentClickableWhenMenuOpened(false)
                    .withSavedState(savedInstanceState)
                    .withMenuLayout(R.layout.activity_main_drawer)
                    .inject();

            screenIcons = loadScreenIcons();
            screenTitles = loadScreenTitles();

            adapter = new DrawerAdapter(Arrays.asList(
                    createItemFor ( POS_FEEDS ).setChecked ( true ),
                    createItemFor (POS_STREAMS),
                    createItemFor (POS_TESTIMONIALS),
                    createItemFor (POS_SEND_MESSAGE),
                    createItemFor(POS_NETWORKS),
                    createItemFor ( POS_VideoUpload ),
                    createItemFor (POS_DONATE),
                    createItemFor(POS_ABOUT),
                    new SpaceItem(48),
                    createItemFor(POS_LOGOUT)));
            adapter.setListener(this);

            RecyclerView list = findViewById(R.id.list);
            list.setNestedScrollingEnabled(false);
            list.setLayoutManager(new LinearLayoutManager(this));
            list.setAdapter(adapter);

            adapter.setSelected ( POS_FEEDS );
            setUserProfile();

            mBottomSheetDialog = new BottomSheetDialog(this);
            sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_dialog, null);
            mBottomSheetDialog.setContentView(sheetView);

            LinearLayout text_post = sheetView.findViewById(R.id.text_post);
            LinearLayout photo_post = sheetView.findViewById(R.id.image_post);

            text_post.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mBottomSheetDialog.dismiss();
                    PostText.startActivity(MainActivity.this);

                }
            });

            photo_post.setOnClickListener ( view -> {
                mBottomSheetDialog.dismiss();
                PostImage.startActivity(MainActivity.this);
            } );

            if(!TextUtils.isEmpty(getIntent().getStringExtra("openFragment"))){

                if(getIntent().getStringExtra("openFragment").equals("forLike")) {
                    startActivity(new Intent(this, SinglePostView.class).putExtra("post_id", getIntent().getStringExtra("post_id")).putExtra("forComment",false));
                }else{
                    startActivity(new Intent(this, SinglePostView.class).putExtra("post_id", getIntent().getStringExtra("post_id")).putExtra("forComment",true));
                }
            }

            setRecentsView();
        }
    }

    private void setRecentsView() {

        //search_container=findViewById(R.id.search_container);
        recentsRecyclerView = findViewById(R.id.recentsView);

        recentsRecyclerView.setItemAnimator(new DefaultItemAnimator());
        recentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        recentsRecyclerView.setHasFixedSize(true);
        recentsRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
    }

    private void askPermission() {

        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                        )
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                         if(report.isAnyPermissionPermanentlyDenied()){
                            Toast.makeText(MainActivity.this, "You have denied some permissions permanently, if the app force close try granting permission from settings.", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {

                    }
                }).check();

    }

    private void setUserProfile() {

        Cursor rs = userHelper.getData(1);
        rs.moveToFirst();

        String nam = rs.getString(rs.getColumnIndex(UserHelper.CONTACTS_COLUMN_NAME));
        String imag = rs.getString(rs.getColumnIndex(UserHelper.CONTACTS_COLUMN_IMAGE));

        if (!rs.isClosed()) {
            rs.close();
        }

        username = findViewById(R.id.username);
        imageView = findViewById(R.id.profile_image);
        username.setText(nam);
        Glide.with(this)
                .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_user_art_g_2))
                .load(imag)
                .into(imageView);

    }

    public void showDialog(){

        new BottomDialog.Builder(this)
                .setTitle("Information")
                .setContent("Email has not been verified, please verify and continue.")
                .setPositiveText("Send again")
                .setPositiveBackgroundColorResource(R.color.colorAccentt)
                .setCancelable(true)
                .onPositive(new BottomDialog.ButtonCallback() {
                    @Override
                    public void onClick(@NonNull final BottomDialog dialog) {
                        mAuth.getCurrentUser().sendEmailVerification()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        dialog.dismiss();
                                        Toast.makeText(MainActivity.this, "Verification email sent", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e("Error", e.getMessage());
                                    }
                                });
                    }
                })
                .setNegativeText("Ok")
                .onNegative(new BottomDialog.ButtonCallback() {
                    @Override
                    public void onClick(@NonNull BottomDialog dialog) {
                        dialog.dismiss();
                    }
                })
                .show();

    }

    public FragmentManager GetFragmentManager()
    {
     return getSupportFragmentManager ();
    }
    
    @Override
    public void onItemSelected(int position) {

        Fragment selectedScreen;
        switch (position) {

            case POS_FEEDS:
                toolbar.setTitle("Feeds");
                try {
                    getSupportActionBar().setTitle("Feeds");
                }catch (Exception e){
                    Log.e("Error",e.getMessage());
                }
                this.invalidateOptionsMenu();
                mState=true;
                mStateStream =true;
                selectedScreen = new Feeds ();
                showFragment(selectedScreen);
                
                slidingRootNav.closeMenu(true);

                break;

            case POS_STREAMS:
                toolbar.setTitle("Streams");
                try {
                    getSupportActionBar().setTitle("Streams");
                }catch (Exception e){
                    Log.e("Error",e.getMessage());
                }
                this.invalidateOptionsMenu();
                mState=false;
                mStateStream =true;
                selectedScreen = new Stream ();
                showFragment(selectedScreen);

                slidingRootNav.closeMenu(true);

                break;
    
            case POS_TESTIMONIALS:
                toolbar.setTitle("Testimonials");
                try {
                    getSupportActionBar().setTitle("Testimonials");
                }catch (Exception e){
                    Log.e("Error",e.getMessage());
                }
                this.invalidateOptionsMenu();
                mState=false;
                mStateStream =true;
                selectedScreen = new Testimonials ();
                showFragment(selectedScreen);
        
                slidingRootNav.closeMenu(true);
        
                break;

            case POS_SEND_MESSAGE:

                if(currentuser.isEmailVerified()) {
                    toolbar.setTitle("Flash Messages");
                    try {
                        getSupportActionBar().setTitle("Flash Messages");
                    } catch (Exception e) {
                        Log.e("Error", e.getMessage());
                    }
                    this.invalidateOptionsMenu();
                    mState = false;
                    mStateStream =false;
                    selectedScreen = new FlashMessage();
                    showFragment(selectedScreen);

                    slidingRootNav.closeMenu(true);
                }else{
                    showDialog();
                }

                break;

            case POS_NETWORKS:

                if(currentuser.isEmailVerified()) {
                    toolbar.setTitle("Manage Networks");
                    try {
                        getSupportActionBar().setTitle("Manage Networks");
                    } catch (Exception e) {
                        Log.e("Error", e.getMessage());
                    }
                    this.invalidateOptionsMenu();
                    mState = false;
                    mStateStream =true;
                    selectedScreen = new FriendsFragment();
                    showFragment(selectedScreen);

                    slidingRootNav.closeMenu(true);
                }else{
                    showDialog();
                }
                break;
    
            case POS_VideoUpload:
                toolbar.setTitle("Video Uploader");
                try {
                    getSupportActionBar().setTitle("Video Uploader");
                }catch (Exception e){
                    Log.e("Error",e.getMessage());
                }
                this.invalidateOptionsMenu();
                mState=false;
                mStateStream =true;
                selectedScreen = new VideoUploader ();
                showFragment(selectedScreen);
        
                slidingRootNav.closeMenu(true);
        
                break;
                
            case POS_DONATE:
                toolbar.setTitle("Donate");
                try {
                    getSupportActionBar().setTitle("Donate");
                }catch (Exception e){
                    Log.e("Error",e.getMessage());
                }
                this.invalidateOptionsMenu();
                mState=false;
                mStateStream =true;
                //selectedScreen = new Stream ();
                //showFragment(selectedScreen);
        
                slidingRootNav.closeMenu(true);
        
                break;

            case POS_ABOUT:

                if(currentuser.isEmailVerified()) {
                    toolbar.setTitle("About");
                    try {
                        getSupportActionBar().setTitle("About");
                    } catch (Exception e) {
                        Log.e("Error", e.getMessage());
                    }
                    this.invalidateOptionsMenu();
                    mState = false;
                    mStateStream =false;
                    selectedScreen = new About();
                    showFragment(selectedScreen);

                    slidingRootNav.closeMenu(true);
                }else{
                    showDialog();
                }
                break;


            case POS_LOGOUT:

                if (currentuser != null && isOnline()) {

                    new MaterialDialog.Builder(this)
                            .title("Logout")
                            .content("Are you sure do you want to logout from this account?")
                            .positiveText("Yes")
                            .onPositive ( (dialog, which) -> {
                                logout();
                                dialog.dismiss();
                            } ).negativeText ( "No" )
                            .onNegative ( (dialog, which) -> dialog.dismiss () ).show ();

                } else {

                    new MaterialDialog.Builder(this)
                            .title("Logout")
                            .content("A technical occurred while logging you out, Check your network connection and try again.")
                            .positiveText("Done")
                            .onPositive ( (dialog, which) -> dialog.dismiss () ).show ();

                }

                break;

            default:
                selectedScreen = new Feeds ();
                showFragment(selectedScreen);

        }

        slidingRootNav.closeMenu(true);

    }


    public void logout() {
        try
        {
            performUploadTask();
        }catch (Exception e)
        {
            Log.d("MainActivity",e.getMessage());
        }
        final ProgressDialog mDialog = new ProgressDialog(this);
        mDialog.setIndeterminate(true);
        mDialog.setMessage("Logging you out...");
        mDialog.setCancelable(false);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();

        Map<String, Object> tokenRemove = new HashMap<>();
        tokenRemove.put("token_id", "");

        firestore.collection("Users").document(userId).update(tokenRemove).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                userHelper.deleteContact(1);
                mAuth.signOut();
                LoginActivity.startActivityy(MainActivity.this);
                mDialog.dismiss();
                finish();
                overridePendingTransitionExit();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("Logout Error", e.getMessage());
            }
        });


    }
    
    public static void showFragment (Fragment fragment) {
        activity.getActivity ().getSupportFragmentManager ().beginTransaction ()
                .replace ( R.id.container, fragment ).commitNow ();
        activity.setmCurrentFragment ( fragment );
    }

    private DrawerItem createItemFor(int position) {
        return new SimpleItem(screenIcons[position], screenTitles[position])
                .withIconTint(color(R.color.minimal_black))
                .withTextTint(color(R.color.minimal_black))
                .withSelectedIconTint(color(R.color.colorAccentt))
                .withSelectedTextTint(color(R.color.colorAccentt));
    }

    @NonNull
    private String[] loadScreenTitles() {
        return getResources().getStringArray(R.array.ld_activityScreenTitles);
    }

    private Drawable[] loadScreenIcons() {
        TypedArray ta = getResources().obtainTypedArray(R.array.ld_activityScreenIcons);
        Drawable[] icons = new Drawable[ta.length()];
        for (int i = 0; i < ta.length(); i++) {
            int id = ta.getResourceId(i, 0);
            if (id != 0) {
                icons[i] = ContextCompat.getDrawable(this, id);
            }
        }
        ta.recycle();
        return icons;
    }

    @ColorInt
    private int color(@ColorRes int res) {
        return ContextCompat.getColor(this, res);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransitionExit();
    }

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        overridePendingTransitionEnter();
    }

    protected void overridePendingTransitionEnter() {
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
    }

    protected void overridePendingTransitionExit() {
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }

    private void firebaseMessagingService() {

        FirebaseMessaging.getInstance().subscribeToTopic(Config.TOPIC_GLOBAL);

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i("OnBroadcastReceiver", "received");

               if (intent.getAction().equals(Config.PUSH_NOTIFICATION)) {
                    Log.i("OnBroadcastReceiver", "push_received");

                    String click_action = intent.getStringExtra("click_action");

                    switch (click_action) {
                        case "com.bitec.saafs.TARGETNOTIFICATION":

                            resultIntent = new Intent(MainActivity.this, NotificationActivity.class);

                            showAlert(resultIntent, intent);

                            break;
                        case "com.bitec.saafs.TARGETNOTIFICATIONREPLY":

                            resultIntent = new Intent(MainActivity.this, NotificationReplyActivity.class);

                            showAlert(resultIntent, intent);

                            break;
                        case "com.bitec.saafs.TARGETNOTIFICATION_IMAGE":

                            resultIntent = new Intent(MainActivity.this, NotificationImage.class);

                            showAlert(resultIntent, intent);


                            break;
                        case "com.bitec.saafs.TARGETNOTIFICATIONREPLY_IMAGE":

                            resultIntent = new Intent(MainActivity.this, NotificationImageReply.class);

                            showAlert(resultIntent, intent);

                            break;
                        case "com.bitec.saafs.TARGET_FRIENDREQUEST":

                            resultIntent = new Intent(MainActivity.this, FriendProfile.class);

                            showAlert(resultIntent, intent);


                            break;
                        case "com.bitec.saafs.TARGET_COMMENT":

                            resultIntent = new Intent(MainActivity.this, CommentsActivity.class);

                            showAlert(resultIntent,intent);

                            break;
                        case "com.bitec.saafs.TARGET_UPDATE":

                            resultIntent = new Intent(MainActivity.this, UpdateAvailable.class);

                            showAlert(resultIntent,intent);

                            break;
                        case "com.bitec.saafs.TARGET_LIKE":

                            resultIntent = new Intent(MainActivity.this, SinglePostView.class);

                            showAlert(resultIntent,intent);

                            break;
                        default:

                            resultIntent = null;
                            break;
                    }

                }
            }
        };
    }

    private void showAlert(final Intent resultIntent, Intent intent) {

        String title = intent.getStringExtra("title");
        String body = intent.getStringExtra("body");
        String name = intent.getStringExtra("name");
        String from_image = intent.getStringExtra("from_image");
        String message = intent.getStringExtra("message");
        String from_id = intent.getStringExtra("from_id");
        String notification_id = intent.getStringExtra("notification_id");
        String timestamp = intent.getStringExtra("timestamp");
        String reply_for = intent.getStringExtra("reply_for");
        String image = intent.getStringExtra("image");
        String reply_image = intent.getStringExtra("reply_image");

        String f_id = intent.getStringExtra("f_id");
        String f_name = intent.getStringExtra("f_name");
        String f_email = intent.getStringExtra("f_email");
        String f_token = intent.getStringExtra("f_token");
        String f_image = intent.getStringExtra("f_image");

        String user_id=intent.getStringExtra("user_id");
        String post_id=intent.getStringExtra("post_id");
        String post_desc=intent.getStringExtra("post_desc");
        String admin_id=intent.getStringExtra("admin_id");

        String channel=intent.getStringExtra("channel");
        String version=intent.getStringExtra("version");
        String improvements=intent.getStringExtra("improvements");
        String link=intent.getStringExtra("link");
        String question_id=intent.getStringExtra("question_id");
        String question_timestamp=intent.getStringExtra("question_timestamp");

        resultIntent.putExtra("title", title);
        resultIntent.putExtra("body", body);
        resultIntent.putExtra("name", name);
        resultIntent.putExtra("from_image", from_image);
        resultIntent.putExtra("message", message);
        resultIntent.putExtra("from_id", from_id);
        resultIntent.putExtra("notification_id", notification_id);
        resultIntent.putExtra("timestamp", timestamp);
        resultIntent.putExtra("reply_for", reply_for);
        resultIntent.putExtra("image", image);
        resultIntent.putExtra("reply_image", reply_image);

        resultIntent.putExtra("f_id", f_id);
        resultIntent.putExtra("f_name", f_name);
        resultIntent.putExtra("f_email", f_email);
        resultIntent.putExtra("f_image", f_image);
        resultIntent.putExtra("f_token", f_token);

        resultIntent.putExtra("user_id", user_id);
        resultIntent.putExtra("post_id", post_id);
        resultIntent.putExtra("post_desc", post_desc);
        resultIntent.putExtra("admin_id", admin_id);

        resultIntent.putExtra("channel", channel);
        resultIntent.putExtra("version", version);
        resultIntent.putExtra("improvements", improvements);
        resultIntent.putExtra("link", link);
        resultIntent.putExtra("question_id", question_id);
        resultIntent.putExtra("question_timestamp", question_timestamp);

        /*Alerter.create(MainActivity.this)
                .setTitle(title)
                .setText(body)
                .enableSwipeToDismiss()
                .setDuration(7000)//7sec
                .enableProgress(true)
                .enableVibration(true)
                .setBackgroundColorRes(R.color.colorAccentt)
                .setProgressColorRes(R.color.colorPrimaryy)
                .setTitleAppearance(R.style.AlertTextAppearance_Title)
                .setTextAppearance(R.style.AlertTextAppearance_Text)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(resultIntent);
                    }
                }).show();*/


    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);

        super.onPause();
    }

    public void performUploadTask() throws Exception{

        if(isOnline()){

            Cursor rc =userHelper.getData(1);
            rc.moveToFirst();

            final String nam = rc.getString(rc.getColumnIndex(UserHelper.CONTACTS_COLUMN_NAME));
            final String emai = rc.getString(rc.getColumnIndex(UserHelper.CONTACTS_COLUMN_EMAIL));
            final String imag = rc.getString(rc.getColumnIndex(UserHelper.CONTACTS_COLUMN_IMAGE));
            final String password = rc.getString(rc.getColumnIndex(UserHelper.CONTACTS_COLUMN_PASS));
            final String usernam = rc.getString(rc.getColumnIndex(UserHelper.CONTACTS_COLUMN_USERNAME));
            final String loc = rc.getString(rc.getColumnIndex(UserHelper.CONTACTS_COLUMN_LOCATION));
            final String bi = rc.getString(rc.getColumnIndex(UserHelper.CONTACTS_COLUMN_BIO));

            if(!rc.isClosed()){
                rc.close();
            }

            FirebaseFirestore.getInstance().collection("Users").document(userId).get().addOnSuccessListener(documentSnapshot -> {

                String name = documentSnapshot.getString("name");
                String image = documentSnapshot.getString("image");
                final String email = documentSnapshot.getString("email");
                String bio = documentSnapshot.getString("bio");
                String usrname = documentSnapshot.getString("username");
                String location = documentSnapshot.getString("location");

                username.setText(name);
                Glide.with(MainActivity.this)
                        .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_user_art_g_2))
                        .load(image)
                        .into(imageView);

                if ( image != null && !image.equals(imag)) {
                    storageReference.putFile(Uri.parse(imag)).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {

                            storageReference.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                                Map<String, Object> userMap = new HashMap<>();
                                userMap.put("image", downloadUri.toString());

                                FirebaseFirestore.getInstance().collection("Users").document(userId).update(userMap).addOnSuccessListener(aVoid -> {
                                    userHelper.updateContactImage(1, downloadUri.toString());
                                    Glide.with(MainActivity.this)
                                            .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_user_art_g_2))
                                            .load(downloadUri)
                                            .into(imageView);

                                });
                            });

                        }
                    });
                }

                if (bio !=  null && !bio.equals(bi)) {
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("bio", bi);

                    FirebaseFirestore.getInstance().collection("Users").document(userId).update(userMap).addOnSuccessListener(aVoid -> userHelper.updateContactBio(1, bi));
                }

                if (location != null && !location.equals(loc)) {
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("location", loc);

                    FirebaseFirestore.getInstance().collection("Users").document(userId).update(userMap).addOnSuccessListener(aVoid -> userHelper.updateContactLocation(1, loc));
                }

                if (name != null && !name.equals(nam)) {
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("name", nam);

                    FirebaseFirestore.getInstance().collection("Users").document(userId).update(userMap).addOnSuccessListener(aVoid -> {
                        userHelper.updateContactName(1, nam);
                        username.setText(nam);

                    });
                }

                if (!currentuser.getEmail().equals(emai)) {


                    credential = EmailAuthProvider
                            .getCredential(currentuser.getEmail(), password);

                    currentuser.reauthenticate(credential)
                            .addOnCompleteListener(task -> currentuser.updateEmail(emai).addOnCompleteListener(task1 -> {

                                if (task1.isSuccessful()) {

                                    if (!email.equals(emai)) {
                                        Map<String, Object> userMap = new HashMap<>();
                                        userMap.put("email", emai);

                                        FirebaseFirestore.getInstance().collection("Users").document(userId).update(userMap).addOnSuccessListener(aVoid -> userHelper.updateContactEmail(1, emai));
                                    }

                                } else {

                                    Log.e("Update email error", task1.getException().getMessage() + "..");

                                }

                            }));
                }
            });

        }
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(NetworkChangeReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onDestroy();

    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public void onViewProfileClicked(View view) {

        toolbar.setTitle("My Profile");
        try {
            getSupportActionBar().setTitle("My Profile");
        }catch (Exception e){
            Log.e("Error",e.getMessage());
        }
        this.invalidateOptionsMenu();
        mState=false;
        showFragment(new ProfileFragment());
        slidingRootNav.closeMenu(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_posts, menu);
        add_post=menu.findItem(R.id.action_new);
        refresh=menu.findItem(R.id.action_refresh);
        search=menu.findItem(R.id.action_search);
        
        if(mState){
            add_post.setVisible(true);
            refresh.setVisible(true);
            search.setVisible(false);
        }else{
            add_post.setVisible(false);
            refresh.setVisible(false);
            search.setVisible(false);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_new:
                if(currentuser.isEmailVerified()) {
                    mBottomSheetDialog.show();
                }else{
                    showDialog();
                }
                return true;

            case R.id.action_refresh:
                showFragment (new Feeds () );
                return true;
            case R.id.action_search:
                showFragment (new Feeds () );
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    @Override
    public Toolbar getToolbar () {
        return toolbar;
    }
    
    @Override
    public TextView getUsername () {
        return username;
    }
    
    @Override
    public MainActivity getActivity () {
        return this;
    }
    
    @Override
    public FirebaseUser getCurrentuser () {
        return currentuser;
    }
    
    @Override
    public Fragment getmCurrentFragment () {
        return mCurrentFragment;
    }
    
    @Override
    public String getUserId () {
        return userId;
    }
    
    @Override
    public CircleImageView getImageView () {
        return imageView;
    }
    
    @Override
    public void setmCurrentFragment (Fragment fragment) {
        mCurrentFragment = fragment;
    }
}
