package com.example.appGrabacion.presenters;

import android.net.Uri;
import com.example.appGrabacion.contracts.FolderContract;
import com.example.appGrabacion.entities.FileEntry;
import com.example.appGrabacion.models.FolderModel;

import java.util.List;

public class FolderPresenter implements FolderContract.Presenter {

    private FolderContract.View view;
    private final FolderModel service;

    public FolderPresenter(FolderModel service) {
        this.service = service;
    }

    @Override
    public void attachView(FolderContract.View view) {
        this.view = view;
    }

    @Override
    public void detachView() {
        this.view = null;
    }

    @Override
    public void loadFiles(String token) {
        if (view != null) {
            view.showLoading();
            service.fetchFiles(token, new FolderModel.FilesCallback() {
                @Override
                public void onSuccess(List<FileEntry> fetched) {
                    if (view != null) {
                        view.hideLoading();
                        view.showFiles(fetched);
                    }
                }

                @Override
                public void onError(Throwable t) {
                    if (view != null) {
                        view.hideLoading();
                        view.showError(t.getMessage());
                    }
                }
            });
        }
    }

    @Override
    public void uploadFile(String token, Uri uri) {
        if (view != null) {
            view.showLoading();
            service.uploadFile(token, uri, new FolderModel.UploadCallback() {
                @Override
                public void onSuccess() {
                    if (view != null) {
                        view.hideLoading();
                        view.showUploadSuccess();
                        // Recargar archivos después de subir uno nuevo
                        loadFiles(token);
                    }
                }

                @Override
                public void onError(Throwable t) {
                    if (view != null) {
                        view.hideLoading();
                        view.showError(t.getMessage());
                    }
                }
            });
        }
    }
}
