package com.antony.choirapp.activites.mp3;

import android.os.Environment;

import com.antony.choirapp.models.Mp3Item;
import com.antony.choirapp.mvp.Mp3Contract;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.List;

public class Mp3Presenter implements Mp3Contract.Presenter, Mp3Contract.Interactor.DataLoadingFinishedListener {

    private Mp3Contract.View view;
    private Mp3Contract.Interactor interactor;
    private long mFileSize;
    private String mSongName;

    Mp3Presenter(Mp3Contract.View view, Mp3Contract.Interactor interactor) {

        this.view = view;
        this.interactor = interactor;
    }

    @Override
    public void requestData() {

        view.showProgress();
        interactor.fetchData(this);
    }

    @Override
    public void onDestroy() {

        this.view = null;
        this.interactor = null;
    }

    @Override
    public void requestDownload(String songName) {

        StorageReference storageRef = FirebaseStorage.getInstance().getReference("Mp3/" + songName);

        File localFile1 = new File(Environment.getExternalStorageDirectory(), "MTC_CHOIR");
        if (!localFile1.exists()) {
            localFile1.mkdir();
        }

        long mFreeSpace = localFile1.getFreeSpace();

        storageRef.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
            @Override
            public void onSuccess(StorageMetadata storageMetadata) {

                mFileSize = storageMetadata.getSizeBytes();
            }
        });

        if (mFreeSpace > mFileSize) {

            File localFile = new File(Environment.getExternalStorageDirectory(), "/MTC_CHOIR/" + songName + ".mp3");

            if (!localFile.exists()) {

                this.mSongName = songName;

                interactor.downloadSong(this, songName,storageRef,localFile);
            }
            else{

                view.onSuccess(songName,"downloaded");

            }
        }
    }

    @Override
    public void onDownloadCanceled() {

        interactor.onDownloadCanceled();
    }

    @Override
    public void onFinished(List<Mp3Item> mp3Items) {

        view.onDataLoaded(mp3Items);
        view.hideProgress();
    }

    @Override
    public void onDownloadFinished() {


    }

    @Override
    public void onProgress(int progress) {

        view.onProgress(progress);
        if(progress == 100){

            view.onSuccess(mSongName,"downloaded");
        }
    }

    @Override
    public void onFailure() {

    }
}
