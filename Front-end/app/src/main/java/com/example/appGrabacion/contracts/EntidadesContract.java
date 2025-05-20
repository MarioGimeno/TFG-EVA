// com/example/appGrabacion/contracts/EntidadesContract.java
package com.example.appGrabacion.contracts;

import com.example.appGrabacion.models.Entidad;
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
}
