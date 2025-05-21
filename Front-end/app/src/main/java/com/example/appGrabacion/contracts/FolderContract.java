package com.example.appGrabacion.contracts;

import android.net.Uri;
import com.example.appGrabacion.entities.FileEntry;
import com.example.appGrabacion.models.FolderModel;

import java.util.List;

public interface FolderContract {
    interface View {
        void showLoading();
        void hideLoading();
        void showFiles(List<FileEntry> files);
        void showUploadSuccess();
        void showError(String message);
    }

    interface Presenter {
        void attachView(View view);
        void detachView();
        void loadFiles(String token);
        void uploadFile(String token, Uri uri);
    }

    interface Service {
        void fetchFiles(String token, FolderModel.FilesCallback callback);
        void uploadFile(String token, Uri uri, FolderModel.UploadCallback callback);
    }
}
