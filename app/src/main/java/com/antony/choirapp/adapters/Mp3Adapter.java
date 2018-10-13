package com.antony.choirapp.adapters;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.antony.choirapp.R;
import com.antony.choirapp.database.DatabaseHelper;
import com.antony.choirapp.models.Mp3Item;
import com.antony.choirapp.utils.AppController;

import java.util.List;

public class Mp3Adapter extends RecyclerView.Adapter<Mp3Adapter.MyViewHolder> {

    private List<Mp3Item> mp3ItemList;
    private DatabaseHelper myDb;
    private String songName;
    private String addedBy;
    private boolean isDownloaded = false;
    private boolean isPlaying = false;
    private ItemClickListener mListener;

    public Mp3Adapter(List<Mp3Item> mp3ItemList) {

        this.mp3ItemList = mp3ItemList;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView mSongNameTextView;
        private TextView mAddedByTextView;
        private TextView mSongStartTextView;
        private TextView mSongEndTextView;
        private ImageView mPlay;
        private ImageView mShare;
        private ImageView mDelete;
        private SeekBar mSeekBar;

        MyViewHolder(@NonNull final View itemView) {
            super(itemView);
            mSongNameTextView = itemView.findViewById(R.id.text_songName);
            mAddedByTextView = itemView.findViewById(R.id.text_addedBy);
            mSongStartTextView = itemView.findViewById(R.id.text_songStart);
            mSongEndTextView = itemView.findViewById(R.id.text_songEnd);
            mSeekBar = itemView.findViewById(R.id.seekBar);
            mPlay = itemView.findViewById(R.id.image_play);
            mShare = itemView.findViewById(R.id.image_share);
            mDelete = itemView.findViewById(R.id.image_delete);

            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

            mDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    mListener.onDelete(mp3ItemList.get(getAdapterPosition()).getmSongName());

                }
            });

            mShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    mListener.onShareAudio(mp3ItemList.get(getAdapterPosition()).getmSongName());
                }
            });

            mPlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (!AppController.isPlaying || AppController.nowPlayingPosition == getAdapterPosition()) {

                        songName = mp3ItemList.get(getAdapterPosition()).getmSongName();

                        if (myDb.isDownloaded(songName) == 0) {

                            mListener.onDownload(songName, getAdapterPosition());

                        } else if (!AppController.isPlaying) {

                            mPlay.setImageDrawable(itemView.getContext().getResources().getDrawable(R.drawable.ic_pause));
                            mListener.onPlay(mSongStartTextView, mSongEndTextView, mPlay, mSeekBar, mp3ItemList.get(getAdapterPosition()).getmSongName(), getAdapterPosition());
                        } else {

                            mPlay.setImageDrawable(itemView.getContext().getResources().getDrawable(R.drawable.ic_play));
                            mListener.onPauseClicked();
                        }
                    } else if (AppController.nowPlayingPosition != getAdapterPosition()) {

                        Toast.makeText(itemView.getContext(), "Stop the player to change the song", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                    if (fromUser && getAdapterPosition() == AppController.nowPlayingPosition) {
                        mListener.onSeek(progress, seekBar);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
        }
    }

    @NonNull
    @Override
    public Mp3Adapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_mp3, viewGroup, false);
        myDb = new DatabaseHelper(viewGroup.getContext());
        mListener = (ItemClickListener) viewGroup.getContext();
        return new MyViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull Mp3Adapter.MyViewHolder myViewHolder, int i) {

        songName = mp3ItemList.get(i).getmSongName();
        addedBy = mp3ItemList.get(i).getmAddedUser();
        isDownloaded = myDb.isDownloaded(songName) == 1;

        myViewHolder.mSongNameTextView.setText(String.valueOf(i + 1) + ". " + songName);
        myViewHolder.mAddedByTextView.setText("Added By: " + addedBy);
        myViewHolder.mSongStartTextView.setText("00:00");
        myViewHolder.mSongEndTextView.setText("00:00");

        if (mp3ItemList.get(i).getmAddedUser().equals(AppController.mUserName))
            myViewHolder.mDelete.setVisibility(View.VISIBLE);

        if (!isDownloaded) {
            myViewHolder.mPlay.setImageResource(R.drawable.ic_download);
            myViewHolder.mSeekBar.setVisibility(View.GONE);
        } else myViewHolder.mPlay.setImageResource(R.drawable.ic_play);


    }

    @Override
    public int getItemCount() {
        return mp3ItemList.size();
    }

    public interface ItemClickListener {

        void onDownload(String songName, int position);

        void onPlay(TextView start, TextView end, ImageView play, SeekBar seekBar, String songName, int position);

        void onSeek(int progress, SeekBar seekBar);

        void onPauseClicked();

        void onDelete(String songName);

        void onShareAudio(String songName);
    }
}
