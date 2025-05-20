package com.example.appGrabacion.contracts;

import com.example.appGrabacion.models.FileEntry;
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
        void uploadFile(String token, android.net.Uri uri);
    }
}
