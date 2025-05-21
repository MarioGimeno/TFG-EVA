package com.example.appGrabacion.presenters;

import com.example.appGrabacion.contracts.CategoriasContract;
import com.example.appGrabacion.entities.Categoria;

import java.util.List;

public class CategoriasPresenter implements CategoriasContract.Presenter {
    private CategoriasContract.View view;
    private final CategoriasContract.Service service;

    public CategoriasPresenter(CategoriasContract.Service service) {
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
        service.fetchAll(new CategoriasContract.Service.Callback<List<Categoria>>() {
            @Override
            public void onSuccess(List<Categoria> categorias) {
                if (view != null) {
                    view.hideLoading();
                    view.showCategories(categorias);
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

    @Override
    public void loadCategoryById(int categoryId) {
        if (view == null) return;
        view.showLoading();
        service.fetchById(categoryId, new CategoriasContract.Service.Callback<Categoria>() {
            @Override
            public void onSuccess(Categoria categoria) {
                if (view != null) {
                    view.hideLoading();
                    view.showCategory(categoria);
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
