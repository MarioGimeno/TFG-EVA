package com.example.appGrabacion.services;

import android.content.Context;

import com.example.appGrabacion.models.Entidad;
import com.example.appGrabacion.models.Recurso;

import java.util.List;

public class GenericActivityService {
    /** Generic callback para cualquier tipo de lista */
    public interface LoadCallback<T> {
        void onSuccess(List<T> items);
        void onError(Throwable t);
    }

    private final EntityService   entityService;
    private final ResourceService resourceService;

    public GenericActivityService(Context ctx) {
        entityService   = new EntityService(ctx);
        resourceService = new ResourceService(ctx);
    }

    /** Carga todas las entidades */
    public void loadEntidades(final LoadCallback<Entidad> cb) {
        entityService.fetchAll(new EntityService.EntityCallback() {
            @Override public void onSuccess(List<Entidad> list) {
                cb.onSuccess(list);
            }
            @Override public void onError(Throwable t) {
                cb.onError(t);
            }
        });
    }

    /** Carga todos los servicios (recursos) */
    public void loadServicios(final LoadCallback<Recurso> cb) {
        resourceService.fetchAll(new ResourceService.ResourceCallback() {
            @Override public void onSuccess(List<Recurso> list) {
                cb.onSuccess(list);
            }
            @Override public void onError(Throwable t) {
                cb.onError(t);
            }
        });
    }

    /** Carga servicios filtrados por categoría */
    public void loadServiciosPorCategoria(String categoria,
                                          final LoadCallback<Recurso> cb) {
        resourceService.fetchByCategory(categoria, new ResourceService.ResourceCallback() {
            @Override public void onSuccess(List<Recurso> list) {
                cb.onSuccess(list);
            }
            @Override public void onError(Throwable t) {
                cb.onError(t);
            }
        });
    }
}
