package com.example.appGrabacion.entities;

import com.google.gson.annotations.SerializedName;
public class Entidad {
    @SerializedName("id_entidad")
    private int idEntidad;

    @SerializedName("imagen")
    private String imagen;

    @SerializedName("email")
    private String email;

    @SerializedName("telefono")
    private String telefono;

    @SerializedName("pagina_web")
    private String paginaWeb;

    @SerializedName("direccion")
    private String direccion;

    @SerializedName("horario")
    private String horario;

    // Getters y setters
    public int getIdEntidad() { return idEntidad; }
    public void setIdEntidad(int idEntidad) { this.idEntidad = idEntidad; }

    public String getImagen() { return imagen; }
    public void setImagen(String imagen) { this.imagen = imagen; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getPaginaWeb() { return paginaWeb; }
    public void setPaginaWeb(String paginaWeb) { this.paginaWeb = paginaWeb; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getHorario() { return horario; }
    public void setHorario(String horario) { this.horario = horario; }
}
