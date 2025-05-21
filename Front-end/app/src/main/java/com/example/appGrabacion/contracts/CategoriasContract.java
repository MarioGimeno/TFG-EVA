package com.example.appGrabacion.contracts;

import com.example.appGrabacion.entities.Categoria;
import java.util.List;

public interface CategoriasContract {
    interface View {
        void showLoading();
        void hideLoading();
        void showCategories(List<Categoria> categorias);
        void showCategory(Categoria categoria); // <-- método para detalle categoría
        void showError(String message);
    }

    interface Presenter {
        void attachView(View view);
        void detachView();
        void loadCategories();
        void loadCategoryById(int categoryId);  // <-- método para detalle categoría
    }

    interface Service {
        interface Callback<T> {
            void onSuccess(T result);
            void onError(Throwable t);
        }

        void fetchAll(Callback<List<Categoria>> callback);
        void fetchById(int id, Callback<Categoria> callback);
    }
}
