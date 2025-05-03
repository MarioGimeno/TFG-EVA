package com.example.appGrabacion.models;

public class ContactEntry {
    private int id;
    private String name;
    private String email;

    // Constructor principal (al mappear desde la API)
    public ContactEntry(int id, String name, String email) {
        this.id    = id;
        this.name  = name;
        this.email = email;
    }

    // Nuevo constructor para crear LOCALMENTE antes de tener un id
    public ContactEntry(String name, String email) {
        this(0, name, email);
    }

    // Getters y setters
    public int getId()             { return id; }
    public String getName()        { return name; }
    public String getEmail()       { return email; }
    public void setId(int id)      { this.id = id; }
    public void setName(String n)  { this.name = n; }
    public void setEmail(String e) { this.email = e; }
}
