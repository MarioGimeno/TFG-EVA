package com.example.appGrabacion.models;

import com.google.gson.annotations.SerializedName;
import java.util.Objects;

public class Recurso {
    @SerializedName("id")
    private int id;

    @SerializedName("id_entidad")
    private int idEntidad;

    @SerializedName("id_categoria")
    private int idCategoria;

    @SerializedName("imagen")
    private String imagen;

    @SerializedName("email")
    private String email;

    @SerializedName("telefono")
    private String telefono;

    @SerializedName("direccion")
    private String direccion;

    @SerializedName("horario")
    private String horario;

    @SerializedName("servicio")
    private String servicio;

    @SerializedName("descripcion")
    private String descripcion;

    @SerializedName("requisitos")
    private String requisitos;

    @SerializedName("gratuito")
    private boolean gratuito;

    @SerializedName("web")
    private String web;

    @SerializedName("accesible")
    private boolean accesible;

    // Getters y setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getIdEntidad() { return idEntidad; }
    public void setIdEntidad(int idEntidad) { this.idEntidad = idEntidad; }

    public int getIdCategoria() { return idCategoria; }
    public void setIdCategoria(int idCategoria) { this.idCategoria = idCategoria; }

    public String getImagen() { return imagen; }
    public void setImagen(String imagen) { this.imagen = imagen; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getHorario() { return horario; }
    public void setHorario(String horario) { this.horario = horario; }

    public String getServicio() { return servicio; }
    public void setServicio(String servicio) { this.servicio = servicio; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getRequisitos() { return requisitos; }
    public void setRequisitos(String requisitos) { this.requisitos = requisitos; }

    public boolean isGratuito() { return gratuito; }
    public void setGratuito(boolean gratuito) { this.gratuito = gratuito; }

    public String getWeb() { return web; }
    public void setWeb(String web) { this.web = web; }

    public boolean isAccesible() { return accesible; }
    public void setAccesible(boolean accesible) { this.accesible = accesible; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Recurso)) return false;
        Recurso r = (Recurso) o;
        return id == r.id &&
                idEntidad == r.idEntidad &&
                idCategoria == r.idCategoria &&
                gratuito == r.gratuito &&
                accesible == r.accesible &&
                Objects.equals(imagen, r.imagen) &&
                Objects.equals(email, r.email) &&
                Objects.equals(telefono, r.telefono) &&
                Objects.equals(direccion, r.direccion) &&
                Objects.equals(horario, r.horario) &&
                Objects.equals(servicio, r.servicio) &&
                Objects.equals(descripcion, r.descripcion) &&
                Objects.equals(requisitos, r.requisitos) &&
                Objects.equals(web, r.web);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, idEntidad, idCategoria, imagen, email, telefono,
                direccion, horario, servicio, descripcion,
                requisitos, gratuito, web, accesible);
    }
}
