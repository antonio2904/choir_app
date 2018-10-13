package com.antony.choirapp.activites.splash;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.antony.choirapp.R;
import com.antony.choirapp.activites.mp3.Mp3Activity;
import com.antony.choirapp.utils.AppController;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;

import java.io.File;

public class SplashActivity extends AppCompatActivity {

    private int mVersionCode = 0;
    private int latest_verion_code = 0;
    private boolean is_under_maintenance = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);


        checkForPermission();
    }

    private void checkForPermission() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1);
        } else {

            initUi();
        }
    }

    private void initUi() {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                File apk = new File(Environment.getExternalStorageDirectory(), "/MTC_CHOIR/mtc_choir.apk");
                try {
                    if (apk.exists()) apk.delete();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                update();

            }
        }, 2000);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    initUi();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    finish();
                }
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    public void navigateToHome() {

        Intent intent = new Intent(SplashActivity.this, Mp3Activity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.right, R.anim.left);
        SplashActivity.this.finish();
    }

    private void redirectStore(String updateUrl) {
//        final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(updateUrl));
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(intent);
        final File file = new File(Environment.getExternalStorageDirectory(), "MTC_CHOIR");
        if (!file.exists()) {
            file.mkdir();
        }
        final File file1 = new File(Environment.getExternalStorageDirectory(), "/MTC_CHOIR/mtc_choir.apk");
        StorageReference storageRef = FirebaseStorage.getInstance().getReference("mtc_choir.apk");
        final ProgressDialog mProgressDialog = new ProgressDialog(SplashActivity.this);
        mProgressDialog.setTitle("Downloading");
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
        storageRef.getFile(file1).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                mProgressDialog.dismiss();
                StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                StrictMode.setVmPolicy(builder.build());
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(file1), "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
                finish();
                startActivity(intent);
            }
        }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                double progress = (double) taskSnapshot.getBytesTransferred() / (double) taskSnapshot.getTotalByteCount() * 100;
                mProgressDialog.setProgress((int) progress);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                mProgressDialog.dismiss();
                Toast.makeText(SplashActivity.this, "Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void update() {
        final FirebaseRemoteConfig mRemoteConfig = FirebaseRemoteConfig.getInstance();
        mRemoteConfig.fetch(0);
        mRemoteConfig.activateFetched();

        boolean is_update_required = mRemoteConfig.getBoolean("is_update_required");
        is_under_maintenance = mRemoteConfig.getBoolean("is_under_maintenance");
        if (!mRemoteConfig.getString("latest_version").equals("")) {
            latest_verion_code = Integer.valueOf(mRemoteConfig.getString("latest_version"));
        }
        try {
            PackageInfo pInfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
            mVersionCode = pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }


        if (is_update_required && AppController.isNetworkAvailable(SplashActivity.this) && mVersionCode < latest_verion_code) {
            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(SplashActivity.this);
            alertDialog.setTitle("Update Available");
            alertDialog.setMessage("Update to new available version!!");
            alertDialog.setCancelable(false);

            alertDialog.setPositiveButton("UPDATE",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            String updateUrl = mRemoteConfig.getString("update_url");
                            redirectStore(updateUrl);
                        }
                    });

            alertDialog.setNegativeButton("SKIP",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            navigateToHome();
                        }
                    });

            alertDialog.show();
        } else {
            navigateToHome();
        }
    }
}
