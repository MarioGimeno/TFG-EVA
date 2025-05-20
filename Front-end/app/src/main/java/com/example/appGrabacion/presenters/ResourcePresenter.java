package com.example.appGrabacion.presenters;

import com.example.appGrabacion.contracts.ResourceContract;
import com.example.appGrabacion.models.Recurso;

import java.util.List;

public class ResourcePresenter implements ResourceContract.Presenter {

    private ResourceContract.View view;
    private final ResourceContract.Service service;

    public ResourcePresenter(ResourceContract.Service service) {
        this.service = service;
    }

    @Override
    public void attachView(ResourceContract.View view) {
        this.view = view;
    }

    @Override
    public void detachView() {
        this.view = null;
    }

    @Override
    public void loadAllResources() {
        if (view == null) return;
        view.showLoading();
        service.fetchAll(new ResourceContract.Service.ResourceCallback() {
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
                view.showError(t.getMessage());
            }
        });
    }

    @Override
    public void loadResourcesByCategory(int categoriaId) {
        if (view == null) return;
        view.showLoading();
        service.fetchByCategory(categoriaId, new ResourceContract.Service.ResourceCallback() {
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
                view.showError(t.getMessage());
            }
        });
    }

    @Override
    public void loadResourceById(int id) {
        if (view == null) return;
        view.showLoading();
        service.fetchById(id, new ResourceContract.Service.ResourceDetailCallback() {
            @Override
            public void onSuccess(Recurso recurso) {
                if (view == null) return;
                view.hideLoading();
                view.showResourceDetail(recurso);
            }

            @Override
            public void onError(Throwable t) {
                if (view == null) return;
                view.hideLoading();
                view.showError(t.getMessage());
            }
        });
    }

    @Override
    public void loadGratuitos() {
        if (view == null) return;
        view.showLoading();
        service.fetchGratuitos(new ResourceContract.Service.ResourceCallback() {
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
                view.showError(t.getMessage());
            }
        });
    }

    @Override
    public void loadAccesibles() {
        if (view == null) return;
        view.showLoading();
        service.fetchAccesibles(new ResourceContract.Service.ResourceCallback() {
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
                view.showError(t.getMessage());
            }
        });
    }
}
