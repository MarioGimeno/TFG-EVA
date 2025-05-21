package com.example.appGrabacion.entities;

import com.google.gson.annotations.SerializedName;

public class Categoria {
    @SerializedName("id_categoria")
    private int idCategoria;

    @SerializedName("nombre")
    private String nombre;

    @SerializedName("img_categoria")
   private String imgCategoria;

    public int getIdCategoria() { return idCategoria; }
    public void setIdCategoria(int idCategoria) { this.idCategoria = idCategoria; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getImgCategoria() {
           return imgCategoria;
    }
    public void setImgCategoria(String imgCategoria) {
        this.imgCategoria = imgCategoria;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Categoria)) return false;
        Categoria that = (Categoria) o;
        return idCategoria == that.idCategoria
                && nombre.equals(that.nombre)
                && ((imgCategoria == null && that.imgCategoria == null)
                || (imgCategoria != null && imgCategoria.equals(that.imgCategoria)));
    }
}