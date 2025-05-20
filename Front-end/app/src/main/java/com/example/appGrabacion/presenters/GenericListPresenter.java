package com.example.appGrabacion.presenters;

import com.example.appGrabacion.contracts.GenericListContract;
import com.example.appGrabacion.models.Entidad;
import com.example.appGrabacion.models.Recurso;
import com.example.appGrabacion.services.GenericActivityService;

import java.util.List;

public class GenericListPresenter implements GenericListContract.Presenter {
    private GenericListContract.View view;
    private GenericActivityService service;

    public GenericListPresenter(GenericActivityService service) {
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
                service.loadEntidades(new GenericActivityService.LoadCallback<Entidad>() {
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
                service.loadServicios(new GenericActivityService.LoadCallback<Recurso>() {
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
                service.loadGratuitos(new GenericActivityService.LoadCallback<Recurso>() {
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
                service.loadAccesibles(new GenericActivityService.LoadCallback<Recurso>() {
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
                    service.loadServiciosPorCategoria(categoryId, new GenericActivityService.LoadCallback<Recurso>() {
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