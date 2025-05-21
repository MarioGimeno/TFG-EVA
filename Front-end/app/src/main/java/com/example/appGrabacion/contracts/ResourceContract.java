package com.example.appGrabacion.contracts;

import com.example.appGrabacion.entities.Recurso;

import java.util.List;

public interface ResourceContract {

    interface View {
        void showLoading();
        void hideLoading();
        void showResources(List<Recurso> recursos);
        void showResourceDetail(Recurso recurso);
        void showError(String message);
    }

    interface Presenter {
        void attachView(View view);
        void detachView();
        void loadAllResources();
        void loadResourcesByCategory(int categoriaId);
        void loadResourceById(int id);
        void loadGratuitos();
        void loadAccesibles();
    }

    interface Service {
        interface ResourceCallback {
            void onSuccess(List<Recurso> recursos);
            void onError(Throwable t);
        }
        interface ResourceDetailCallback {
            void onSuccess(Recurso recurso);
            void onError(Throwable t);
        }

        void fetchAll(ResourceCallback callback);
        void fetchByCategory(int categoriaId, ResourceCallback callback);
        void fetchById(int id, ResourceDetailCallback callback);
        void fetchGratuitos(ResourceCallback callback);
        void fetchAccesibles(ResourceCallback callback);
    }
}
