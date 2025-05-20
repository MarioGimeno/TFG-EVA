// src/main/java/com/example/appGrabacion/presenters/CategoriasPresenter.java
package com.example.appGrabacion.presenters;

import com.example.appGrabacion.contracts.CategoriasContract;
import com.example.appGrabacion.models.Categoria;
import com.example.appGrabacion.services.CategoriaService;
import com.example.appGrabacion.services.CategoriaService.CategoriaCallback;
import java.util.List;

public class CategoriasPresenter implements CategoriasContract.Presenter {
    private CategoriasContract.View view;
    private final CategoriaService service;

    public CategoriasPresenter(CategoriaService service) {
        this.service = service;
    }

    @Override
    public void attachView(CategoriasContract.View view) {
        this.view = view;
    }

    @Override
    public void detachView() {
        this.view = null;
    }

    @Override
    public void loadCategories() {
        if (view == null) return;
        view.showLoading();
        service.fetchAll(new CategoriaCallback() {
            @Override
            public void onSuccess(List<Categoria> list) {
                if (view != null) {
                    view.hideLoading();
                    view.showCategories(list);
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
