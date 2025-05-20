// src/main/java/com/example/appGrabacion/contracts/CategoriasContract.java
package com.example.appGrabacion.contracts;

import com.example.appGrabacion.models.Categoria;
import java.util.List;

public interface CategoriasContract {
    interface View {
        void showLoading();
        void hideLoading();
        void showCategories(List<Categoria> categorias);
        void showError(String message);
    }

    interface Presenter {
        void attachView(View view);
        void detachView();
        void loadCategories();
    }
}
