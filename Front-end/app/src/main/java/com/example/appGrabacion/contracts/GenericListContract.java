package com.example.appGrabacion.contracts;

import com.example.appGrabacion.entities.Entidad;
import com.example.appGrabacion.entities.Recurso;

import java.util.List;

public interface GenericListContract {

    interface Service {
        interface LoadCallback<T> {
            void onSuccess(List<T> items);
            void onError(Throwable t);
        }

        void loadEntidades(LoadCallback<Entidad> callback);
        void loadServicios(LoadCallback<Recurso> callback);
        void loadServiciosPorCategoria(int categoriaId, LoadCallback<Recurso> callback);
        void loadGratuitos(LoadCallback<Recurso> callback);
        void loadAccesibles(LoadCallback<Recurso> callback);
    }

    interface View {
        void showLoading();
        void hideLoading();
        void showItems(List<?> items);
        void showError(String message);
    }

    interface Presenter {
        void attachView(View view);
        void detachView();
        void loadItems(String type, int categoryId);
    }
}
