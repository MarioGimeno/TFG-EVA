// src/main/java/com/example/appGrabacion/presenters/EntidadDetailPresenter.java
package com.example.appGrabacion.presenters;

import com.example.appGrabacion.contracts.EntidadDetailContract;
import com.example.appGrabacion.models.Entidad;
import com.example.appGrabacion.models.Recurso;
import com.example.appGrabacion.services.EntityService;
import com.example.appGrabacion.services.ResourceService;

import java.util.ArrayList;
import java.util.List;

public class EntidadDetailPresenter implements EntidadDetailContract.Presenter {
    private EntidadDetailContract.View view;
    private final EntityService   entityService;
    private final ResourceService resourceService;

    public EntidadDetailPresenter(EntityService entityService,
                                  ResourceService resourceService) {
        this.entityService   = entityService;
        this.resourceService = resourceService;
    }

    @Override
    public void attachView(EntidadDetailContract.View view) {
        this.view = view;
    }

    @Override
    public void detachView() {
        this.view = null;
    }

    @Override
    public void loadEntidadAndResources(int entidadId) {
        if (view == null) return;
        view.showLoading();

        entityService.fetchById(entidadId, new EntityService.EntityDetailCallback() {
            @Override
            public void onSuccess(Entidad e) {
                if (view == null) return;
                view.showEntidad(e);

                resourceService.fetchAll(new ResourceService.ResourceCallback() {
                    @Override
                    public void onSuccess(List<Recurso> list) {
                        if (view == null) return;
                        List<Recurso> filtered = new ArrayList<>();
                        for (Recurso r : list) {
                            if (r.getIdEntidad() == entidadId) {
                                filtered.add(r);
                            }
                        }
                        view.hideLoading();
                        view.showResources(filtered);
                    }
                    @Override
                    public void onError(Throwable t) {
                        if (view == null) return;
                        view.hideLoading();
                        view.showError("Error cargando recursos: " + t.getMessage());
                    }
                });
            }
            @Override
            public void onError(Throwable t) {
                if (view == null) return;
                view.hideLoading();
                view.showError("Error cargando entidad: " + t.getMessage());
            }
        });
    }
}
