package com.antony.choirapp.mvp;

import com.antony.choirapp.models.Mp3Item;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.List;

public interface Mp3Contract {

    interface View {

        void onSuccess(String SongName, String successMsg);

        void onError(String errorMsg);

        void showProgress();

        void hideProgress();

        void onProgress(int progress);

        void hideDownloadProgress();

        void onDataLoaded(List<Mp3Item> mp3Items);
    }

    interface Presenter {

        void requestData();

        void onDestroy();

        void requestDownload(String songName);

        void onDownloadCanceled();
    }

    interface Interactor {

        void fetchData(final DataLoadingFinishedListener dataLoadingFinishedListener);

        void downloadSong(final DataLoadingFinishedListener dataLoadingFinishedListener, String songName, StorageReference storageReference, File file);

        void onDownloadCanceled();

        interface DataLoadingFinishedListener {

            void onFinished(List<Mp3Item> mp3Items);

            void onDownloadFinished();

            void onProgress(int progress);

            void onFailure();
        }
    }
}
