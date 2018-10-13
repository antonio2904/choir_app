package com.antony.choirapp.activites.mp3;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.antony.choirapp.database.DatabaseHelper;
import com.antony.choirapp.models.Mp3Item;
import com.antony.choirapp.mvp.Mp3Contract;
import com.antony.choirapp.utils.AppController;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Mp3Interactor implements Mp3Contract.Interactor {

    private Context mContext;
    private DatabaseHelper myDb;
    private List<Mp3Item> mp3Items = new ArrayList<>();
    private long mFileSize;
    private FileDownloadTask fileDownloadTask;

    Mp3Interactor(Context mContext) {
        this.mContext = mContext;
        myDb = new DatabaseHelper(mContext);
    }

    @Override
    public void fetchData(final DataLoadingFinishedListener dataLoadingFinishedListener) {


        if (AppController.isNetworkAvailable(mContext)) {


            final FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference ref = database.getReference("Mp3");

            // Attach a listener to read the data at our posts reference
            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                        for (DataSnapshot dsp1 : dsp.getChildren()) {
                            Mp3Item mp3Item = dsp1.getValue(Mp3Item.class);
                            mp3Items.add(mp3Item);
                            myDb.insertData(mp3Item.getmSongName(), mp3Item.getmAddedUser(), 0, "");
                        }
                    }

                    for (Mp3Item mp3Item : myDb.getAllData()) {
                        if (!mp3Items.contains(mp3Item)) {

                            if (!myDb.getPath(mp3Item.getmSongName()).equals("")) {
                                File file = new File(Uri.parse(myDb.getPath(mp3Item.getmSongName())).getPath());
                                if (file.exists()) file.delete();
                            }
                            myDb.deleteData(mp3Item.getmSongName());
                        }
                    }

                    mp3Items.clear();
                    mp3Items = myDb.getAllData();

                    dataLoadingFinishedListener.onFinished(mp3Items);

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    System.out.println("The read failed: " + databaseError.getCode());
                }
            });
            ref.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    String songName = dataSnapshot.getKey();
                    if (myDb.getPath(songName).equals("")) {
                        File file = new File(Environment.getExternalStorageDirectory(), "/MTC/" + songName + ".mp3");
                        if (file.exists()) file.delete();
                    }
                    myDb.deleteData(songName);
                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } else {

            mp3Items = myDb.getAllData();
            dataLoadingFinishedListener.onFinished(mp3Items);
        }
    }

    @Override
    public void downloadSong(final DataLoadingFinishedListener dataLoadingFinishedListener, final String songName, StorageReference storageRef, File localFile) {

        fileDownloadTask = storageRef.getFile(localFile);
        fileDownloadTask.addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                myDb.updateData(songName, 1);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
                dataLoadingFinishedListener.onFailure();
            }
        }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                double progress = (double) taskSnapshot.getBytesTransferred() / (double) taskSnapshot.getTotalByteCount() * 100;
                dataLoadingFinishedListener.onProgress((int) progress);
            }
        });
    }

    @Override
    public void onDownloadCanceled() {

        if (fileDownloadTask.isInProgress()) fileDownloadTask.cancel();
    }
}
