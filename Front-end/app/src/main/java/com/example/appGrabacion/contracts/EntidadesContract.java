package com.example.appGrabacion.contracts;

import com.example.appGrabacion.models.Entidad;
import com.example.appGrabacion.services.EntityModel;

import java.util.List;

public interface EntidadesContract {
    interface View {
        void showLoading();
        void hideLoading();
        void showEntidades(List<Entidad> entidades);
        void showError(String message);
    }
    interface Presenter {
        void attachView(View view);
        void detachView();
        void loadEntidades();
    }

    interface Service {
        void fetchAll(EntityModel.EntityCallback callback);
        void fetchById(int id, EntityModel.EntityDetailCallback callback);
    }
}
