package com.antony.choirapp.activites.mp3;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.widget.CircularProgressDrawable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.antony.choirapp.R;
import com.antony.choirapp.activites.youtube.YoutubeActivity;
import com.antony.choirapp.adapters.Mp3Adapter;
import com.antony.choirapp.database.DatabaseHelper;
import com.antony.choirapp.models.Mp3Item;
import com.antony.choirapp.mvp.Mp3Contract;
import com.antony.choirapp.utils.AppController;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class Mp3Activity extends AppCompatActivity implements Mp3Contract.View, Mp3Adapter.ItemClickListener {

    private RecyclerView mMp3Recycler;
    private Mp3Adapter mp3Adapter;
    private List<Mp3Item> mList = new ArrayList<>();
    private List<Mp3Item> mQueriedResultList = new ArrayList<>();
    private Mp3Presenter mPresenter;
    private DatabaseHelper myDb;
    private int mPosition;
    private MediaPlayer mMediaPlayer;
    private Handler mHandler;
    private Runnable mRunnable;
    private SharedPreferences sharedPref;
    private ProgressDialog mProgressDialog;
    private BottomNavigationView mBottomNavigationView;
    private Button mUploadButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mp3);
        initUi();
        mPresenter = new Mp3Presenter(this, new Mp3Interactor(this));
        mPresenter.requestData();
    }

    private void initUi() {

        checkUserCreated();

        if (FirebaseAuth.getInstance().getCurrentUser() == null)
            FirebaseAuth.getInstance().signInAnonymously();

        mMp3Recycler = findViewById(R.id.recycler_mp3);
        LinearLayoutManager llm = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mMp3Recycler.setLayoutManager(llm);
        mMp3Recycler.setItemAnimator(new DefaultItemAnimator());
        mp3Adapter = new Mp3Adapter(mList);
        mMp3Recycler.setAdapter(mp3Adapter);
        EditText mSearchEditText = findViewById(R.id.et_search);
        myDb = new DatabaseHelper(this);
        mUploadButton = findViewById(R.id.button_upload);

        mUploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onAudioUpload();
            }
        });

        mBottomNavigationView = findViewById(R.id.bottomNavigationView);
        mBottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                Intent intent;

                switch (menuItem.getItemId()) {

                    case R.id.navigation_home:


                        break;

                    case R.id.navigation_youtube:

//                        intent = new Intent(Mp3Activity.this, YoutubeActivity.class);
//                        startActivity(intent);
//                        overridePendingTransition(0, 0);
                        break;

                    case R.id.navigation_karaoke:

                        break;
                }
                return true;
            }
        });

        mSearchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                mQueriedResultList.clear();

                for (Mp3Item mp3Item : mList)

                    if (Pattern.compile(Pattern.quote(s.toString()), Pattern.CASE_INSENSITIVE).matcher(mp3Item.getmSongName()).find())
                        mQueriedResultList.add(mp3Item);

                mp3Adapter = new Mp3Adapter(mQueriedResultList);
                mMp3Recycler.setAdapter(mp3Adapter);
            }
        });
    }

    private void checkUserCreated() {

        sharedPref = this.getSharedPreferences(
                "mtc_choir", Context.MODE_PRIVATE);
        if (!sharedPref.getString("user", "").isEmpty()) {

            AppController.mUserName = sharedPref.getString("user", "");
        } else {
            showDialog();
        }
    }

    private void showDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter your Name");
        View view = getLayoutInflater().inflate(R.layout.layout_user_dialog, null);
        final EditText et = view.findViewById(R.id.et_username);
        builder.setView(view);
        builder.setPositiveButton("Ok", null);
        builder.setCancelable(false);
        final AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {

                ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (!et.getText().toString().isEmpty()) {
                            AppController.mUserName = et.getText().toString();
                            sharedPref.edit().putString("user", AppController.mUserName).apply();
                            FirebaseDatabase db = FirebaseDatabase.getInstance();
                            db.getReference().child("Users").child(et.getText().toString()).setValue(et.getText().toString());
                            dialog.dismiss();
                        }
                    }
                });
            }
        });

        dialog.show();
    }

    @Override
    public void onSuccess(String songName, String successMsg) {

        if (successMsg.equals("downloaded")) {

            myDb.updateData(songName, 1);
            mp3Adapter.notifyItemChanged(mPosition);
        }
    }

    @Override
    public void onError(String errorMsg) {

    }

    @Override
    public void showProgress() {

        mMp3Recycler.setVisibility(View.GONE);
        mUploadButton.setVisibility(View.GONE);
        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgress() {

        mMp3Recycler.setVisibility(View.VISIBLE);
        mUploadButton.setVisibility(View.VISIBLE);
        findViewById(R.id.progressBar).setVisibility(View.GONE);
    }

    @Override
    public void onProgress(int progress) {

        if (mProgressDialog == null) {

            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setTitle("Downloading");
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.setButton(ProgressDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    mPresenter.onDownloadCanceled();
                }
            });
            mProgressDialog.show();
        } else if (mProgressDialog.isShowing()) {

            mProgressDialog.setProgress(progress);
        }

        if (progress == 100) mProgressDialog.dismiss();
    }

    @Override
    public void hideDownloadProgress() {

    }

    @Override
    public void onDataLoaded(List<Mp3Item> mp3Items) {

        mList.clear();
        mList.addAll(mp3Items);
        mp3Adapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
        mPresenter.onDestroy();
    }

    @Override
    public void onDownload(String songName, int position) {

        mPosition = position;
        mPresenter.requestDownload(songName);
    }

    @Override
    public void onPlay(final TextView start, final TextView end, final ImageView play, final SeekBar seekBar, String songName, int position) {

        if (position != AppController.nowPlayingPosition) {

            mMediaPlayer = null;
            seekBar.setProgress(0);
        }

        if (mMediaPlayer == null) {

            Uri uri;

            if (!myDb.getPath(songName).equals("")) {
                File file = new File("storage/emulated/0/" + myDb.getPath(songName));
                if (file.exists()) {
                    uri = Uri.parse("storage/emulated/0/" + myDb.getPath(songName));
                } else if (new File("storage/emulated/1/" + myDb.getPath(songName)).exists()) {
                    uri = Uri.parse("storage/emulated/1/" + myDb.getPath(songName));
                } else {
                    uri = Uri.parse("");
                }
            } else {
                uri = Uri.parse(new File(Environment.getExternalStorageDirectory() + "/MTC_CHOIR/" + songName + ".mp3").toString());
            }
            mMediaPlayer = MediaPlayer.create(Mp3Activity.this, uri);

        }
        mHandler = new Handler();
        mRunnable = new Runnable() {
            @Override
            public void run() {
                if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {

                    seekBar.setProgress(mMediaPlayer.getCurrentPosition());
                    start.setText(AppController.formatMilliseconds(mMediaPlayer.getCurrentPosition()));

                }
                mHandler.postDelayed(this, 1000);

            }
        };
        try {

            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    seekBar.setVisibility(View.GONE);
                    end.setVisibility(View.GONE);
                    start.setText("00:00");
                    AppController.isPlaying = false;
                    play.setImageDrawable(getResources().getDrawable(R.drawable.ic_play));
                    if (mMediaPlayer != null) mMediaPlayer.release();
                    mMediaPlayer = null;
                    mHandler.removeCallbacks(mRunnable);
                    mRunnable = null;
                    mHandler = null;
                }
            });

            end.setVisibility(View.VISIBLE);
            seekBar.setVisibility(View.VISIBLE);
            seekBar.setMax(mMediaPlayer.getDuration());
            end.setText(AppController.formatMilliseconds(mMediaPlayer.getDuration()));

            mMediaPlayer.start();
            AppController.isPlaying = true;
            AppController.nowPlayingPosition = position;
            Mp3Activity.this.runOnUiThread(mRunnable);
        } catch (Exception e) {
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onSeek(int progress, SeekBar seekBar) {

        if (mMediaPlayer != null) {
            mMediaPlayer.seekTo(progress);
        }
    }

    @Override
    public void onPauseClicked() {

        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            AppController.isPlaying = false;
            mHandler.removeCallbacks(mRunnable);
        }
    }

    @Override
    public void onDelete(final String songName) {


        if (AppController.isNetworkAvailable(Mp3Activity.this)) {

            if (!AppController.isPlaying) {

                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(Mp3Activity.this);
                alertDialog.setMessage("Do you want to delete " + songName + " ?");
                alertDialog.setCancelable(false);

                alertDialog.setPositiveButton("DELETE",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                delete(songName);

                            }
                        });

                alertDialog.setNegativeButton("CANCEL",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                alertDialog.show();
            } else {
                Toast.makeText(this, "Stop the player to delete", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(Mp3Activity.this, "Please check your internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    public void delete(String songName) {

        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("Mp3");
        myRef.child(songName).setValue(null);

        mProgressDialog = new ProgressDialog(Mp3Activity.this);

        mProgressDialog.setTitle("Deleting");
        mProgressDialog.setMessage("Please wait");
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

        StorageReference storageReference = FirebaseStorage.getInstance().getReference("Mp3/" + songName);
        storageReference.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                mProgressDialog.dismiss();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                mProgressDialog.dismiss();
                Toast.makeText(Mp3Activity.this, "Failed", Toast.LENGTH_SHORT).show();
            }
        });

        File file = new File(Environment.getExternalStorageDirectory() + "/MTC_CHOIR/" + songName + ".mp3");
        if (file.exists()) file.delete();
    }

    boolean isdoublepressed = false;

    @Override
    public void onBackPressed() {

        if (isdoublepressed) {

            if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
                mMediaPlayer.reset();
                AppController.isPlaying = false;
            }
            super.onBackPressed();
            return;
        }

        this.isdoublepressed = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                isdoublepressed = false;
            }
        }, 2000);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mBottomNavigationView != null)
            mBottomNavigationView.setSelectedItemId(R.id.navigation_home);
    }

    public void onAudioUpload() {

        if (!AppController.isPlaying) {
            if (mHandler != null) mHandler.removeCallbacks(mRunnable);
            Intent intent;
            intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("audio/mpeg");
            startActivityForResult(intent, 1);
        } else {
            Toast.makeText(this, "Stop the player to upload", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            if ((data != null) && (data.getData() != null)) {

                upload(data.getData());
            }
        }
    }

    public void upload(final Uri data) {

        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(Mp3Activity.this);
        alertDialog.setTitle("Enter Details");
        alertDialog.setCancelable(false);

        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.layout_upload_youtube, null);
        dialogView.findViewById(R.id.et_link_youtube).setVisibility(View.GONE);
        alertDialog.setView(dialogView);

        alertDialog.setPositiveButton("UPLOAD",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        EditText name_et = dialogView.findViewById(R.id.et_name_youtube);
                        AppController.hideKeyboardFrom(Mp3Activity.this, dialogView);
                        mProgressDialog = new ProgressDialog(Mp3Activity.this);
                        mProgressDialog.setCancelable(false);
                        mProgressDialog.setTitle("Uploading");
                        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                        mProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new Message());
                        mProgressDialog.show();
                        final String name = name_et.getText().toString();
                        if (!name.equals("")) {

                            // Now you can use that Uri to get the file path, or upload it, ...
                            FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
                            final StorageReference myRef = firebaseStorage.getReference("Mp3/" + name);

                            try {
                                InputStream iStream = getContentResolver().openInputStream(data);
                                byte[] inputData = getBytes(iStream);


                                final UploadTask uploadTask = myRef.putBytes(inputData);
                                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Mp3");
                                        Map<String, Mp3Item> mp3ItemMap = new HashMap<>();
                                        Mp3Item mp3Item = new Mp3Item();
                                        mp3Item.setmAddedUser(AppController.mUserName);
                                        mp3Item.setmSongName(name);
                                        mp3ItemMap.put(name, mp3Item);
                                        databaseReference.child(name).setValue(mp3ItemMap);
                                        File file = new File(data.getPath());//create path from uri
                                        final String[] split = file.getPath().split(":");//split the path.
                                        String filePath;
                                        if (split.length > 1)
                                            filePath = split[1];//assign it to a string(your choice).
                                        else filePath = split[0];

                                        myDb.insertData(name, AppController.mUserName, 1, filePath);

                                        mProgressDialog.dismiss();
                                        mProgressDialog = null;
                                    }
                                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                        double progress = (double) taskSnapshot.getBytesTransferred() / (double) taskSnapshot.getTotalByteCount() * 100;
                                        mProgressDialog.setProgress((int) progress);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        mProgressDialog.dismiss();
                                        Toast.makeText(Mp3Activity.this, "Failed", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnCanceledListener(new OnCanceledListener() {
                                    @Override
                                    public void onCanceled() {
                                        mProgressDialog.dismiss();
                                    }
                                });
                                mProgressDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        uploadTask.cancel();
                                        Toast.makeText(Mp3Activity.this, "Upload Cancelled", Toast.LENGTH_SHORT).show();
                                        mProgressDialog.dismiss();
                                    }
                                });

                            } catch (Exception ignored) {
                            }
                        }
                    }
                });

        alertDialog.setNegativeButton("CANCEL",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        alertDialog.show();
    }

    public byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }
}
