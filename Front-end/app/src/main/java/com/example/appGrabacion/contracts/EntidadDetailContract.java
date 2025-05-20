// src/main/java/com/example/appGrabacion/contracts/EntidadDetailContract.java
package com.example.appGrabacion.contracts;

import com.example.appGrabacion.models.Entidad;
import com.example.appGrabacion.models.Recurso;
import java.util.List;

public interface EntidadDetailContract {
    interface View {
        void showLoading();
        void hideLoading();
        void showEntidad(Entidad entidad);
        void showResources(List<Recurso> recursos);
        void showError(String message);
    }

    interface Presenter {
        void attachView(View view);
        void detachView();
        void loadEntidadAndResources(int entidadId);
    }
}
