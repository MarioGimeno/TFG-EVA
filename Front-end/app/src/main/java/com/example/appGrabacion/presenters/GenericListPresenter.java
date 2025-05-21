package com.example.appGrabacion.presenters;

import com.example.appGrabacion.contracts.GenericListContract;
import com.example.appGrabacion.entities.Entidad;
import com.example.appGrabacion.entities.Recurso;
import com.example.appGrabacion.models.GenericActivityModel;

import java.util.List;

public class GenericListPresenter implements GenericListContract.Presenter {
    private GenericListContract.View view;
    private GenericActivityModel service;

    public GenericListPresenter(GenericActivityModel service) {
        this.service = service;
    }

    @Override
    public void attachView(GenericListContract.View view) {
        this.view = view;
    }

    @Override
    public void detachView() {
        this.view = null;
    }

    @Override
    public void loadItems(String type, int categoryId) {
        if (view == null) return;
        view.showLoading();

        switch (type.toLowerCase()) {
            case "entidades":
                service.loadEntidades(new GenericActivityModel.LoadCallback<Entidad>() {
                    @Override
                    public void onSuccess(List<Entidad> items) {
                        if (view != null) {
                            view.hideLoading();
                            view.showItems(items);
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
                break;
            case "servicios":
                service.loadServicios(new GenericActivityModel.LoadCallback<Recurso>() {
                    @Override
                    public void onSuccess(List<Recurso> items) {
                        if (view != null) {
                            view.hideLoading();
                            view.showItems(items);
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
                break;
            case "gratuitos":
                service.loadGratuitos(new GenericActivityModel.LoadCallback<Recurso>() {
                    @Override
                    public void onSuccess(List<Recurso> items) {
                        if (view != null) {
                            view.hideLoading();
                            view.showItems(items);
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
                break;
            case "accesibles":
                service.loadAccesibles(new GenericActivityModel.LoadCallback<Recurso>() {
                    @Override
                    public void onSuccess(List<Recurso> items) {
                        if (view != null) {
                            view.hideLoading();
                            view.showItems(items);
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
                break;
            default:
                if (categoryId > 0) {
                    service.loadServiciosPorCategoria(categoryId, new GenericActivityModel.LoadCallback<Recurso>() {
                        @Override
                        public void onSuccess(List<Recurso> items) {
                            if (view != null) {
                                view.hideLoading();
                                view.showItems(items);
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
}