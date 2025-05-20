package com.example.appGrabacion.services;

import android.content.Context;

import com.example.appGrabacion.contracts.GenericListContract;
import com.example.appGrabacion.models.Entidad;
import com.example.appGrabacion.models.Recurso;

import java.util.List;

public class GenericActivityModel implements GenericListContract.Service {

    private final EntityModel entityModel;
    private final ResourceModel resourceModel;

    public GenericActivityModel(Context ctx) {
        entityModel = new EntityModel(ctx);
        resourceModel = new ResourceModel(ctx);
    }

    @Override
    public void loadEntidades(final GenericListContract.Service.LoadCallback<Entidad> cb) {
        entityModel.fetchAll(new EntityModel.EntityCallback() {
            @Override public void onSuccess(List<Entidad> list) {
                cb.onSuccess(list);
            }
            @Override public void onError(Throwable t) {
                cb.onError(t);
            }
        });
    }

    @Override
    public void loadServicios(final GenericListContract.Service.LoadCallback<Recurso> cb) {
        resourceModel.fetchAll(new ResourceModel.ResourceCallback() {
            @Override public void onSuccess(List<Recurso> list) {
                cb.onSuccess(list);
            }
            @Override public void onError(Throwable t) {
                cb.onError(t);
            }
        });
    }

    @Override
    public void loadServiciosPorCategoria(int categoria, final GenericListContract.Service.LoadCallback<Recurso> cb) {
        resourceModel.fetchByCategory(categoria, new ResourceModel.ResourceCallback() {
            @Override public void onSuccess(List<Recurso> list) {
                cb.onSuccess(list);
            }
            @Override public void onError(Throwable t) {
                cb.onError(t);
            }
        });
    }

    @Override
    public void loadGratuitos(final GenericListContract.Service.LoadCallback<Recurso> cb) {
        resourceModel.fetchGratuitos(new ResourceModel.ResourceCallback() {
            @Override public void onSuccess(List<Recurso> list) {
                cb.onSuccess(list);
            }
            @Override public void onError(Throwable t) {
                cb.onError(t);
            }
        });
    }

    @Override
    public void loadAccesibles(final GenericListContract.Service.LoadCallback<Recurso> cb) {
        resourceModel.fetchAccesibles(new ResourceModel.ResourceCallback() {
            @Override public void onSuccess(List<Recurso> list) {
                cb.onSuccess(list);
            }
            @Override public void onError(Throwable t) {
                cb.onError(t);
            }
        });
    }
}
