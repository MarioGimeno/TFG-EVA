package com.example.appGrabacion.presenters;

import com.example.appGrabacion.contracts.CategoriaDetailContract;
import com.example.appGrabacion.entities.Categoria;
import com.example.appGrabacion.entities.Recurso;
import com.example.appGrabacion.models.CategoriaModel;
import com.example.appGrabacion.models.ResourceModel;

import java.util.List;

public class CategoriaDetailPresenter implements CategoriaDetailContract.Presenter {
    private CategoriaDetailContract.View view;
    private final CategoriaModel categoriaModel;
    private final ResourceModel resourceModel;

    public CategoriaDetailPresenter(CategoriaModel categoriaModel,
                                    ResourceModel resourceModel) {
        this.categoriaModel = categoriaModel;
        this.resourceModel = resourceModel;
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

        // Cargar detalles de la categoría
        categoriaModel.fetchById(categoryId, new CategoriaModel.Callback<Categoria>() {
            @Override
            public void onSuccess(Categoria categoria) {
                if (view == null) return;
                if (categoria == null) {
                    view.hideLoading();
                    view.showError("Categoría no encontrada");
                    return;
                }
                view.showCategory(categoria);

                // Cargar recursos de esa categoría
                resourceModel.fetchByCategory(categoryId, new ResourceModel.ResourceCallback() {
                    @Override
                    public void onSuccess(List<Recurso> recursos) {
                        if (view == null) return;
                        view.hideLoading();
                        view.showResources(recursos);
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
