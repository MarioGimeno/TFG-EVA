// com/example/appGrabacion/presenters/EntidadesPresenter.java
package com.example.appGrabacion.presenters;

import com.example.appGrabacion.contracts.EntidadesContract;
import com.example.appGrabacion.models.Entidad;
import com.example.appGrabacion.services.EntityModel;

import java.util.List;

public class EntidadesPresenter implements EntidadesContract.Presenter {

    private EntidadesContract.View view;
    private final EntityModel service;

    public EntidadesPresenter(EntityModel service) {
        this.service = service;
    }

    @Override
    public void attachView(EntidadesContract.View view) {
        this.view = view;
    }

    @Override
    public void detachView() {
        this.view = null;
    }

    @Override
    public void loadEntidades() {
        if (view != null) {
            view.showLoading();
            service.fetchAll(new EntityModel.EntityCallback() {
                @Override
                public void onSuccess(List<Entidad> list) {
                    if (view != null) {
                        view.hideLoading();
                        view.showEntidades(list);
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
}
