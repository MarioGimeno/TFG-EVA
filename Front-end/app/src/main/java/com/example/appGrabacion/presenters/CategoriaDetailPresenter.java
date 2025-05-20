// src/main/java/com/example/appGrabacion/presenters/CategoriaDetailPresenter.java
package com.example.appGrabacion.presenters;

import com.example.appGrabacion.contracts.CategoriaDetailContract;
import com.example.appGrabacion.models.Categoria;
import com.example.appGrabacion.models.Recurso;
import com.example.appGrabacion.services.CategoriaService;
import com.example.appGrabacion.services.ResourceService;
import java.util.List;

public class CategoriaDetailPresenter implements CategoriaDetailContract.Presenter {
    private CategoriaDetailContract.View view;
    private final CategoriaService categoriaService;
    private final ResourceService   resourceService;

    public CategoriaDetailPresenter(CategoriaService categoriaService,
                                    ResourceService resourceService) {
        this.categoriaService = categoriaService;
        this.resourceService  = resourceService;
    }

    @Override
    public void attachView(CategoriaDetailContract.View view) {
        this.view = view;
    }

    @Override
    public void detachView() {
        this.view = null;
    }

    @Override
    public void loadCategoryAndResources(int categoryId) {
        if (view == null) return;
        view.showLoading();

        // 1) Cargar detalles de la categoría
        categoriaService.fetchById(categoryId, new CategoriaService.CategoriaDetailCallback() {
            @Override
            public void onSuccess(Categoria c) {
                if (view == null) return;
                view.showCategory(c);

                // 2) Cargar recursos de esa categoría
                resourceService.fetchByCategory(categoryId, new ResourceService.ResourceCallback() {
                    @Override
                    public void onSuccess(List<Recurso> list) {
                        if (view == null) return;
                        view.hideLoading();
                        view.showResources(list);
                    }
                    @Override
                    public void onError(Throwable t) {
                        if (view == null) return;
                        view.hideLoading();
                        view.showError("Error al cargar recursos: " + t.getMessage());
                    }
                });
            }

            @Override
            public void onError(Throwable t) {
                if (view == null) return;
                view.hideLoading();
                view.showError("Error al cargar categoría: " + t.getMessage());
            }
        });
    }
}
