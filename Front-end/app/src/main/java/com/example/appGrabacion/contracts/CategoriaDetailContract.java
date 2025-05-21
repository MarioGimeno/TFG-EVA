// src/main/java/com/example/appGrabacion/contracts/CategoriaDetailContract.java
package com.example.appGrabacion.contracts;

import com.example.appGrabacion.entities.Categoria;
import com.example.appGrabacion.entities.Recurso;
import java.util.List;

public interface CategoriaDetailContract {
    interface View {
        void showLoading();
        void hideLoading();
        void showCategory(Categoria categoria);
        void showResources(List<Recurso> recursos);
        void showError(String message);
    }

    interface Presenter {
        void attachView(View view);
        void detachView();
        void loadCategoryAndResources(int categoryId);
    }
}
