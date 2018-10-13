package com.antony.choirapp.activites.mp3;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
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
import android.view.MenuItem;
import android.view.View;
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
import com.google.firebase.auth.FirebaseAuth;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
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

        mBottomNavigationView = findViewById(R.id.bottomNavigationView);
        mBottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                Intent intent;

                switch (menuItem.getItemId()) {

                    case R.id.navigation_home:

                        uploadSong();
                        break;

                    case R.id.navigation_youtube:

                        intent = new Intent(Mp3Activity.this, YoutubeActivity.class);
                        startActivity(intent);
                        overridePendingTransition(0, 0);
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

    private void uploadSong() {

        
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
        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgress() {

        mMp3Recycler.setVisibility(View.VISIBLE);
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
}
