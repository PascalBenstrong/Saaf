package com.bitec.saafs.ui.activities.notification;

import android.app.DownloadManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bitec.saafs.R;
import com.bitec.saafs.utils.Config;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.github.chrisbanes.photoview.PhotoView;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.DialogOnDeniedPermissionListener;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.ArrayList;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ImagePreviewSave extends AppCompatActivity {

    String intent_URI,intent_URL;
    ArrayList<Long> list = new ArrayList<>();
    private PhotoView photoView;
    private long refid;
    private String sender_name;
    public BroadcastReceiver onComplete = new BroadcastReceiver() {

        public void onReceive(Context ctxt, Intent intent) {

            long referenceId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            list.remove(referenceId);
            if (list.isEmpty()) {
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    setupChannels(notificationManager);
                }
                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                        ImagePreviewSave.this, Config.ADMIN_CHANNEL_ID);

                android.app.Notification notification;
                notification = mBuilder
                        .setAutoCancel(true)
                        .setContentTitle("Download success")
                        .setColorized(true)
                        .setSound(Uri.parse("android.resource://"+getPackageName()+"/"+R.raw.hify_sound))
                        .setColor(Color.parseColor("#2591FC"))
                        .setSmallIcon(R.drawable.ic_file_download_accent_24dp)
                        .setContentText("Image saved in /Downloads/Saaf/" + sender_name)
                        .build();

                notificationManager.notify(0, notification);
                Toast.makeText(ctxt, "Image saved in /Downloads/Saaf/" + sender_name, Toast.LENGTH_LONG).show();
            }
        }

    };
    private DownloadManager downloadManager;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(onComplete);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
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

    /**
     * Overrides the pending Activity transition by performing the "Enter" animation.
     */
    protected void overridePendingTransitionEnter() {
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
    }

    /**
     * Overrides the pending Activity transition by performing the "Exit" animation.
     */
    protected void overridePendingTransitionExit() {
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_preview_save);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/bold.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        intent_URI=getIntent().getStringExtra("uri");
        intent_URL=getIntent().getStringExtra("url");
        sender_name=getIntent().getStringExtra("sender_name");

        registerReceiver(onComplete,
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

        photoView = findViewById(R.id.photo_view);

        if(!TextUtils.isEmpty(intent_URI)) {
            photoView.setImageURI(Uri.parse(intent_URI));


        }else {

            Glide.with(this)
                    .setDefaultRequestOptions(new RequestOptions().placeholder(getResources().getDrawable(R.drawable.placeholder2)))
                    .load(intent_URL)
                    .into(photoView);


        }

    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    private void downloadImage(final String ImageURI) {

       new MaterialDialog.Builder(this)
                .title("Save Image")
                .content("Do you want to save this image?")
                .positiveText("YES")
                .negativeText("NO")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(ImageURI));
                        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
                        request.setAllowedOverRoaming(true);
                        request.setTitle("Saaf");
                        request.setDescription("Downloading image...");
                        request.setVisibleInDownloadsUi(true);
                        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "/Saaf Images/"+sender_name  + "/SAAF_" +  System.currentTimeMillis() + ".jpeg");

                        refid = downloadManager.enqueue(request);
                        list.add(refid);


                    }
                }).onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                }).show();

    }

    public void saveImage(View view) {

        Dexter.withActivity(this)
                .withPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        if(isOnline()) {
                            if (!TextUtils.isEmpty(intent_URI)) {
                                downloadImage(intent_URI);
                            } else {
                                downloadImage(intent_URL);
                            }
                        }else{
                            Toast.makeText(ImagePreviewSave.this, "No internet connection", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        if(response.isPermanentlyDenied()){
                                    DialogOnDeniedPermissionListener.Builder
                                            .withContext(ImagePreviewSave.this)
                                            .withTitle("Storage permission")
                                            .withMessage("Storage permission is needed for downloading images.")
                                            .withButtonText(android.R.string.ok)
                                            .withIcon(R.mipmap.logo)
                                            .build();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                    }
                }).check();

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupChannels(NotificationManager notificationManager) {
        CharSequence adminChannelName = "Downloads";
        String adminChannelDescription = "Used to show the progress of downloads";
        NotificationChannel adminChannel;
        adminChannel = new NotificationChannel(Config.ADMIN_CHANNEL_ID, adminChannelName, NotificationManager.IMPORTANCE_DEFAULT);
        adminChannel.setDescription(adminChannelDescription);
        adminChannel.enableVibration(true);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(adminChannel);
        }
    }

}
